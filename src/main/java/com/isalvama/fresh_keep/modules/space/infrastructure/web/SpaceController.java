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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create a new Space",
            description = "creatorId is resolved server-side from the authenticated principal's userId claim. " +
                    "participantIds on creation always contains exactly the creator's userId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Space created",
                    content = @Content(schema = @Schema(implementation = SpaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bean validation failed (spaceName/emoji/storageSpots blank/missing/too " +
                    "long/empty, a storage spot's name/type blank/too long/not a recognized type), the request body is malformed/missing, " +
                    "or a domain rule was violated (spaceName has no letters, emoji fails emoji validation, a storage spot name has no " +
                    "letters, or two storage spots share the same name+type)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while saving the space",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get the spaces data the user is participant in",
            description = "Returns every space where the authenticated user is a participant (creators are " +
                    "automatically participants). Returns 200 with an empty array, not an error, when the user " +
                    "is not a participant in any space."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Spaces retrieved (possibly empty)",
                    content = @Content(schema = @Schema(implementation = SpaceResponse.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while retrieving the user's spaces",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<SpaceResponse>> getByParticipantId(
            @AuthenticationPrincipal(expression = "userId") String userId) {

        List<SpaceResult> result = getSpacesByParticipantIdUseCase.execute(userId);

        List<SpaceResponse> responses = result.stream().map(mapper::toResponse).toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{spaceId}/overview")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get the space's products and storage spots data",
            description = "Storage spots and products are looked up live for this call, not carried over from any " +
                    "earlier request. A space with no products yet returns 200 with productResults: []."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overview retrieved",
                    content = @Content(schema = @Schema(implementation = SpaceOverviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "spaceId path variable is not a valid UUID, or the space does not exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The authenticated user is not a participant of spaceId",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while validating if the space already exists or the participancy of the user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SpaceOverviewResponse> getOverView(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "spaceId") @UUID String spaceId) {

        GetSpaceOverviewResult result = getSpaceOverviewUseCase.execute(new GetSpaceOverviewCommand(spaceId, userId));

        SpaceOverviewResponse response = mapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{spaceId}/invitations")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create an invitation to join the space",
            description = "No request body. Generates a token valid for 24 hours from creation, with no usage-" +
                    "count limit during that window. The caller must already be a participant of spaceId."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invitation created",
                    content = @Content(schema = @Schema(implementation = CreateSpaceInvitationResponse.class))),
            @ApiResponse(responseCode = "400", description = "spaceId path variable is not a valid UUID",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The authenticated user is not a participant of spaceId",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while validating if the space already exists or the participancy of the user or while saving the newly created space invitation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Use a space invitation and join its space",
            description = "No request body. Adds the authenticated user as a participant of the invitation's " +
                    "space; the token's use counter is incremented and the participant is added atomically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Joined the space",
                    content = @Content(schema = @Schema(implementation = JoinSpaceByInvitationResponse.class))),
            @ApiResponse(responseCode = "400", description = "token does not reference an existing invitation, or the " +
                    "invitation has expired",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The authenticated user is already a participant of the invitation's space",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while retrieving the invitation data, validating if the space already exists or the participancy of the user or while recording the new participant",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<JoinSpaceByInvitationResponse> useInvitation(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "token") @NotNull @NotBlank String token) {

        JoinSpaceByInvitationResult result = joinSpaceByInvitationUseCase.execute(new JoinSpaceByInvitationCommand(userId, token));

        return ResponseEntity.ok(new JoinSpaceByInvitationResponse(result.spaceId()));
    }
}
