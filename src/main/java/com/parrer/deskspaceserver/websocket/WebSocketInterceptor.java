package com.parrer.deskspaceserver.websocket;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {
    /**
     * handler处理前调用,attributes属性最终在WebSocketSession里,可能通过webSocketSession.getAttributes().get(key值)获得
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler webSocketHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            // 获取请求路径携带的参数
            String requestId = serverHttpRequest.getServletRequest().getParameter("source");
            if(StringUtils.isBlank(requestId)){
                return true;
            }
            attributes.put("source", requestId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler webSocketHandler, Exception e) {

    }
}
