package com.philips.productcatalog.worker.processor;

import com.philips.productcatalog.core.domain.EventType;
import com.philips.productcatalog.core.domain.Product;
import com.philips.productcatalog.core.domain.ProductEvent;
import com.philips.productcatalog.core.service.ProductEventProducer;
import com.philips.productcatalog.core.service.ProductService;
import com.philips.productcatalog.testutil.ProductFixture;
import com.philips.productcatalog.worker.integration.SupplyChainIntegration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class ProductEventProcessorTest {
    @MockBean
    private ProductEventProducer productEventProducer;

    @MockBean
    private SupplyChainIntegration supplyChainIntegration;

    @Autowired
    private ProductService productService;

    @Autowired
    @SpyBean
    private ProductEventProcessor productEventProcessor;

    @BeforeEach
    void setup() {
        Mockito
                .when(productEventProducer.produce(ArgumentMatchers.any(ProductEvent.class)))
                .then(param -> productEventProcessor.process(param.getArgument(0, ProductEvent.class)));

        Mockito
                .when(supplyChainIntegration.retrieveProductByExternal(ArgumentMatchers.anyString()))
                .then(param -> Optional.of(
                        Product.builder()
                                .id(UUID.fromString(param.getArgument(0, String.class)))
                                .build()));
    }

    @Test
    void testCrudOperations() {
        Product product = productService.create(ProductFixture.aNewProduct());
        ArgumentCaptor<ProductEvent> argument = ArgumentCaptor.forClass(ProductEvent.class);
        Mockito.verify(
                        productEventProcessor,
                        Mockito.times(1))
                .process(argument.capture());
        MatcherAssert.assertThat(argument.getValue().getEventType(), Is.is(EventType.PRODUCT_CREATED));

        productService.update(product.toBuilder().name("Another Name").build());
        Mockito.verify(
                        productEventProcessor,
                        Mockito.times(2))
                .process(argument.capture());
        MatcherAssert.assertThat(argument.getValue().getEventType(), Is.is(EventType.PRODUCT_UPDATED));

        productService.delete(product.toBuilder().name("Another Name").build());
        Mockito.verify(
                        productEventProcessor,
                        Mockito.times(3))
                .process(argument.capture());
        MatcherAssert.assertThat(argument.getValue().getEventType(), Is.is(EventType.PRODUCT_DELETED));
    }

}
