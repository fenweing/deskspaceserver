package com.parrer.deskspaceserver.websocket;

import java.util.Arrays;

/**
 * @author 90139
 * @version 1.0
 * @date 2019/11/18 15:08
 * @description WS业务处理枚举
 */
public enum WsMethod {
    EVENT_REAL_PLAY_METHOD("realPlay", "realPlayHandleService"),
    EVENT_STOP_REAL_PLAY_METHOD("stopRealPlay", "realPlayHandleService"),
    EVENT_LOT_BORDER_ADD_METHOD("setLotBorder", "lotBorderHandleService"),
    EVENT_LOT_BORDER_GET_METHOD("getLotBorder", "lotBorderHandleService"),
    EVENT_PLAYBACK_METHOD("startPlayBack,stopPlayBack,pausePlayBack,normalPlayBack" +
            ",fastPlayBack,slowPlayBack,speedPlayBack", "playBackServiceImpl"),
    EVENT_QUERY_PLAYBACK_RECORD_METHOD("queryPlayBackRecord", "videoRecordServiceImpl");
    private String methodName;

    private String serviceName;

    WsMethod(String methodName, String serviceName) {
        this.methodName = methodName;
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public static WsMethod getMQMethod(String methodName) {
        WsMethod enumType = null;
        for (WsMethod mqMethod1 : WsMethod.values()) {
            String[] split = mqMethod1.methodName.split(",");
            if (Arrays.asList(split).contains(methodName)) {
                enumType = mqMethod1;
                break;
            }
        }
        return enumType;
    }
}
