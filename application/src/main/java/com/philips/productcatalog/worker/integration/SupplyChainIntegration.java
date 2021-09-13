package com.philips.productcatalog.worker.integration;

import com.philips.productcatalog.core.domain.Product;
import com.philips.thirdparty.supplychain.SupplyChainClient;
import com.philips.thirdparty.supplychain.dto.ProductDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SupplyChainIntegration {
    private SupplyChainClient client;

    @Autowired
    public SupplyChainIntegration(SupplyChainClient client) {
        this.client = client;
    }

    public Optional<Product> retrieveProductByExternal(String externalId) {
        return Optional.ofNullable(
                        client
                                .getProductById(externalId)
                                .block()
                )
                .map(this::toDomain);
    }

    public void create(Product product) {
        client.createProduct(toRepresentation(product)).block();
    }

    public void update(Product product) {
        client.createProduct(toRepresentation(product)).block();
    }

    public void delete(Product product) {
        client.deleteProductById(product.getId().toString()).block();
    }

    public ProductDto toRepresentation(Product product) {
        return ProductDto.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    public Product toDomain(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }
        return Product.builder()
                .id(UUID.fromString(productDto.getId()))
                .name(productDto.getName())
                .price(productDto.getPrice())
                .quantity(productDto.getQuantity())
                .build();
    }
}
