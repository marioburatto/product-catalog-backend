package com.philips.productcatalog.worker.configuration;

import com.philips.thirdparty.supplychain.SupplyChainClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class SupplyChainConfiguration {
    @Bean
    SupplyChainClient createSupplyChainClient(SupplyChainProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        properties.getConnectionTimeoutMillis())
                .responseTimeout(Duration.ofMillis(properties.getResponseTimeoutMillis()))
                .doOnConnected(conn ->
                        conn
                                .addHandlerLast(new ReadTimeoutHandler(
                                        properties.getReadTimeoutMillis(), TimeUnit.MILLISECONDS)
                                )
                                .addHandlerLast(new WriteTimeoutHandler(
                                        properties.getWriteTimeoutMillis(), TimeUnit.MILLISECONDS)
                                )
                );

        WebClient client = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return new SupplyChainClient(client);
    }
}
