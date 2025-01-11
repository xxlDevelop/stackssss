package org.yx.hoststack.center.ws.common;

public class NodeWithLevel {
    Node node;
    int level;

    public NodeWithLevel(Node node, int level) {
        this.node = node;
        this.level = level;
    }

    public Node getNode() {
        return node;
    }

    public int getLevel() {
        return level;
    }
}
