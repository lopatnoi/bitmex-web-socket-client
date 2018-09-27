package com.bitmex;

import com.bitmex.api.Operation;
import com.bitmex.configuration.ClientConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;

import java.net.URI;
import java.util.Collections;

public class AppLauncher {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static AnnotationConfigApplicationContext context;

    static {
        context = new AnnotationConfigApplicationContext();
        context.register(ClientConfiguration.class);
        context.refresh();
    }

    public static void main(String[] args) {
        new AppLauncher().execute();
    }

    private void execute() {
        URI url = URI.create("wss://www.bitmex.com/realtime");
        Mono<String> subscribe = subscribe("XBTUSD");

        ReactorNettyWebSocketClient client = context.getBean(ReactorNettyWebSocketClient.class);
        TopicProcessor<String> output = TopicProcessor.create();

        Mono<Void> request = client.execute(url, session ->
                session.send(subscribe.map(session::textMessage))           // subscribe on socket
                        .thenMany(session.receive()                         // receive messages
                                .map(WebSocketMessage::getPayloadAsText)
                                .subscribeWith(output))
                        .then()
        );

        output.doOnSubscribe(subscription -> request.subscribe())
                .subscribe(
                        msg ->
                                System.out.println(Thread.currentThread().getName() + " : " + msg),
                        System.err::println);

        while (true) {

        }
    }

    private Mono<String> subscribe(String orderBook) {
        Operation operation = new Operation("subscribe", Collections.singletonList("orderBookL2:" + orderBook));
        try {
            return Mono.just(OBJECT_MAPPER.writeValueAsString(operation));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.empty();
        }
    }
}
