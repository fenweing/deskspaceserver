package com.parrer.deskspaceserver.log;

public interface LogService {
    String connect(WebsocketMessage connectInfo);

    WebsocketResponse command(WebsocketMessage command);

    void disconnect(WebsocketMessage websocketMessage);
}
