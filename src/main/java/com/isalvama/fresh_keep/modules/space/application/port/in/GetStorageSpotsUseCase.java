package com.isalvama.fresh_keep.modules.space.application.port.in;

import com.isalvama.fresh_keep.modules.space.application.port.in.command.GetStorageSpotsCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.StorageSpotResult;

import java.util.List;

public interface GetStorageSpotsUseCase {
    List<StorageSpotResult> execute (GetStorageSpotsCommand command);
}
