#!/bin/bash

set -euo pipefail

USERNAME="${USERNAME:?USERNAME is required}"
DOCKER_APP_IMAGE="${DOCKER_APP_IMAGE:?DOCKER_APP_IMAGE is required}"

COMPOSE_FILE="${COMPOSE_FILE:-/home/${USERNAME}/deploy/compose.blue-green.yml}"
NGINX_CONFIG_PATH="${NGINX_CONFIG_PATH:-/etc/nginx/sites-available/ssl.conf}"

BLUE_PORT=8080
GREEN_PORT=8081

BLUE_HEALTH_CHECK_URL="http://127.0.0.1:8082/actuator/health"
GREEN_HEALTH_CHECK_URL="http://127.0.0.1:8083/actuator/health"

HEALTH_CHECK_ATTEMPTS="${HEALTH_CHECK_ATTEMPTS:-10}"
HEALTH_CHECK_DELAY="${HEALTH_CHECK_DELAY:-5}"
BEFORE_HEALTH_CHECK_DELAY="${BEFORE_HEALTH_CHECK_DELAY:-15}"

run_sudo() {
    if ! sudo -n "$@" 2>/dev/null; then
        echo "Passwordless sudo is required for this command: $*"
        echo "Configure sudoers for ${USER:-the deploy user} before running this script."
        exit 1
    fi
}

health_check() {
    local target_url="$1"

    echo "Performing health check for ${target_url}"

    for attempt in $(seq 1 "${HEALTH_CHECK_ATTEMPTS}"); do
        local response
        response=$(curl -s -o /dev/null -w "%{http_code}" "${target_url}" || true)

        if [ "${response}" = "200" ]; then
            echo "Health check passed (${attempt}/${HEALTH_CHECK_ATTEMPTS})"
            return 0
        fi

        echo "Health check failed (${attempt}/${HEALTH_CHECK_ATTEMPTS}), response=${response}"
        sleep "${HEALTH_CHECK_DELAY}"
    done

    return 1
}

switch_container() {
    local previous_container="$1"
    local previous_port="$2"
    local next_container="$3"
    local next_port="$4"
    local health_check_url="$5"

    echo "Pulling latest image ${DOCKER_APP_IMAGE}"
    docker pull "${DOCKER_APP_IMAGE}"

    echo "Starting ${next_container}"
    docker compose -f "${COMPOSE_FILE}" up -d "${next_container}"

    echo "Waiting ${BEFORE_HEALTH_CHECK_DELAY}s for startup"
    sleep "${BEFORE_HEALTH_CHECK_DELAY}"

    if ! health_check "${health_check_url}"; then
        echo "Health check failed for ${next_container}, rolling back"
        docker compose -f "${COMPOSE_FILE}" stop "${next_container}"
        exit 1
    fi

    echo "Switching nginx upstream from ${previous_port} to ${next_port}"
    run_sudo sed -i "s/server 127.0.0.1:${previous_port};/server 127.0.0.1:${next_port};/" "${NGINX_CONFIG_PATH}"
    run_sudo nginx -t
    run_sudo nginx -s reload

    echo "Stopping previous container ${previous_container}"
    docker rm -f "${previous_container}" >/dev/null 2>&1 || true
}

if docker ps --format '{{.Names}}' | grep -qx 'app-green'; then
    echo "### GREEN -> BLUE ###"
    switch_container "app-green" "${GREEN_PORT}" "app-blue" "${BLUE_PORT}" "${BLUE_HEALTH_CHECK_URL}"
else
    echo "### BLUE -> GREEN ###"
    switch_container "app-blue" "${BLUE_PORT}" "app-green" "${GREEN_PORT}" "${GREEN_HEALTH_CHECK_URL}"
fi

echo "Deployment completed successfully"
