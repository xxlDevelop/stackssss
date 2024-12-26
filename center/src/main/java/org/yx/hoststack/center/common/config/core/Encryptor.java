package org.yx.hoststack.center.common.config.core;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
/**
 * @Description : Encryption and Decryption Library - Decrypt Registrar
 * @Author : Lee666
 * @Date : 2024/5/30
 * @Version : 1.0
 */
@Component
public class Encryptor {
    @Bean(name="encryptablePropertyResolver")
    EncryptablePropertyResolver encryptablePropertyResolver(){
        return new MyEncryptPropertyResolver();
    }
}