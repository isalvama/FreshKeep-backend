package com.isalvama.fresh_keep.modules.account.application.port.out.dto;

public record ResolvedEntities(
        String userId,
        String adminId
) {
    public static ResolvedEntities constitute (String userId, String adminId){
        return new ResolvedEntities(
                userId,
                adminId
        );
    }
}
