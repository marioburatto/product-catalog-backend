package com.philips.productcatalog.worker.processor;

import com.philips.productcatalog.core.domain.Product;
import com.philips.productcatalog.core.domain.ProductEvent;
import com.philips.productcatalog.core.service.ProductService;
import com.philips.productcatalog.worker.integration.SupplyChainIntegration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ProductEventProcessor {
    private SupplyChainIntegration supplyChainIntegration;
    private ProductService productService;

    public ProductEventProcessor(SupplyChainIntegration supplyChainIntegration,
                                 ProductService productService) {
        this.supplyChainIntegration = supplyChainIntegration;
        this.productService = productService;
    }

    public ProductEvent process(ProductEvent productEvent) {
        log.info("Consuming product event {}", productEvent.toString());

        Optional<Product> currentProduct = productService
                .findById(productEvent.getProductId());

        Optional<Product> supplyChainProduct = supplyChainIntegration
                .retrieveProductByExternal(productEvent.getProductId().toString());

        if (currentProduct.isPresent()) {
            if (supplyChainProduct.isPresent()) {
                supplyChainIntegration.update(currentProduct.get());
                log.info("Product updated in SupplyChain {}", productEvent.getProductId());
            } else {
                supplyChainIntegration.create(currentProduct.get());
                log.info("Product created in SupplyChain {}", productEvent.getProductId());
            }
        } else if (supplyChainProduct.isPresent()) {
            supplyChainIntegration.delete(supplyChainProduct.get());
            log.info("Product deleted from SupplyChain {}", productEvent.getProductId());
        } else {
            log.info("Nothing to do at SupplyChain for product {}", productEvent.getProductId());
        }

        return productEvent;
    }
}
