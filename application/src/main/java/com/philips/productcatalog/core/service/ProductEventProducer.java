package com.philips.productcatalog.core.service;

import com.philips.productcatalog.core.domain.ProductEvent;

public interface ProductEventProducer {
    ProductEvent produce(ProductEvent event);
}
