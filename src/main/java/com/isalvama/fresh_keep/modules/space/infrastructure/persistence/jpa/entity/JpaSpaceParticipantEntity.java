package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "spaces_participants")
public class JpaSpaceParticipantEntity {

    @EmbeddedId
    private JpaSpaceParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("spaceId")
    @JoinColumn(name = "space_id", nullable = false)
    private JpaSpaceEntity space;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public JpaSpaceParticipantEntity(
            JpaSpaceEntity space,
            UUID participantId,
            Instant joinedAt
    ) {
        this.space = space;
        this.id = new JpaSpaceParticipantId(space.getId(), participantId);
        this.joinedAt = joinedAt;
    }
}
