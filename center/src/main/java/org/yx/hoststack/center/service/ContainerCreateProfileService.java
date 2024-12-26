package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.ContainerCreateProfile;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface ContainerCreateProfileService extends IService<ContainerCreateProfile> {

    Page<ContainerCreateProfile> findPage(ContainerCreateProfile params);

    List<ContainerCreateProfile> findList(ContainerCreateProfile params);

    ContainerCreateProfile findById(Long id);

    boolean insert(ContainerCreateProfile containerCreateProfile);

    boolean update(ContainerCreateProfile containerCreateProfile);

    int delete(Long id);

}