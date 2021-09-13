package com.philips.productcatalog.testutil;

import com.philips.productcatalog.core.domain.Product;

import java.math.BigDecimal;

public class ProductFixture {

    public static Product aNewProduct() {
        return Product.builder()
                .name("NewProduct")
                .price(BigDecimal.TEN)
                .quantity(99)
                .shortDescription("Just a sample")
                .build();
    }
}
