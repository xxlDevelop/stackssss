package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 中继转发节点信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_relay_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelayInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  大区标识
     */
    private String zone;

    /**
     *  分区标识
     */
    private String region;

    /**
     *  中继节点标识
     */
    private String relay;

    /**
     *  中继转发节点公网IP
     */
    private String relayIp;

    /**
     *  公网HTTPS基地址
     */
    private String netHttpsSvc;

    /**
     *  公网WSS基地址
     */
    private String netWssSvc;

    /**
     *  中继节点GPS坐标
     */
    private String location;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelayInfo relayInfo = (RelayInfo) o;
        return Objects.equals(zone, relayInfo.zone) && Objects.equals(region, relayInfo.region) && Objects.equals(relay, relayInfo.relay) && Objects.equals(relayIp, relayInfo.relayIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zone, region, relay, relayIp);
    }

}