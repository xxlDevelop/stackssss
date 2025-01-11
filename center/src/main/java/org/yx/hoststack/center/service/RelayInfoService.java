package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.req.relay.RelayListReq;
import org.yx.hoststack.center.common.req.relay.RelayUpdateReq;
import org.yx.hoststack.center.common.resp.PageResp;
import org.yx.hoststack.center.common.resp.relay.RelayListResp;
import org.yx.hoststack.center.entity.RelayInfo;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface RelayInfoService extends IService<RelayInfo> {

    Page<RelayInfo> findPage(RelayInfo params);

    List<RelayInfo> findList(RelayInfo params);

    RelayInfo findById(Long id);

    boolean insert(RelayInfo relayInfo);

    boolean update(RelayInfo relayInfo);

    int delete(Long id);

    R<?> updateRelay(RelayUpdateReq relayUpdateReq);

    R<PageResp<RelayListResp>> listRelay(RelayListReq relayListReq);
}