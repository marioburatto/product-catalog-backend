package com.philips.productcatalog.core.service;

import com.philips.productcatalog.core.domain.EventType;
import com.philips.productcatalog.core.domain.Product;
import com.philips.productcatalog.core.domain.ProductEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProductEventPublisher {
    private ProductEventProducer productEventProducer;

    public ProductEventPublisher(ProductEventProducer productEventProducer) {
        this.productEventProducer = productEventProducer;
    }

    public void productCreated(Product product) {
        publish(createEvent(product, EventType.PRODUCT_CREATED));
    }

    public void productUpdated(Product product) {
        publish(createEvent(product, EventType.PRODUCT_UPDATED));
    }

    public void productDeleted(Product product) {
        publish(createEvent(product, EventType.PRODUCT_DELETED));
    }

    public ProductEvent publish(ProductEvent event) {
        productEventProducer.produce(event);
        return event;
    }

    public ProductEvent createEvent(Product product, EventType eventType) {
        return ProductEvent.builder()
                .eventDate(Instant.now())
                .eventType(eventType)
                .productId(product.getId())
                .build();
    }
}
