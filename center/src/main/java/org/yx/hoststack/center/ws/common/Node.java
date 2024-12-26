package org.yx.hoststack.center.ws.common;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public class Node {
    String serviceId;
    /**
     *
     * 类型：Center, Relay, IDC, Host
     */
    String type;
    String hostId;
    Channel channel;
    Node parent;
    List<Node> children = new ArrayList<>();

    public Node(String serviceId, String type, Channel channel) {
        this.serviceId = serviceId;
        this.type = type;
        this.channel = channel;
    }

    /**
     *
     * 向节点添加子节点，并设置子节点的父节点引用
     */
    public void addChild(Node childNode) {
        childNode.parent = this;
        children.add(childNode);
    }

    /**
     *获取该节点的父节点
     */
    public Node getParent() {
        return parent;
    }

    /**
     *
     *打印当前节点及其子节点的信息
     */
    public void printNodeInfo(int level) {
        String indentation = " ".repeat(level * 2);
        System.out.println(indentation + type + " (" + serviceId + ")");
        
        for (Node child : children) {
            child.printNodeInfo(level + 1);
        }
    }
}
