package com.parrer.deskspaceserver.controller;

import com.parrer.deskspaceserver.log.LogService;
import com.parrer.deskspaceserver.log.WebsocketMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class LogController {
    @Autowired
    private LogService logService;

    @PostMapping("/connect")
    public Object connect(@RequestBody @Validated(value = {ConnectionParam.class}) WebsocketMessage websocketMessage) {
        return logService.connect(websocketMessage);
    }

    @PostMapping("/command")
    public Object command(@RequestBody @Validated(value = {CommandParam.class}) WebsocketMessage websocketMessage) {
        return logService.command(websocketMessage);
    }

    @PostMapping("/disconnect")
    public Object disconnect(@RequestBody @Validated(value = {DisconnectParam.class}) WebsocketMessage websocketMessage) {
        logService.disconnect(websocketMessage);
        return StringUtils.EMPTY;
    }


}
