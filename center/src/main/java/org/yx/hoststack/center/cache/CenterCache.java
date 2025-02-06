package org.yx.hoststack.center.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class CenterCache {
    private final ImageCache imageCache;
    private final ContainerCache containerCache;
    private final ServiceDetailCache serviceDetailCache;

    public void initCache() {
        imageCache.initCache();
        containerCache.initCache();
        serviceDetailCache.initCache();
        //TODO load other cache
    }
}
