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
    x00000408("Session Info not found", 143408),

    x00000409("Tenant Info not found", 143409),
    x00000410("Region Info not found", 143410),
    x00000411("Host Init failed ", 143411),
    x00000412("Heartbeat report Information error", 143412),
    x00000413("Save Or Update IdcInfo error", 143413),
    x00000414("Update IdcInfo error", 143414),
    x00000415("Save Or Update RelayInfo error", 143415),
    x00000416("Update RelayInfo error", 143416),
    x00000417("Save Or Update ServiceDetail error", 143417),
    x00000418("Update ServiceDetail error", 143418),
    x00000419("idc exit successful.", 143419),


    x00000600("Agent Init Error, Agent Need to exist.", 143600),
    x00000601("IDC/Relay Unregistered", 143601),
    x00000602("Host exit successful.", 143602),


    // IDC Network Configuration Error Codes
    x00030001("Duplicate internal network address found", 12230001),
    x00030002("Duplicate external network address found", 12230002),
    x00030003("Invalid internal network address format", 12230003),
    x00030004("Invalid external network address format", 12230004),
    x00030005("Network address configurations already exist", 12230005),
    x00030006("Network save failed", 12230006),

    x00000100("No available net IP", 143100),
    x00000101("Insufficient available net IP addresses", 143101),


    x00000420("Image data not found", 143420),
    x00000430("Host data not found", 143430),
    x00000440("Host session data not found", 143440),
    x00000450("Container create region does not match the selected host region", 143450),


    x00000500("Registration encountered issues ", 143500),
    x00000501("Unknown column", 143501),
    x00000502("The corresponding channel was not found", 143502),
    x00000503("SendMsgError", 143503),
    x00000504("Channel is inactive", 143504),
    x00000505("Init SysModule、CpuInfo、GpuInfo Error", 143505),
    x00000506("Post remoteServer Fail", 143506),

    x00000507("Failed to create image", 143507),
    x00000508("Image with MD5 %s already exists", 143508),
    x00000509("Update imageInfo fail", 143509),
    x00000510("Update imageInfo status fail", 143510),
    x00000511("Failed to create volume", 143511),
    x00000512("Failed to delete volume", 143512),
    x00000513("Failed to mount volume", 143513),
    x00000514("Create image cmd job result null", 143514),
    x00000515("Volume mount info not found", 143515),
    x00000516("Create volume cmd job result null", 143516),
    x00000517("Send create job volume result null", 143517),
    x00000518("Send image job volume result null", 143518),
    x00000519("Send delete job volume result null", 143519),
    x00000520("Delete volume cmd job result null", 143520),
    x00000521("Send mount job volume result null", 143521),
    x00000522("Mount volume cmd job result null", 143522),
    x00000523("Failed to unmount volume", 143523),
    x00000524("Send unmount job volume result null", 143524),
    x00000525("Unmount volume cmd job result null", 143525),
    x00000526("Failed to query volume list", 143526),
    x00000527("save image fail", 143527),
    x00000528("Failed to generate upload url", 143528),
    x00000529("Failed to generate download url", 143529),
    x00000530("region and idc cannot both be empty", 143530),
    x00000531("Bucket %s already exists", 143531),
    x00000532("bucketCreate is error", 143532),
    x00000533("bucketDelete is error", 143533),
    x00000534("Bucket %s not found", 143534),
    x00000535("storage idc list buckets failed", 143535),
    x00000536("storage file list failed", 143536),


    x00000700("UnknownJobType", 143700),
    x00000701("SendJobFail", 143701),

    x00000010("Request too fast", 143010),
    ;

    private final String msg;
    private final int value;
}