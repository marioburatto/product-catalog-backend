package com.philips.thirdparty.supplychain;

import com.philips.thirdparty.supplychain.dto.ProductDto;
import com.philips.thirdparty.supplychain.dto.ProductsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public class SupplyChainClient {

    private WebClient webClient;

    public SupplyChainClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ProductsDto> listProducts() {
        return webClient.get()
                .uri("/supply-chain")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProductsDto.class);
    }

    public Mono<ProductDto> createProduct(ProductDto productDto) {
        return webClient.post()
                .uri("/supply-chain")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(productDto)
                .retrieve()
                .bodyToMono(ProductDto.class);
    }

    public Mono<ProductDto> getProductById(String id) {
        return webClient.get()
                .uri("/supply-chain/{productId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .onErrorResume(
                        WebClientResponseException.class,
                        ex -> ex.getStatusCode() == HttpStatus.NOT_FOUND ? Mono.empty() : Mono.error(ex));
    }

    public Mono<ResponseEntity<Void>> deleteProductById(String id) {
        return webClient.delete()
                .uri("/supply-chain/{productId}", id)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(
                        WebClientResponseException.class,
                        ex -> ex.getStatusCode() == HttpStatus.NOT_FOUND ? Mono.empty() : Mono.error(ex));
    }
}
