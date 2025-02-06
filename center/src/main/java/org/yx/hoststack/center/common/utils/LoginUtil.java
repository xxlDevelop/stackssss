package org.yx.hoststack.center.common.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;
import org.yx.hoststack.center.common.exception.UnauthorizedException;
import org.yx.hoststack.center.common.req.XUserDTO;

import static org.yx.lib.utils.util.WebUtil.getRequest;

public class LoginUtil extends WebUtils {

    private LoginUtil() {
    }


    public static String getXUserStr() {
        return getRequest().getHeader("x-user");
    }

    public static XUserDTO getXUser() {
        String user = getRequest().getHeader("x-user");
        if (user == null) {
            throw new UnauthorizedException();
        }
        return JSONUtil.toBean(Base64.decodeStr(user), XUserDTO.class);
    }

    public static XUserDTO getCurrentUser() {
        return getXUser();
    }

    public static Long getTenantId() {
        XUserDTO currentUser = getXUser();
        if (currentUser.getTid() == null) {
            throw new UnauthorizedException();
        }
        return currentUser.getTid();
    }

    public static String getTenantType() {
        XUserDTO currentUser = getXUser();
        String tType = currentUser.getTenantType();
        if (ObjectUtils.isEmpty(tType)) {
            throw new UnauthorizedException();
        } else {
            return tType;
        }

    }

    public static String getTimeZone() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(requestAttributes)) {
            return "UTC";
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (request.getAttribute("timeZone") == null) {
            return "UTC";
        }
        return request.getAttribute("timeZone").toString();
    }

    public static String getUserName() {

//        XUserDTO currentUser = getXUser();
        return getRequest().getAttribute("uname").toString();
    }

}
