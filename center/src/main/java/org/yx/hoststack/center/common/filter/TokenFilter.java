package org.yx.hoststack.center.common.filter;

import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.constant.RequestAttributeConstants;
import org.yx.hoststack.center.common.req.XUserDTO;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.yx.hoststack.center.common.enums.SysCode.x00000401;

/**
 * @Description : Request Filter
 * @Author : Lee666
 * @Date : 2023/10/10
 * @Version : 1.0
 */
@WebFilter(filterName = "TokenFilter",
        /*Wildcard (*) indicates interception of all web resources*/
        urlPatterns = "/*",
        initParams = {
                /* Here you can put some initialization parameters */
                @WebInitParam(name = "charset", value = "utf-8")
        })
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
class TokenFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.TOKEN_FILTER_EVENT)
                .p(LogFieldConstants.ACTION, CenterEvent.Action.TOKEN_FILTER_EVENT_ACTION_INIT).i();
        Filter.super.init(filterConfig);
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        String xUser = httpRequest.getHeader(CommonConstants.X_USER);
        if (StringUtil.isBlank(xUser)) {
            KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.TOKEN_FILTER_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.TOKEN_FILTER_EVENT_ACTION_DO_FILTER)
                    .p(LogFieldConstants.ERR_MSG, "x-user is null")
                    .p(LogFieldConstants.ReqUrl, ((HttpServletRequest) servletRequest).getRequestURI())
                    .i();
            write403(servletResponse);
            return;
        }
        MDC.put(CommonConstants.X_USER, xUser);
        String xUserStr = new String(Base64.getDecoder().decode(xUser.getBytes(StandardCharsets.UTF_8)));
        XUserDTO xUserDTO = JSONUtil.toBean(xUserStr, XUserDTO.class);

        if (!StringUtils.hasLength(xUserDTO.getUid()) && !StringUtils.hasLength(xUserDTO.getAk())) {
            KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.TOKEN_FILTER_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.TOKEN_FILTER_EVENT_ACTION_DO_FILTER)
                    .p(LogFieldConstants.ERR_MSG, "The UID and AK of x-user are null")
                    .p(LogFieldConstants.ReqUrl, ((HttpServletRequest) servletRequest).getRequestURI())
                    .i();
            write403(servletResponse);
            return;
        }
        if (!StringUtils.hasLength(xUserDTO.getRoleId()) && !StringUtils.hasLength(xUserDTO.getTenantType()) && (xUserDTO.getTid() == null || xUserDTO.getTid() == 0L)) {
            KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.TOKEN_FILTER_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.TOKEN_FILTER_EVENT_ACTION_DO_FILTER)
                    .p(LogFieldConstants.ERR_MSG, "The RoleId and Tid and TenantType of x-user are null")
                    .p(LogFieldConstants.ReqUrl, ((HttpServletRequest) servletRequest).getRequestURI())
                    .i();
            write403(servletResponse);
            return;
        }
        String token = httpRequest.getHeader(RequestAttributeConstants.TOKEN);
        servletRequest.setAttribute(RequestAttributeConstants.TOKEN, token);
        servletRequest.setAttribute(RequestAttributeConstants.TENANT_ID, xUserDTO.getTid());
        if (StringUtils.hasLength(xUserDTO.getUid())) {
            servletRequest.setAttribute(RequestAttributeConstants.UID, Long.valueOf(xUserDTO.getUid()));
        }
        servletRequest.setAttribute(RequestAttributeConstants.ROLE_ID, Integer.valueOf(xUserDTO.getRoleId()));
        servletRequest.setAttribute(RequestAttributeConstants.TENANT_TYPE, xUserDTO.getTenantType());
        servletRequest.setAttribute(RequestAttributeConstants.AK, xUserDTO.getAk());
        servletRequest.setAttribute(RequestAttributeConstants.X_USER_STR, xUserStr);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }


    private void write403(ServletResponse servletResponse) throws IOException {
        servletResponse.setContentType(ContentType.JSON.getValue());
        PrintWriter printWriter = servletResponse.getWriter();
        printWriter.write(JSONUtil.toJsonStr(R.builder().code(x00000401.getValue()).msg(x00000401.getMsg()).traceId(MDC.get("traceId")).build()));
        printWriter.flush();
    }
}
