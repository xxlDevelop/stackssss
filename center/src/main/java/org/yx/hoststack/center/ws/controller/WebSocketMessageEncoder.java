package org.yx.hoststack.center.ws.controller;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import org.apache.commons.codec.binary.Base64;
import org.yx.hoststack.protocol.ws.agent.req.HostInitializeReq;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper.Body;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper.CommonMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper.Header;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WebSocketMessageEncoder {

//    public static void main(String[] args) throws Exception {
//        // 创建 Header
//        Header header = Header.newBuilder()
//                .setLinkSide(1)
//                .setMethId(10000)
//                .setIdcSid(UUID.randomUUID().toString())
//                .build();
//
//        E2CMessage.E2C_EdgeRegisterReq edgeRegisterReq = E2CMessage.E2C_EdgeRegisterReq.newBuilder().setServiceIp("192.168.33.248").build();
//
//        // 创建 Body
//        Body body = Body.newBuilder()
//                .setCode(0)
//                .setPayload(ByteString.copyFrom(edgeRegisterReq.toByteArray()))
//                .build();
//
//
//        // 创建 CommonMessage
//        CommonMessage commonMessage = CommonMessage.newBuilder()
//                .setHeader(header)
//                .setBody(body)
//                .build();
//
//        // 序列化为字节数组
//        byte[] messageBytes = commonMessage.toByteArray();
//
//        // 转换为 Base64 编码
//        String base64Encoded = Base64.encodeBase64String(messageBytes);
//
//        // 输出 Base64 编码
//        System.out.println("Base64 Encoded Message: " + base64Encoded);
//    }


    /**
     *agentStartTs: 1234567890
     * agentType: "host"
     * resourcePool: "idc"
     * osStartTs: 1234567890
     * devSn: "device12345"
     * osType: "linux"
     * osVersion: "Ubuntu 20.04 LTS"
     * agentVersion: "1.0.0"
     * osMem: 16384
     * localIp: "192.168.1.1"
     * disk: "10000,SSD | 20000,HDD"
     * gpuList {
     *   gpuType: "NVIDIA GeForce RTX 3080"
     *   gpuManufacturer: "NVIDIA"
     *   gpuMem: 10
     *   gpuBusType: "PCIe"
     *   gpuDeviceId: "GPU12345"
     *   gpuBusId: "0000:17:00.0"
     * }
     * netCardList {
     *   netCardName: "eth0"
     *   netCardType: "Ethernet"
     *   netCardLinkSpeed: 1000.0
     * }
     * runtimeEnv: "bm"
     * hostId: "123456789"
     * detailedId: "cpuId:123,biosId:456"
     * xToken: "IDC"
     *
     *
     *
     * header {
     *   linkSide: 1
     *   traceId: "7E6D2238-57D1-7CDA-36DE-D2B427D7323A"
     *   zone: "China"
     *   region: "North-BeiJing"
     *   idcSid: "290fe739e9df20ee3c2f0b8e3262f08d"
     *   methId: 10200
     * }
     *
     * @author yijian
     * @date 2025/1/17 17:20
     */


    public static void main(String[] args) throws Exception {
        List<E2CMessage.GpuInfo> gpuInfoList = Lists.newArrayList();
        gpuInfoList.add(E2CMessage.GpuInfo.newBuilder()
                .setGpuType("NVIDIA GeForce RTX 4090")
                .setGpuManufacturer("NVIDIA")
                .setGpuMem(24)  // 24GB 显存
                .setGpuBusType("PCIe Gen 4")
                .setGpuDeviceId("GPU-0987ABCD")
                .setGpuBusId("0000:65:00.0")
                .build());

        List<E2CMessage.NetCardInfo> netCardInfoList = Lists.newArrayList();
        netCardInfoList.add(E2CMessage.NetCardInfo.newBuilder()
                .setNetCardName("eth0")
                .setNetCardType("Ethernet")
                .setNetCardLinkSpeed(10000.0f)  // 10Gbps
                .build());

        netCardInfoList.add(E2CMessage.NetCardInfo.newBuilder()
                .setNetCardName("eth1")
                .setNetCardType("Ethernet")
                .setNetCardLinkSpeed(1000.0f)  // 1Gbps
                .build());

        E2CMessage.E2C_HostInitializeReq hostInitialize = E2CMessage.E2C_HostInitializeReq.newBuilder()
                .setAgentStartTs(new Date().getTime())  // 当前时间戳
                .setAgentType("container")
                .setResourcePool("idc")  // IDC数据中心
                .setRuntimeEnv("bm")  // Bare Metal 环境
                .setOsStartTs(new Date().getTime())  // 当前时间戳
                .setDevSn("dev-sn-12345")
                .setOsType("linux")
                .setOsVersion("Ubuntu 22.04 LTS")
                .setAgentVersion("2.1.3")  // 更高版本的 agent
                .setOsMem(65536)  // 64GB 内存
                .setLocalIp("10.0.1.10")
                .setDisk("50000,SSD | 200000,HDD")  // 50TB SSD | 200TB HDD
//                .setHostId("host-56789")
                .setDetailedId("detailed-98765")
                .setProxy(1)
                .addAllGpuList(gpuInfoList)
                .addAllNetCardList(netCardInfoList)
                .setXToken("x-token-56789")
                .build();



        // 创建 Header
        Header header = Header.newBuilder()
                .setLinkSide(1)
                .setMethId(ProtoMethodId.HostInitialize.getValue())
                .setIdcSid("290fe739e9df20ee3c2f0b8e3262f08d")
                .setZone("China-South")
                .setRegion("South-HangZhou")
                .build();

        // 创建 Body
        Body body = Body.newBuilder()
                .setCode(0)
                .setPayload(ByteString.copyFrom(hostInitialize.toByteArray()))
                .build();


        // 创建 CommonMessage
        CommonMessage commonMessage = CommonMessage.newBuilder()
                .setHeader(header)
                .setBody(body)
                .build();

        byte[] messageBytes = commonMessage.toByteArray();

        // 转换为 Base64 编码
        String base64Encoded = Base64.encodeBase64String(messageBytes);

        // 输出 Base64 编码
        System.out.println("Base64 Encoded Message: " + base64Encoded);
    }
}
