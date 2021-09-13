package com.philips.productcatalog.worker.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Configuration
@ConfigurationProperties(prefix = "integration.supply-chain")
public class SupplyChainProperties {
    private String baseUrl;
    private Integer connectionTimeoutMillis;
    private Integer responseTimeoutMillis;
    private Integer readTimeoutMillis;
    private Integer writeTimeoutMillis;
}
