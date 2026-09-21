package com.isalvama.fresh_keep.modules.space.infrastructure.web;

import com.isalvama.fresh_keep.modules.space.application.port.in.*;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceInvitationCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.GetSpaceOverviewCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.JoinSpaceByInvitationCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.CreateSpaceInvitationResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.GetSpaceOverviewResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.JoinSpaceByInvitationResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper.CreateSpaceCommandMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper.SpaceResponseMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request.CreateSpaceRequest;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.CreateSpaceInvitationResponse;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.JoinSpaceByInvitationResponse;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceOverviewResponse;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
@Tag(name = "Spaces", description = "Spaces endpoints")
public class SpaceController {

    private final SpaceResponseMapper mapper;
    private final CreateSpaceUseCase createSpaceUseCase;
    private final GetSpacesByParticipantIdUseCase getSpacesByParticipantIdUseCase;
    private final GetSpaceOverviewUseCase getSpaceOverviewUseCase;
    private final CreateSpaceInvitationUseCase createSpaceInvitationUseCase;
    private final JoinSpaceByInvitationUseCase joinSpaceByInvitationUseCase;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new Space")
    public ResponseEntity<SpaceResponse> create(
            @Valid @RequestBody CreateSpaceRequest request,
            @AuthenticationPrincipal(expression = "userId") String userId) {

        CreateSpaceCommand command = CreateSpaceCommandMapper.toCommand(request, userId);

        SpaceResult result = createSpaceUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toResponse(result));
    }

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get the spaces data the user is participant in")
    public ResponseEntity<List<SpaceResponse>> getByParticipantId(
            @AuthenticationPrincipal(expression = "userId") String userId) {

        List<SpaceResult> result = getSpacesByParticipantIdUseCase.execute(userId);

        List<SpaceResponse> responses = result.stream().map(mapper::toResponse).toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{spaceId}/overview")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get the space's products and storage spots data")
    public ResponseEntity<SpaceOverviewResponse> getOverView(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "spaceId") @UUID String spaceId) {

        GetSpaceOverviewResult result = getSpaceOverviewUseCase.execute(new GetSpaceOverviewCommand(spaceId, userId));

        SpaceOverviewResponse response = mapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{spaceId}/invitations")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create an invitation to join the space")
    public ResponseEntity<CreateSpaceInvitationResponse> createInvitation(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "spaceId") @UUID String spaceId) {

        CreateSpaceInvitationResult result = createSpaceInvitationUseCase.execute(new CreateSpaceInvitationCommand(spaceId, userId));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toResponse(result));
    }

    @PostMapping("/invitations/{token}/join")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Use a space invitation and join its space")
    public ResponseEntity<JoinSpaceByInvitationResponse> useInvitation(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "token") @NotNull @NotBlank String token) {

        JoinSpaceByInvitationResult result = joinSpaceByInvitationUseCase.execute(new JoinSpaceByInvitationCommand(userId, token));

        return ResponseEntity.ok(new JoinSpaceByInvitationResponse(result.spaceId()));
    }
}
