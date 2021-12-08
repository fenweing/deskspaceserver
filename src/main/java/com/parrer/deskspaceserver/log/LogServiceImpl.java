package com.parrer.deskspaceserver.log;

import com.parrer.deskspaceserver.constant.OpsTypeEnum;
import com.parrer.deskspaceserver.websocket.MyWebSocketHandler;
import com.parrer.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {
    @Autowired
    private MyWebSocketHandler socketHandler;
    @Autowired
    private WebsocketResponseWaitContext waitContext;

    @Override
    public String connect(WebsocketMessage websocketMessage) {
        websocketMessage.setOpsType(OpsTypeEnum.CONNECT.getKey());
        Boolean send = socketHandler.send(websocketMessage);
        if (!send) {
            throw new ServiceException("连接出错！");
        }
        WebsocketResponse websocketResponse = waitContext.getConnectInfo(websocketMessage.getConnectInfo());
        if (websocketResponse == null || StringUtils.isBlank(websocketResponse.getConnectionUuid())) {
            throw new ServiceException("获取连接uuid为空!-{}", websocketMessage);
        }
        return websocketResponse.getConnectionUuid();
    }

    @Override
    public WebsocketResponse command(WebsocketMessage command) {
        command.setOpsType(OpsTypeEnum.COMMAND.getKey());
        socketHandler.send(command);
        List<WebsocketResponse> websocketResponseList = waitContext.getCommandResult(command.getRequestNum());
        if (CollectionUtils.isEmpty(websocketResponseList)) {
            return null;
        }
        WebsocketResponse firstResponse = websocketResponseList.get(0);
        if (websocketResponseList.size() == 1) {
            return firstResponse;
        }
        String data = firstResponse.getData();
        data = data == null ? StringUtils.EMPTY : data;
        StringBuilder stringBuilder = new StringBuilder(data).append("\r\n");
        for (int i = 1; i < websocketResponseList.size(); i++) {
            stringBuilder.append(websocketResponseList.get(i).getData()).append("\r\n");
        }
        firstResponse.setData(stringBuilder.toString());
        return firstResponse;
    }

    @Override
    public void disconnect(WebsocketMessage websocketMessage) {
        websocketMessage.setOpsType(OpsTypeEnum.DISCONNECT.getKey());
        socketHandler.send(websocketMessage);
    }
}
