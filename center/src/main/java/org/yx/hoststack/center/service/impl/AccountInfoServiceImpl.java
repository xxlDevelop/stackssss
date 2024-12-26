package org.yx.hoststack.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.entity.AccountInfo;
import org.yx.hoststack.center.mapper.AccountInfoMapper;
import org.yx.hoststack.center.service.AccountInfoService;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountInfoServiceImpl extends ServiceImpl<AccountInfoMapper, AccountInfo> implements AccountInfoService {

    private final AccountInfoMapper accountInfoMapper;

    @Override
    public Page<AccountInfo> findPage(AccountInfo params) {
        Page<AccountInfo> page = new Page<>(1, 10);//TODO 自行处理
        LambdaQueryWrapper<AccountInfo> query = Wrappers.lambdaQuery(AccountInfo.class);
        return accountInfoMapper.selectPage(page, query);
    }

    @Override
    public List<AccountInfo> findList(AccountInfo params) {
        LambdaQueryWrapper<AccountInfo> query = Wrappers.lambdaQuery(AccountInfo.class);
        return accountInfoMapper.selectList(query);
    }

    @Override
    public AccountInfo findById(Long id) {
        return accountInfoMapper.selectById(id);
    }

    @Override
    public boolean insert(AccountInfo accountInfo) {
        return save(accountInfo);
    }

    @Override
    public boolean update(AccountInfo accountInfo) {
        return updateById(accountInfo);
    }

    @Override
    public int delete(Long id) {
        return accountInfoMapper.deleteById(id);
    }

}