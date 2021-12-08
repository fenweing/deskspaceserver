package com.parrer.deskspaceserver.log;

import com.parrer.deskspaceserver.controller.CommandParam;
import com.parrer.deskspaceserver.controller.ConnectionParam;
import com.parrer.deskspaceserver.controller.DisconnectParam;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WebsocketMessage {
    @NotNull
    private Integer osType;
    //    @NotNull
    private Integer opsType;
    @NotNull(groups = CommandParam.class)
    private String command;
    private String filePath;
    @NotNull(groups = ConnectionParam.class)
    private ConnectInfo connectInfo;
    @NotNull(groups = {CommandParam.class, DisconnectParam.class})
    private String connectionUuid;
    @NotNull(groups = CommandParam.class)
    private String requestNum;
}
