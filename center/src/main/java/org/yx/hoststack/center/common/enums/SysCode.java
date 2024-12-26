package org.yx.hoststack.center.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysCode {
    x00000000("Success", 0),
    x00000400("Incorrect input, please confirm", 143400),
    x00000401("Token timeout", 143401),
    x00000403("Unauthorized", 143403),
    x00000404("Not found", 143404),
    x00000405("Action not allow", 143405),
    x00000406("Data is exists", 143406),
    x00000407("Invalid IP: region not found", 143407),
    x00000408("Server Detail not found", 143408),
    x00000409("Tenant Info not found", 143409),
    x00000410("Region Info not found", 143410),
    x00000411("Host Init failed ", 143411),
    x00000412("Heartbeat report Information error", 143412),


    x00000500("Internal server error", 143500),


    x00000010("Request too fast", 143010),
    ;

    private final String msg;
    private final int value;
}