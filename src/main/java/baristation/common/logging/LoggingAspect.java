package baristation.common.logging;

import baristation.common.annotation.ExternalApiLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // @ExternalApiLog 어노테이션이 붙은 메서드를 타겟으로 설정
    @Around("@annotation(externalApiLog)")
    public Object logExternalApiExecution(ProceedingJoinPoint jp, ExternalApiLog externalApiLog) throws Throwable {
        String className = jp.getTarget().getClass().getSimpleName();
        String methodName = jp.getSignature().getName();
        String traceId = TraceIdUtil.getTraceId();

        // 어노테이션에 적어둔 설명값 가져오기 (없으면 빈 문자열)
        String apiDescription = externalApiLog.value().isEmpty() ? "" : " [" + externalApiLog.value() + "]";

        // 1. 외부 API 요청 진입 로그
        log.info("[External API Request]{} {} -> {} start. traceId={}",
                apiDescription, className, methodName, traceId);

        long startTime = System.currentTimeMillis();

        try {
            Object result = jp.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // 2. 외부 API 요청 성공 및 소요 시간 로그
            log.info("[External API Response]{} {} -> {} done. duration={}ms, traceId={}",
                    apiDescription, className, methodName, duration, traceId);

            return result;

        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;

            // 3. 외부 API 통신 중 에러 발생 시 소요 시간과 함께 경고 로그 기록
            log.warn("[External API Failed]{} {} -> {} exception={}! duration={}ms, traceId={}",
                    apiDescription, className, methodName, e.getClass().getSimpleName(), duration, traceId);
            throw e;
        }
    }
}