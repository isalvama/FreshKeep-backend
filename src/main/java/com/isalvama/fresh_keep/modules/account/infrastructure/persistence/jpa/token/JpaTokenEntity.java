package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.token;

import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.account.JpaAccountEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity(name = "tokens")
public class JpaTokenEntity {

    public enum TokenType{
        BEARER
    }

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, columnDefinition = "BEARER")
    private TokenType tokenType = TokenType.BEARER;

    private boolean revoked;

    private boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private JpaAccountEntity account;

}
