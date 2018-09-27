package com.bitmex.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

@Configuration
public class ClientConfiguration {

    @Bean
    public ReactorNettyWebSocketClient reactorNettyWebSocketClient() {
        return new NettyWebClient(128 * 1024); // 128 * 1024 = 131072
    }
}
