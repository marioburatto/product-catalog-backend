package com.philips.thirdparty.supplychain.dto;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProductsDto {
    private List<ProductDto> bundle;
}
