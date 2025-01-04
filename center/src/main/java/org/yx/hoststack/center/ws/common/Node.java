package org.yx.hoststack.center.ws.common;

import io.netty.channel.Channel;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    String serviceId;
    RegisterNodeEnum type;
    String hostId;
    Channel channel;
    Node parent;
    List<Node> children = new ArrayList<>();

    // 用于全局快速查找的哈希表
    public static final ConcurrentHashMap<String, Node> NODE_MAP = new ConcurrentHashMap<>();

    public Node(String serviceId, RegisterNodeEnum type, Channel channel) {
        this.serviceId = serviceId;
        this.type = type;
        this.channel = channel;
        // 添加节点到哈希表中
        NODE_MAP.put(serviceId, this);
    }

    public void addChild(Node childNode) {
        childNode.parent = this;
        children.add(childNode);

    }

    public Node addOrUpdateNode(String serviceId, RegisterNodeEnum registerNodeEnum, Channel channel, Node... parent) {
        // 查找是否已经存在该节点
        Node existNode = findNodeByServiceId(serviceId);

        // 如果节点已存在，更新其 channel 并处理父节点关系
        if (existNode != null) {
            // 更新 channel
            existNode.channel = channel;

            // 如果传入了父节点且父节点与当前父节点不同，则需要重新调整父子关系
            if (parent != null && parent.length > 0) {
                Node parentNode = parent[0];

                // 检查父节点是否变化，如果变化，则需要从原父节点删除该节点
                if (existNode.parent != parentNode) {
                    // 从原父节点中移除该节点
                    if (existNode.parent != null) {
                        existNode.parent.children.remove(existNode);
                    }

                    // 将该节点添加到新的父节点
                    parentNode.addChild(existNode);
                }
            }
            return existNode; // 直接返回已更新的节点
        }

        // 如果节点不存在，创建新的节点
        Node newNode = new Node(serviceId, registerNodeEnum, channel);

        // 处理父节点关系
        addParentIfNeeded(newNode, parent);

        // 将新节点加入到 NODE_MAP
        NODE_MAP.put(newNode.serviceId, newNode);

        return newNode; // 返回新创建的节点
    }

    // 辅助方法：处理父节点添加逻辑
    private void addParentIfNeeded(Node node, Node... parent) {
        if (parent != null && parent.length > 0) {
            Node parentNode = parent[0];  // 只处理第一个父节点
            if (!parentNode.children.contains(node)) {
                parentNode.addChild(node); // 添加子节点
            }
        } else {
            // 如果没有传入父节点，且当前节点不存在，作为当前节点的子节点添加
            if (!this.children.contains(node)) { // 当前节点没有子节点
                this.addChild(node);
            }
        }
    }




    /**
     * 使用哈希表根据 serviceId 查找节点
     */
    public static Node findNodeByServiceId(String serviceId) {
        return NODE_MAP.get(serviceId);
    }

    /**
     * 移除节点（包括其所有子节点）
     */
    public void removeNode() {
        // 递归移除所有子节点
        removeNodeRecursively(this);
    }

    public void removeNodeRecursively(Node node) {
        // 使用栈来存储节点
        Deque<Node> stack = new LinkedList<>();
        stack.push(node);

        // 深度优先遍历
        while (!stack.isEmpty()) {
            Node current = stack.pop();
            // 先将子节点入栈
            for (Node child : current.children) {
                stack.push(child);
            }
            // 从哈希表中删除节点
            NODE_MAP.remove(current.serviceId);
        }

        // 从父节点中移除
        if (node.parent != null) {
            node.parent.children.remove(node);
        }
    }

    public void addOrUpdateNode() {
    }
    public void printNodeInfo(int level) {
        String indentation = " ".repeat(level * 2);
        System.out.println(indentation + type + " (" + serviceId + ")");

        for (Node child : children) {
            child.printNodeInfo(level + 1);
        }
    }
}
