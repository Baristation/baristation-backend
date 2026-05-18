package baristation.common.logging;

import org.slf4j.MDC;

public final class TraceIdUtil {

    public static final String TRACE_ID_KEY = "traceId";

    private TraceIdUtil() {
    }

    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return traceId == null ? "-" : traceId;
    }
}

