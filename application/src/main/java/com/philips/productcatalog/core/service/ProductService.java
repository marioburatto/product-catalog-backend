package com.philips.productcatalog.core.service;

import com.philips.productcatalog.core.domain.Product;
import com.philips.productcatalog.core.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    private ProductRepository productRepository;
    private ProductEventPublisher eventPublisher;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Product> listAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product create(Product product) {
        Product newProduct = productRepository.save(product);
        eventPublisher.productCreated(newProduct);
        return newProduct;
    }

    @Transactional
    public Product update(Product product) {
        Product newProduct = productRepository.save(product);
        eventPublisher.productUpdated(newProduct);
        return newProduct;
    }

    @Transactional
    public void delete(Product product) {
        productRepository.delete(product);
        eventPublisher.productDeleted(product);
    }
}
