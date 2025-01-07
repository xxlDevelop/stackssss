package org.yx.hoststack.center.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.req.relay.RelayUpdateReq;
import org.yx.hoststack.center.entity.RelayInfo;
import org.yx.hoststack.center.mapper.RelayInfoMapper;
import org.yx.hoststack.center.service.RelayInfoService;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RelayInfoServiceImpl extends ServiceImpl<RelayInfoMapper, RelayInfo> implements RelayInfoService {


    private final RelayInfoMapper relayInfoMapper;

    @Override
    public Page<RelayInfo> findPage(RelayInfo params) {
        Page<RelayInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<RelayInfo> query = Wrappers.lambdaQuery(RelayInfo.class);
        return relayInfoMapper.selectPage(page, query);
    }

    @Override
    public List<RelayInfo> findList(RelayInfo params) {
        LambdaQueryWrapper<RelayInfo> query = Wrappers.lambdaQuery(RelayInfo.class);
        return relayInfoMapper.selectList(query);
    }

    @Override
    public RelayInfo findById(Long id) {
        return relayInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(RelayInfo relayInfo) {
        return save(relayInfo);
    }

    @Override
    public boolean update(RelayInfo relayInfo) {
        return updateById(relayInfo);
    }

    @Override
    public int delete(Long id) {
        return relayInfoMapper.deleteById(id);
    }

    @Override
    public R<?> updateRelay(RelayUpdateReq relayUpdateReq) {
        try {
            // 创建更新条件
            LambdaUpdateWrapper<RelayInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(RelayInfo::getRelay, relayUpdateReq.getRelay())
                    .set(RelayInfo::getRelayIp, relayUpdateReq.getRelayIp())
                    .set(RelayInfo::getNetHttpsSvc, relayUpdateReq.getNetHttpsSvc())
                    .set(RelayInfo::getNetWssSvc, relayUpdateReq.getNetWssSvc())
                    .set(RelayInfo::getLocation, relayUpdateReq.getLocation());
            int rowsUpdated = relayInfoMapper.update(updateWrapper);

            if (rowsUpdated > 0) {
                return R.ok();
            } else {
                return R.failed("No relay found with the specified identifier");
            }
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.RELAY_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.RELAY_EVENT_ACTION_RELAY_UPDATE)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p("relayUpdateReq", JSON.toJSONString(relayUpdateReq))
                    .e(e);
            return R.failed("updateRelay error");
        }
    }

}