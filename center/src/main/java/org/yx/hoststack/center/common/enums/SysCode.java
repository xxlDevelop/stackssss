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

    // IDC Network Configuration Error Codes
    x00030001("Duplicate internal network address found", 12230001),
    x00030002("Duplicate external network address found", 12230002),
    x00030003("Invalid internal network address format", 12230003),
    x00030004("Invalid external network address format", 12230004),
    x00030005("Network address configurations already exist", 12230005),
    x00030006("Network save failed", 12230006),


    x00000420("Image data not found", 143420),
    x00000430("Host data not found", 143430),
    x00000440("Host session data not found", 143440),


    x00000500("Internal server error", 143500),


    x00000010("Request too fast", 143010),
    ;

    private final String msg;
    private final int value;
}