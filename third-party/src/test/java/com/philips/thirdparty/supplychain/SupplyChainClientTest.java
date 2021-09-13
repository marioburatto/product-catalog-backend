package com.philips.thirdparty.supplychain;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.philips.thirdparty.supplychain.dto.ProductDto;
import com.philips.thirdparty.supplychain.dto.ProductsDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

//@RunWith(SpringRunner.class)
public class SupplyChainClientTest {

    private static final int singleRequestTime = 1000;
    private static WireMockServer wireMockServer;
    public static SupplyChainClient client;

    @BeforeAll
    public static void setup() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        client = new SupplyChainClient(WebClient.create("http://localhost:" + wireMockServer.port()));
    }

    @AfterAll
    public static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void listAllProducts() throws Exception {
        stubFor(get(urlEqualTo("/supply-chain")).willReturn(aResponse()
                .withFixedDelay(singleRequestTime)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(listAllResponse())));

        Mono<ProductsDto> result = client.listProducts();

        StepVerifier
                .create(result)
                .expectNextMatches(products -> products.getBundle().size() == 3)
                .verifyComplete();
    }

    @Test
    public void getProductById() throws Exception {
        String id = "97a5a660-ca95-43e8-870b-cfa4aecaaa14";
        stubFor(get(urlEqualTo("/supply-chain/" + id)).willReturn(aResponse()
                .withFixedDelay(singleRequestTime)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getByIdResponse())));

        Mono<ProductDto> result = client.getProductById(id);

        StepVerifier
                .create(result)
                .expectNextMatches(product -> product.getId().equals(id))
                .verifyComplete();
    }

    @Test
    public void getProductByIdNotFound() throws Exception {
        String id = "000000000000000000000";
        stubFor(get(urlEqualTo("/supply-chain/" + id)).willReturn(aResponse()
                .withFixedDelay(singleRequestTime)
                .withStatus(404)
                .withBody("")));

        Mono<ProductDto> result = client.getProductById(id);

        StepVerifier
                .create(result)
                .verifyComplete();
    }

    @Test
    public void getProductByIdError() throws Exception {
        String id = "000000000000000000000";
        wireMockServer.stubFor(get(urlEqualTo("/supply-chain/" + id)).willReturn(aResponse()
                .withFixedDelay(singleRequestTime)
                .withStatus(500)
                .withBody("Random Internal Server Error")));

        Mono<ProductDto> result = client.getProductById(id);

        StepVerifier
                .create(result)
                .expectErrorMatches(error -> error instanceof WebClientResponseException.InternalServerError)
                .verify();
    }

    @Test
    public void createProductSuccessful() throws Exception {
        ProductDto productDto = new ObjectMapper().readValue(getByIdResponse(), ProductDto.class);

        wireMockServer.stubFor(post(urlEqualTo("/supply-chain")).willReturn(aResponse()
                .withFixedDelay(singleRequestTime)
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(getByIdResponse())));

        Mono<ProductDto> result = client.createProduct(productDto);

        StepVerifier
                .create(result)
                .expectNext(productDto)
                .verifyComplete();
    }

    @Test
    public void deleteProductById() throws Exception {
        String id = "97a5a660-ca95-43e8-870b-cfa4aecaaa14";
        wireMockServer.stubFor(delete(urlEqualTo("/supply-chain/" + id)).willReturn(aResponse()
                .withFixedDelay(singleRequestTime)
                .withStatus(204)));

        Mono<ResponseEntity<Void>> result = client.deleteProductById(id);

        StepVerifier
                .create(result)
                .expectNextMatches(response -> response.getStatusCode().equals(HttpStatus.NO_CONTENT))
                .verifyComplete();
    }

    @Test
    public void deleteProductByIdNotFound() throws Exception {
        String id = "000000000000000000000";
        wireMockServer.stubFor(delete(urlEqualTo("/supply-chain/" + id)).willReturn(aResponse()
                .withFixedDelay(singleRequestTime)
                .withStatus(404)
                .withBody("")));

        Mono<ResponseEntity<Void>> result = client.deleteProductById(id);

        StepVerifier
                .create(result)
                .verifyComplete();
    }

    @Test
    public void deleteProductByIdError() throws Exception {
        String id = "000000000000000000000";
        wireMockServer.stubFor(delete(urlEqualTo("/supply-chain/" + id)).willReturn(aResponse()
                .withFixedDelay(singleRequestTime)
                .withStatus(500)
                .withBody("Random Internal Server Error")));

        Mono<ResponseEntity<Void>> result = client.deleteProductById(id);

        StepVerifier
                .create(result)
                .expectErrorMatches(error -> error instanceof WebClientResponseException.InternalServerError)
                .verify();
    }

    private String listAllResponse() throws IOException {
        return readFile("supply-chain-responses/listAll.json");
    }

    private String getByIdResponse() throws IOException {
        return readFile("supply-chain-responses/getById.json");
    }

    private String readFile(String fileName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        return new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
