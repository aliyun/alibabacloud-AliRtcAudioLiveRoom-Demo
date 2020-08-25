package com.alivc.base;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * TraceId跟踪工具类
 *
 * @author haihua.whh
 * @date  2018-12-29
 */
public class TraceIdContext {

    public static ThreadLocal<TraceIdContext> ctx = new InheritableThreadLocal<TraceIdContext>(){
        @Override
        protected TraceIdContext initialValue() {
            return new TraceIdContext();
        }
    };

    private String traceId;

    public String getTraceId() {
        if (StringUtils.isEmpty(traceId)){
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

}
