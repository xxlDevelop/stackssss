package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.entity.AccountInfo;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface AccountInfoService extends IService<AccountInfo> {

    Page<AccountInfo> findPage(AccountInfo params);

    List<AccountInfo> findList(AccountInfo params);

    AccountInfo findById(Long id);

    boolean insert(AccountInfo accountInfo);

    boolean update(AccountInfo accountInfo);

    int delete(Long id);

}