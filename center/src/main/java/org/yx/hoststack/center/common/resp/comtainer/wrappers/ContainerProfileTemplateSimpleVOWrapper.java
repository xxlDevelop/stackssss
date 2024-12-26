package org.yx.hoststack.center.common.resp.comtainer.wrappers;

import org.yx.hoststack.center.common.resp.comtainer.ContainerProfileTemplateSimpleVO;
import org.yx.hoststack.center.common.resp.support.BaseEntityWrapper;
import org.yx.hoststack.center.entity.ContainerProfileTemplate;

import java.util.Objects;

/**
 * @author Lee666
 */
public class ContainerProfileTemplateSimpleVOWrapper extends BaseEntityWrapper<ContainerProfileTemplate, ContainerProfileTemplateSimpleVO> {

    public static ContainerProfileTemplateSimpleVOWrapper build() {
        return new ContainerProfileTemplateSimpleVOWrapper();
    }

    @Override
    public ContainerProfileTemplateSimpleVO entityVO(ContainerProfileTemplate entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        ContainerProfileTemplateSimpleVO vo = new ContainerProfileTemplateSimpleVO();
        vo.setProfile(entity.getProfile());
        vo.setArch(entity.getArch());
        vo.setContainerType(entity.getContainerType());
        vo.setBizType(entity.getBizType());
        vo.setOsType(entity.getOsType());

        return vo;
    }
}
