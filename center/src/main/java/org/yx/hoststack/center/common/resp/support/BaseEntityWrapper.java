package org.yx.hoststack.center.common.resp.support;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Collections;
import java.util.List;

/**
 * @author Lee666
 */
public abstract class BaseEntityWrapper<E, V> {

    public abstract V entityVO(E entity);

    public List<V> listVO(List<E> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(this::entityVO).toList();
    }

    public IPage<V> pageVO(IPage<E> pages) {
        List<V> records = this.listVO(pages.getRecords());
        IPage<V> pageVo = new Page<>(pages.getCurrent(), pages.getSize(), pages.getTotal());
        pageVo.setRecords(records);
        return pageVo;
    }
}
