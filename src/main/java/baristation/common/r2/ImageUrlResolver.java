package baristation.common.r2;

import baristation.common.logging.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ImageUrlResolver {

    private final String publicBaseUrl;

    public ImageUrlResolver(@Value("${cloudflare.r2.public-base-url}") String publicBaseUrl) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
    }

    // DB에 저장된 objectKey/path 값을 프론트에서 접근 가능한 전체 public URL로 변환합니다.
    public String toPublicUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return imagePath;
        }

        if (isAbsoluteUrl(imagePath)) {
            return imagePath;
        }

        // DB에는 objectKey만 저장하고, 프론트 응답 직전에 public URL prefix를 붙입니다.
        String publicUrl = publicBaseUrl + "/" + trimLeadingSlash(imagePath);
        log.debug("[ImageUrl] public URL resolved. imagePath={}, publicUrl={}, traceId={}",
                imagePath, publicUrl, TraceIdUtil.getTraceId());
        return publicUrl;
    }

    // 전체 public URL 또는 /로 시작하는 path 값을 R2에서 사용하는 objectKey 형태로 변환합니다.
    public String toObjectKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }

        if (imageUrl.startsWith(publicBaseUrl + "/")) {
            // 기존 DB 데이터나 요청값이 전체 URL이어도 R2 삭제/수정에는 objectKey만 사용합니다.
            String objectKey = imageUrl.substring((publicBaseUrl + "/").length());
            log.debug("[ImageUrl] objectKey extracted from public URL. objectKey={}, traceId={}",
                    objectKey, TraceIdUtil.getTraceId());
            return objectKey;
        }

        String objectKey = trimLeadingSlash(imageUrl);
        log.debug("[ImageUrl] objectKey normalized. objectKey={}, traceId={}",
                objectKey, TraceIdUtil.getTraceId());
        return objectKey;
    }

    // URL 경로 확인
    public boolean isExternalUrl(String imageUrl) {
        return imageUrl != null
                && isAbsoluteUrl(imageUrl)
                && !imageUrl.startsWith(publicBaseUrl + "/");
    }

    // http:// 또는 https://로 시작하는 절대 URL인지 확인합니다.
    private boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    // objectKey 조립 시 중복 슬래시가 생기지 않도록 앞쪽 /를 제거합니다.
    private String trimLeadingSlash(String value) {
        return value.startsWith("/")
                ? value.substring(1)
                : value;
    }

    // publicBaseUrl 뒤쪽 /를 제거해 URL 조립 시 //가 생기지 않게 합니다.
    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }
}
