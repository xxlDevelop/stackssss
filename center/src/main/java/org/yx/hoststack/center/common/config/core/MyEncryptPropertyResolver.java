package org.yx.hoststack.center.common.config.core;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.exception.beans.MyEncryptPropertyResolverException;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import util.AESUtil;

/**
 * @Description : Encryption and decryption configuration - decryption program
 * @Author : Lee666
 * @Date : 2024/5/30
 * @Version : 1.0
 */
public class MyEncryptPropertyResolver implements EncryptablePropertyResolver {

    private static final String ENC_PREFIX = "ENC#";

    @Override
    public String resolvePropertyValue(String value) {
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.ENCRYPTABLE_PROPERTY_RESOLVER_EVENT);

        if (value != null && value.startsWith(ENC_PREFIX)) {
            String str = value.substring(0, value.indexOf("#"));
            String result = value.substring(str.length() + 1);
            try {
                String decrypt = AESUtil.decrypt(result);
                kvLogger.p(LogFieldConstants.Success, true).i();
                return decrypt;
            } catch (Exception e) {
                kvLogger.p(LogFieldConstants.ERR_MSG, "Decrypt failed=[" + value + "]").p(LogFieldConstants.Success, false).e(e);
                throw new MyEncryptPropertyResolverException(e);
            }
        }

        kvLogger.p(LogFieldConstants.Success, true).i();
        return value;
    }
}