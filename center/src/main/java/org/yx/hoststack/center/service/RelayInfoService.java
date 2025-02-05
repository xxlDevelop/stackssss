package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.RelayInfo;

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

}