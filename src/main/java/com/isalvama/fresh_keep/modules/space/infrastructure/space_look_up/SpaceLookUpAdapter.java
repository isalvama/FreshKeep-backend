package com.isalvama.fresh_keep.modules.space.infrastructure.space_look_up;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.SpaceLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.GetStorageSpotsDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.modules.space.application.port.in.GetStorageSpotsUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.GetStorageSpotsCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.StorageSpotResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpaceLookUpAdapter implements SpaceLookUpPort {
    private final GetStorageSpotsUseCase getStorageSpotsUseCase;

    @Override
    public List<StorageSpotDto> getStorageSpotsBySpaceIdAndParticipantId(GetStorageSpotsDto dto) {

        List<StorageSpotResult> result = getStorageSpotsUseCase.execute(new GetStorageSpotsCommand(dto.spaceId(), dto.userId()));

        return result.stream()
                .map(r -> StorageSpotDto.create(r.id(), r.name(), r.type()))
                .toList();
    }
}
