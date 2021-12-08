package com.parrer.deskspaceserver.log;

import lombok.Data;

import java.util.List;

@Data
public class ConnectionInfoNotice {
    private String host;
    private List<ConnectInfo> connectInfoList;
}
