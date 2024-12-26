package org.yx.hoststack.edge.server.ws.session;

import lombok.Getter;

@Getter
public enum SessionType {
    Host("host"),
    Container("container")
    ;

    private final String value;


    SessionType(String value) {
        this.value = value;
    }

    public static SessionType from(String value) {
        if (value.equalsIgnoreCase("host")) {
            return Host;
        } else {
            return Container;
        }
    }
}
