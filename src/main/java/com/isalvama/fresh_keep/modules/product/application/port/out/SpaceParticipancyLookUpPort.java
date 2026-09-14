package com.isalvama.fresh_keep.modules.product.application.port.out;

import java.util.Set;

public interface SpaceParticipancyLookUpPort {
    boolean isParticipant(String userId, String storageSpotId);
    Set<String> filterAccessible(String userId, Set<String> storageSpotIds);
}
