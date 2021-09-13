package com.philips.thirdparty.supplychain.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class ProductDto {
    private String id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
}
