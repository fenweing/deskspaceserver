package com.parrer.deskspaceserver.log;

import com.parrer.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class WebsocketResponseWaitContext {
    private HashMap<ConnectInfo, LinkedBlockingQueue<WebsocketResponse>> connectInfoQueueMap = new HashMap<>();
    private HashMap<String, LinkedBlockingQueue<WebsocketResponse>> requetNumQueueMap = new HashMap<>();

    public void putConnectInfo(WebsocketResponse websocketResponse) {
        if (websocketResponse == null) {
            LogUtil.error("空返回消息！");
            return;
        }
        ConnectInfo connectInfo = websocketResponse.getConnectInfo();
        connectInfoQueueMap.computeIfAbsent(connectInfo, cn -> new LinkedBlockingQueue<>());
        LinkedBlockingQueue<WebsocketResponse> queue = connectInfoQueueMap.get(connectInfo);
        queue.clear();
        queue.offer(websocketResponse);
    }

    public WebsocketResponse getConnectInfo(ConnectInfo connectInfo) {
        if (connectInfo == null) {
            LogUtil.error("连接信息为空！");
            return null;
        }
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            LogUtil.error(e, "等待结果中出错！");
            return null;
        }
        LinkedBlockingQueue<WebsocketResponse> queue = connectInfoQueueMap.get(connectInfo);
        if (queue == null) {
            LogUtil.error("获取连接信息队列为空！");
            return null;
        }
        WebsocketResponse take = queue.poll();
        return take;
    }

    public void putCommandResult(WebsocketResponse websocketResponse) {
        if (websocketResponse == null) {
            LogUtil.error("空返回消息！");
            return;
        }
        String requestNum = websocketResponse.getRequestNum();
        requetNumQueueMap.computeIfAbsent(requestNum, reqNum -> new LinkedBlockingQueue<>());
        requetNumQueueMap.get(requestNum).offer(websocketResponse);
    }

    public List<WebsocketResponse> getCommandResult(String requestNum) {
        if (StringUtils.isBlank(requestNum)) {
            LogUtil.error("请求序号为空！");
            return null;
        }
        LinkedBlockingQueue<WebsocketResponse> queue = requetNumQueueMap.get(requestNum);
        if (queue == null) {
            LogUtil.error("获取连接信息队列为空！");
            return null;
        }
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            LogUtil.error(e, "等待结果中出错！");
            return null;
        }
        ArrayList<WebsocketResponse> websocketResponses = new ArrayList<>();
        while (queue.size() > 0) {
            WebsocketResponse take = queue.poll();
            websocketResponses.add(take);
        }
        return websocketResponses;
    }
}
