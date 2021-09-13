package com.philips.productcatalog.web.controller;

import com.philips.productcatalog.core.domain.Product;
import com.philips.productcatalog.core.repository.ProductRepository;
import com.philips.productcatalog.core.service.ProductEventProducer;
import com.philips.productcatalog.testutil.ProductFixture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ProductControllerTest {
    @MockBean
    private ProductEventProducer productEventProducer;

    @Autowired
    private ProductController productController;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setup() {
        productRepository.deleteAll();
    }

    @Test
    public void createProductSuccessful() throws Exception {
        Product product = ProductFixture.aNewProduct();

        ResponseEntity<Product> result = productController.createProduct(product);
        Product newProduct = result.getBody();
        MatcherAssert.assertThat(newProduct, IsNull.notNullValue());
        MatcherAssert.assertThat(newProduct.getName(), Is.is(product.getName()));
        MatcherAssert.assertThat(newProduct.getPrice(), Is.is(product.getPrice()));
        MatcherAssert.assertThat(newProduct.getQuantity(), Is.is(product.getQuantity()));
        MatcherAssert.assertThat(newProduct.getShortDescription(), Is.is(product.getShortDescription()));

        MatcherAssert.assertThat(newProduct.getId(), IsNull.notNullValue());
        MatcherAssert.assertThat(newProduct.getCreatedAt(), IsNull.notNullValue());
        MatcherAssert.assertThat(newProduct.getUpdatedAt(), IsNull.notNullValue());
        MatcherAssert.assertThat(newProduct.getUpdatedAt(), Is.is(newProduct.getCreatedAt()));

        Product loadedProduct = productController.findById(newProduct.getId()).getBody();
        MatcherAssert.assertThat(newProduct, IsEqual.equalTo(loadedProduct));
    }

    @Test
    public void createProduct_FailPublish() throws Exception {
        Mockito.when(productEventProducer.produce(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Failure publishing message."));
        Assertions.assertThrows(RuntimeException.class, () -> {
            productController.createProduct(ProductFixture.aNewProduct());
        });
        MatcherAssert.assertThat(productRepository.count(), Is.is(0L));
    }

    @Test
    public void createProduct_SuccessfulCRUD() throws Exception {
        Product product = productController.createProduct(ProductFixture.aNewProduct()).getBody();
        MatcherAssert.assertThat(product, IsNull.notNullValue());
        MatcherAssert.assertThat(productRepository.count(), Is.is(1L));
        MatcherAssert.assertThat(productController.listAll().size(), Is.is(1));

        Product productUpdated = productController.updateProduct(
                product.toBuilder().name("New Product Rename").build(),
                product.getId()).getBody();
        MatcherAssert.assertThat(productRepository.count(), Is.is(1L));
        MatcherAssert.assertThat(productController.listAll().size(), Is.is(1));
        MatcherAssert.assertThat(productUpdated, IsNull.notNullValue());
        MatcherAssert.assertThat(productUpdated.getName(), Is.is("New Product Rename"));
        MatcherAssert.assertThat(productUpdated.getUpdatedAt(), Matchers.greaterThan(product.getUpdatedAt()));

        productController.deleteProduct(product.getId());
        MatcherAssert.assertThat(productRepository.count(), Is.is(0L));
        MatcherAssert.assertThat(productController.listAll().size(), Is.is(0));
    }

    @Test
    public void createProduct_DoNotCreateTwiceTheSameProduct() throws Exception {
        Product product = productController.createProduct(ProductFixture.aNewProduct()).getBody();
        MatcherAssert.assertThat(productController.createProduct(product).getStatusCode(),
                Is.is(HttpStatus.CONFLICT));
        MatcherAssert.assertThat(productRepository.count(), Is.is(1L));
    }
}
