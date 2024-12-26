package org.yx.hoststack.center.common.exception.beans;

import java.io.Serial;

/**
 * @Description : MyEncryptPropertyResolverException
 * @Author : Lee666
 * @Date : 2024/12/23
 * @Version : 1.0
 */
public class MyEncryptPropertyResolverException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 5487024698082626808L;

    public MyEncryptPropertyResolverException(Throwable e){
        super(e);
    }
}
