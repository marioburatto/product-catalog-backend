package com.philips.productcatalog.core.domain;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
@ToString
public class ProductEvent {
    private Instant eventDate;
    private EventType eventType;
    private UUID productId;
}
