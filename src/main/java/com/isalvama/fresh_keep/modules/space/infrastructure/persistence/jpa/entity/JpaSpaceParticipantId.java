package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaSpaceParticipantId implements Serializable {

    @Column(name = "space_id")
    private UUID spaceId;

    @Column(name = "participant_id")
    private UUID participantId;
}
