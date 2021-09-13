package com.philips.productcatalog.core.domain;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode
public class Product {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String shortDescription;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void onPersisting() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
            updatedAt = createdAt;
        } else {
            updatedAt = Instant.now();
        }
    }
}
