package com.philips.productcatalog.worker.configuration;

import com.philips.productcatalog.core.domain.ProductEvent;
import com.philips.productcatalog.worker.processor.ProductEventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;

@Configuration
@Profile("dev")
public class ProductEventConsumerConfiguration {
    @Autowired
    private ProductEventProcessor productEventProcessor;

    @JmsListener(destination = "ProductEvent", containerFactory = "jmsContainerFactory")
    public void productEventListener(ProductEvent productEvent) {
        productEventProcessor.process(productEvent);
    }
}
