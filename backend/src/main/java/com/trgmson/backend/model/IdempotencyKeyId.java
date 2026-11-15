package com.trgmson.backend.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode
public class IdempotencyKeyId implements Serializable {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;
}
