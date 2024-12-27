package org.yx.hoststack.center.common.resp.comtainer.wrappers;

import org.yx.hoststack.center.common.resp.comtainer.ContainerPageDBVO;
import org.yx.hoststack.center.common.resp.comtainer.ContainerSimpleVO;
import org.yx.hoststack.center.common.resp.support.BaseEntityWrapper;

import java.util.Objects;

/**
 * @author Lee666
 */
public class ContainerSimpleVOWrapper extends BaseEntityWrapper<ContainerPageDBVO, ContainerSimpleVO> {

    public static ContainerSimpleVOWrapper build() {
        return new ContainerSimpleVOWrapper();
    }

    @Override
    public ContainerSimpleVO entityVO(ContainerPageDBVO dataVO) {
        if (Objects.isNull(dataVO)) {
            return null;
        }
        ContainerSimpleVO vo = new ContainerSimpleVO();
        vo.setCid(dataVO.getCid());
        vo.setHostId(dataVO.getHostId());
        vo.setImageId(dataVO.getImageId());
        vo.setImageName(dataVO.getImageName());
        vo.setImageVer(dataVO.getImageVer());
        vo.setTenantId(dataVO.getTenantId());
        vo.setZone(dataVO.getZone());
        vo.setRegion(dataVO.getRegion());
        vo.setResourcePool(dataVO.getResourcePool());
        vo.setContainerType(dataVO.getContainerType());
        vo.setBizType(dataVO.getBizType());
        vo.setDevSn(dataVO.getDevSn());
        vo.setOsType(dataVO.getOsType());
        vo.setOsMem(dataVO.getOsMem());
        vo.setRuntimeEnv(dataVO.getRuntimeEnv());
        vo.setLabel(dataVO.getLabel());
        vo.setCreateAt(dataVO.getCreateAt().getTime());
        return vo;
    }
}
