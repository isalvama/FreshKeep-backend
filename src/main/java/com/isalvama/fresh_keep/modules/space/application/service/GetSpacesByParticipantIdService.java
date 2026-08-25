package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.space.application.port.in.GetSpacesByParticipantIdUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSpacesByParticipantIdService implements GetSpacesByParticipantIdUseCase {
    private final SpaceRepositoryPort spaceRepositoryPort;

    @Override
    public List<SpaceResult> execute(String userId) {
        List<Space> spaces = spaceRepositoryPort.getByParticipantId(UserId.from(userId));
        if (spaces.isEmpty()){
            return List.of();
        }
        return spaces.stream().map(SpaceResult::fromDomain).toList();
    }
}
