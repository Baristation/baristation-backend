package baristation.user.event;

import baristation.common.logging.TraceIdUtil;
import baristation.common.r2.R2ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;

/**
 * 회원 탈퇴 시 R2 스토리지의 파일 삭제를 처리하는 비동기 이벤트 리스너
 *
 * 주의사항:
 * - DB 트랜잭션이 정상 커밋된 AFTER_COMMIT 단계에서만 실행됨
 * - @Async를 통해 별도의 스레드에서 비동기로 처리되어 응답 지연 방지
 * - 파일 삭제 실패 시 로그만 기록되고 DB 롤백에는 영향을 주지 않음
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class R2FileDeleteEventListener {

    private final R2ImageService r2ImageService;

    /**
     * 회원 삭제 이벤트 처리
     *
     * @param event 삭제 대상 회원의 파일 키 목록을 담은 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        List<String> fileKeys = event.r2FileKeys();

        log.info("[R2FileDelete] 탈퇴 회원[{}]의 파일 삭제 시작. fileCount={}, traceId={}",
                event.userId(), fileKeys.size(), TraceIdUtil.getTraceId());

        if (fileKeys.isEmpty()) {
            log.info("[R2FileDelete] 삭제할 파일이 없습니다. userId={}, traceId={}",
                    event.userId(), TraceIdUtil.getTraceId());
            return;
        }

        for (String fileKey : fileKeys) {
            if (fileKey == null || fileKey.isEmpty()) {
                log.warn("[R2FileDelete] 빈 파일 키가 전달되었습니다. userId={}", event.userId());
                continue;
            }

            try {
                // R2 스토리지에서 파일 삭제
                r2ImageService.deleteByUrl(fileKey);
                log.info("[R2FileDelete] 파일 삭제 성공. userId={}, fileKey={}, traceId={}",
                        event.userId(), fileKey, TraceIdUtil.getTraceId());
            } catch (Exception e) {
                // 비동기 스레드에서 예외가 발생해도 DB 트랜잭션에는 영향을 주지 않음
                // 고아 파일이 되며, 수동 정리 스크립트나 모니터링을 통해 처리 필요
                log.error("[R2FileDelete] 파일 삭제 실패. userId={}, fileKey={}, traceId={}, error={}",
                        event.userId(), fileKey, TraceIdUtil.getTraceId(), e.getMessage(), e);
            }
        }

        log.info("[R2FileDelete] 탈퇴 회원[{}]의 파일 삭제 완료. traceId={}",
                event.userId(), TraceIdUtil.getTraceId());
    }
}

