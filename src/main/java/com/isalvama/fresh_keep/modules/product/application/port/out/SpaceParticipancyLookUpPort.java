package com.isalvama.fresh_keep.modules.product.application.port.out;

public interface SpaceParticipancyLookUpPort {
    boolean isParticipant(String userId, String storageSpotId);
}
