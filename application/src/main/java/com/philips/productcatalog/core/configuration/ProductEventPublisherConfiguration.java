package com.philips.productcatalog.core.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.philips.productcatalog.core.service.ProductEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

@Configuration
@Profile("dev")
@Slf4j
public class ProductEventPublisherConfiguration {

    @Configuration
    @EnableJms
    static class JmsEmbedded {
    }

    @Bean
    public ProductEventProducer productEventProducer(JmsTemplate jmsTemplate) {
        return event -> {
            jmsTemplate.convertAndSend("ProductEvent", event);
            log.info("Product Event published {}", event.toString());
            return event;
        };
    }

    @Bean
    public ActiveMQConnectionFactoryCustomizer configureRedeliveryPolicy() {
        return connectionFactory ->
        {
            RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
            // configure redelivery policy
            // Never give up retrying
            redeliveryPolicy.setMaximumRedeliveries(RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES);

            // First retry after 2 seconds
            redeliveryPolicy.setInitialRedeliveryDelay(2000);

            // Next retries every 5 seconds
            redeliveryPolicy.setRedeliveryDelay(5000);

            connectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        };
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(defaultObjectMapper());
        return converter;
    }

    @Bean
    public ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }
}
