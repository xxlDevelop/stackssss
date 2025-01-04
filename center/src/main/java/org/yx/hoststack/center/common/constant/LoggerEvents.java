package org.yx.hoststack.center.common.constant;

/**
 * @author Lee666
 */
public interface LoggerEvents {
    /**
     * common
     */
    String APPLICATION_EVENT = "ApplicationRunner";
    String TOKEN_FILTER_EVENT = "TokenFilterEvent";
    String ENCRYPTABLE_PROPERTY_RESOLVER_EVENT = "EncryptablePropertyResolverEvent";
    /**
     * request
     */
    String REQUEST_REMOTE_SERVICE_EVENT = "RequestRemoteServiceEvent";

    interface Actions {
        String APPLICATION_EVENT_INIT = "Init";
        String TOKEN_FILTER_EVENT_ACTION_INIT = "TokenFilterInit";
        String TOKEN_FILTER_EVENT_ACTION_DO_FILTER = "TokenFilterDoFilter";

    }

    interface AppConstants {
        //
    }
}
