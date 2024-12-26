package org.yx.hoststack.center.common.resp.comtainer.wrappers;

import org.yx.hoststack.center.common.resp.comtainer.ContainerSimpleVO;
import org.yx.hoststack.center.common.resp.support.BaseEntityWrapper;
import org.yx.hoststack.center.entity.Container;

import java.util.Objects;

/**
 * @author Lee666
 */
public class ContainerSimpleVOWrapper extends BaseEntityWrapper<Container, ContainerSimpleVO> {

    public static ContainerSimpleVOWrapper build() {
        return new ContainerSimpleVOWrapper();
    }

    @Override
    public ContainerSimpleVO entityVO(Container entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        ContainerSimpleVO vo = new ContainerSimpleVO();
//        vo.setProfile(entity.getProfile());
//        vo.setArch(entity.getArch());
//        vo.setContainerType(entity.getContainerType());
//        vo.setBizType(entity.getBizType());
        vo.setOsType(entity.getOsType());

        return vo;
    }
}
