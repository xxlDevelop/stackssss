package org.yx.hoststack.center.ws.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import org.apache.commons.codec.binary.Base64;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper.CommonMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper.Header;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper.Body;
import org.yx.hoststack.protocol.ws.server.E2CMessage;

public class WebSocketMessageEncoder {

    public static void main(String[] args) throws Exception {
        // 创建 Header
        Header header = Header.newBuilder()
                .setLinkSide(1)
                .setMethId(10000)
                .build();

        E2CMessage.E2C_EdgeRegisterReq edgeRegisterReq = E2CMessage.E2C_EdgeRegisterReq.newBuilder().setServiceIp("192.168.33.218").build();

        // 创建 Body
        Body body = Body.newBuilder()
                .setCode(0)
                .setPayload(ByteString.copyFrom(edgeRegisterReq.toByteArray()))
                .build();


        // 创建 CommonMessage
        CommonMessage commonMessage = CommonMessage.newBuilder()
                .setHeader(header)
                .setBody(body)
                .build();

        // 序列化为字节数组
        byte[] messageBytes = commonMessage.toByteArray();

        // 转换为 Base64 编码
        String base64Encoded = Base64.encodeBase64String(messageBytes);

        // 输出 Base64 编码
        System.out.println("Base64 Encoded Message: " + base64Encoded);
    }
}
