package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.space.application.port.in.CreateSpaceUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateSpaceService implements CreateSpaceUseCase {

    @Override
    public SpaceResult execute(CreateSpaceCommand command) {
        // pasar a domain
        // persistir
        // mappear a Space Result
        // devolver
        return null;
    }
}
