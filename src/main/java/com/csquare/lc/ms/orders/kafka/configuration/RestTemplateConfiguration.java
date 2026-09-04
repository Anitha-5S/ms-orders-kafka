package com.csquare.lc.ms.orders.kafka.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@org.springframework.context.annotation.Configuration
public class RestTemplateConfiguration {

    @Value("${api.connection.timeout:60000}") private int timeOut;

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate(getClientHttpRequestFactory());
    }

    @Bean
    public ClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeOut);
        factory.setReadTimeout(timeOut);
        return factory;
    }

}
