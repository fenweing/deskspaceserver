package com.parrer.deskspaceserver.websocket;

import com.parrer.deskspaceserver.constant.OpsTypeEnum;
import com.parrer.deskspaceserver.log.ConnectInfo;
import com.parrer.deskspaceserver.log.WebsocketMessage;
import com.parrer.deskspaceserver.log.WebsocketResponse;
import com.parrer.deskspaceserver.log.WebsocketResponseWaitContext;
import com.parrer.exception.ServiceException;
import com.parrer.util.CollcUtil;
import com.parrer.util.JsonUtil;
import com.parrer.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 */
@Component
@Slf4j
public class MyWebSocketHandler extends AbstractWebSocketHandler {
    @Autowired
    private WebsocketResponseWaitContext waitContext;
    private Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);
    private static Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private HashMap<ConnectInfo, HashSet<WebSocketSession>> connectInfoSessionMap = new HashMap<>();
    private HashMap<String, WebSocketSession> connectionUuidSessionMap = new HashMap<>();
    private HashSet<WebSocketSession> browserSession = new HashSet<>();
    private HashMap<ConnectInfo, HashSet<WebSocketSession>> connectInfoSessionBrowserMap = new HashMap<>();
    private HashMap<String, WebSocketSession> connectionUuidSessionBrowserMap = new HashMap<>();

    /**
     * webSocket?????????????????????
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
        Object source = session.getAttributes().get("source");
        if (source != null && "browser".equals(source)) {
            browserSession.add(session);
        }
    }

    /**
     * ????????????????????????
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            if (message instanceof TextMessage) {
//            log.info("get text message-{}", message);
                TextMessage textMessage = (TextMessage) message;
                String payload = textMessage.getPayload();
                if (StringUtils.isBlank(payload)) {
                    LogUtil.info("?????????????????????");
                    return;
                }
                try {
                    if (browserSession.contains(session)) {
                        WebsocketMessage websocketMessage = JsonUtil.toObject(payload, WebsocketMessage.class);
                        dealBrowserMessage(session, websocketMessage);
                        return;
                    }
                    WebsocketResponse websocketResponse = JsonUtil.toObject(payload, WebsocketResponse.class);
                    Integer opsType = websocketResponse.getOpsType();
                    if (opsType == null) {
                        LogUtil.error("???????????????????????????-{}", payload);
                        return;
                    }
                    if (OpsTypeEnum.REGISTRY.getKey().equals(opsType)) {
                        LogUtil.info("??????????????????????????????-{}", payload);
                        List<ConnectInfo> connectInfoList = websocketResponse.getConnectInfoList();
                        if (CollectionUtils.isEmpty(connectInfoList)) {
                            LogUtil.error("?????????????????????????????????-{}", payload);
                            return;
                        }
                        for (ConnectInfo connectInfo : connectInfoList) {
                            connectInfoSessionMap.computeIfAbsent(connectInfo, ci -> CollcUtil.ofHashSet(session));
                            //todo ???????????????connectionInfo
                            connectInfoSessionMap.get(connectInfo).clear();
                            connectInfoSessionMap.get(connectInfo).add(session);
                        }
                        return;
                    }
                    if (OpsTypeEnum.CONNECT.getKey().equals(opsType)) {
                        String connectionUuid = websocketResponse.getConnectionUuid();
                        if (StringUtils.isBlank(connectionUuid)) {
                            LogUtil.error("???connectionUuid???");
                            return;
                        }
                        connectionUuidSessionMap.put(connectionUuid, session);
                        WebSocketSession browserSession = connectionUuidSessionBrowserMap.get(connectionUuid);
                        sendBrowserResponse(browserSession, websocketResponse);
//                        waitContext.putConnectInfo(websocketResponse);
                        return;
                    }
                    if (OpsTypeEnum.COMMAND.getKey().equals(opsType)) {
                        String connectionUuid = websocketResponse.getConnectionUuid();
//                        if (StringUtils.isBlank(connectionUuid)) {
//                            LogUtil.error("???connectionUuid???");
//                            return;
//                        }
                        WebSocketSession browserSession = connectionUuidSessionBrowserMap.get(connectionUuid);
                        sendBrowserResponse(browserSession, websocketResponse);
//                        waitContext.putCommandResult(websocketResponse);
                        return;
                    }
                    if (OpsTypeEnum.HANDSHAKE.getKey().equals(opsType)) {
                        return;
                    }
                    LogUtil.error("?????????????????????-{}", websocketResponse);
                } catch (Exception e) {
                    LogUtil.error(e, "????????????????????????-{}", payload);
                }
            } else {
                logger.info("Unexpected WebSocket message type: " + message);
            }
        } catch (Exception e) {
            LogUtil.error(e, "?????????????????????");
        }
    }

    private void sendBrowserResponse(WebSocketSession browserSession, WebsocketResponse websocketResponse) {
        doSend(browserSession, JsonUtil.toString(websocketResponse));
    }

    private void dealBrowserMessage(WebSocketSession session, WebsocketMessage websocketMessage) {
        Integer opsType = websocketMessage.getOpsType();
        if (opsType == null) {
            LogUtil.error("?????????????????????????????????????????????-{}", websocketMessage);
            return;
        }
        if (OpsTypeEnum.CONNECT.getKey().equals(opsType)) {
            websocketMessage.setConnectionUuid(UUID.randomUUID().toString());
            connectionUuidSessionBrowserMap.put(websocketMessage.getConnectionUuid(), session);
            send(websocketMessage);
            return;
        }
        if (OpsTypeEnum.COMMAND.getKey().equals(opsType)) {
            send(websocketMessage);
            return;
        }
        if (OpsTypeEnum.DISCONNECT.getKey().equals(opsType)) {
            send(websocketMessage);
            return;
        }
    }

    public Boolean send(WebsocketMessage message) {
        if (message == null) {
            return false;
        }
        Integer opsType = message.getOpsType();
        if (opsType == null) {
            LogUtil.error("??????????????????-{}", message);
            return false;
        }
        if (OpsTypeEnum.CONNECT.getKey().equals(opsType)) {
            ConnectInfo connectInfo = message.getConnectInfo();
            HashSet<WebSocketSession> webSocketSessions = connectInfoSessionMap.get(connectInfo);
            if (CollectionUtils.isEmpty(webSocketSessions)) {
                LogUtil.error("??????????????????????????????????????????-{}", message);
                return false;
            }
            int size = webSocketSessions.size();
            ArrayList<WebSocketSession> webSocketSessionsList = new ArrayList<>(webSocketSessions);
            webSocketSessionsList.sort((o1, o2) -> StringUtils.compare(o1.getId(), o2.getId()));
            int choose = RandomUtils.nextInt(0, size);
            return sendMessage(webSocketSessionsList.get(choose), message);
        }
        String connectionUuid = message.getConnectionUuid();
        if (OpsTypeEnum.COMMAND.getKey().equals(opsType)) {
            WebSocketSession webSocketSession = connectionUuidSessionMap.get(connectionUuid);
            if (webSocketSession == null) {
                LogUtil.error("??????connectionUuid??????session??????????????????????????????");
                return false;
            }
            return sendMessage(webSocketSession, message);
        }
        if (OpsTypeEnum.DISCONNECT.getKey().equals(opsType)) {
            WebSocketSession webSocketSession = connectionUuidSessionMap.get(connectionUuid);
            if (webSocketSession == null) {
                LogUtil.error("??????connectionUuid??????session??????????????????????????????");
                return false;
            }
            return sendMessage(webSocketSession, message);
        }
        LogUtil.error("???????????????????????????????????????-{}", message);
        return false;
    }

    public boolean sendMessage(WebSocketSession webSocketSession, WebsocketMessage message) {
        return doSend(webSocketSession, JsonUtil.toString(message));
    }

    /**
     * ?????????????????????
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        try {
            log.error("websocket???????????????", exception);
            String sessionId = session.getId();
            sessionMap.remove(sessionId);
        } catch (Exception e) {
            log.error("deal handleTransportError failed!", e);
        }
    }


    /**
     * ?????????????????????
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            String sessionId = session.getId();
            sessionMap.remove(sessionId);
            Set<Map.Entry<String, WebSocketSession>> entries = connectionUuidSessionBrowserMap.entrySet();
            for (Map.Entry<String, WebSocketSession> entry : entries) {
                if (sessionId.equals(entry.getValue().getId())) {
                    connectionUuidSessionBrowserMap.remove(entry.getKey());
                }
            }
        } catch (Exception e) {
            log.error("deal afterConnectionClosed failed!", e);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * ??????????????????
     */
    private boolean doSend(WebSocketSession session, Object message) {
        if (session.isOpen()) {
            try {
                synchronized (session) {
                    Class clazz = message.getClass();
                    if (clazz.isAssignableFrom(byte[].class)) {
                        BinaryMessage binaryMessage = new BinaryMessage((byte[]) message);
                        log.info("websocket??????byte??????-???{}???");
                        session.sendMessage(binaryMessage);
                        return true;
                    } else if (clazz == String.class) {
                        session.sendMessage(new TextMessage(((String) message).getBytes()));
                        return true;
                    } else {
                        log.warn("????????????websocket?????????????????????class-???{}???", clazz.getName());
                        return false;
                    }
                }
            } catch (Exception e) {
                throw new ServiceException(e, "??????websocket???????????????");
            }
        } else {
            log.error("??????????????????websocket??????????????????sessionId-???{}???", session.getId());
            return false;
        }
    }
}
