package com.isalvama.fresh_keep.modules.space.infrastructure.web;

import com.isalvama.fresh_keep.modules.space.application.port.in.CreateSpaceUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper.CreateSpaceCommandMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper.SpaceResponseMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request.CreateSpaceRequest;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceResponseMapper mapper;
    private final CreateSpaceUseCase createSpaceUseCase;

    // ── POST /api/v1/spaces ──────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SpaceResponse> create(
            @RequestPart("data") @Valid CreateSpaceRequest request,
            @AuthenticationPrincipal(expression = "userId") String userId
    ) {

        CreateSpaceCommand command = CreateSpaceCommandMapper.toCommand(request, userId);

        SpaceResult result = createSpaceUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toResponse(result));
    }
}
