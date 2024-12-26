package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.entity.ContainerNetConfig;
import org.yx.hoststack.center.mapper.ContainerNetConfigMapper;
import org.yx.hoststack.center.service.ContainerNetConfigService;

/**
 * <p>
 * 容器IP配置信息表 服务实现类
 * </p>
 *
 * @author Lee666
 * @since 2024-12-25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContainerNetConfigServiceImpl extends ServiceImpl<ContainerNetConfigMapper, ContainerNetConfig> implements ContainerNetConfigService {

}
