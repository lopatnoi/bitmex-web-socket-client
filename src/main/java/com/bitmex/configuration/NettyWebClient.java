package com.bitmex.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.websocket.WebsocketInbound;

import java.net.URI;

public class NettyWebClient extends ReactorNettyWebSocketClient {

    private static final Log logger = LogFactory.getLog(ReactorNettyWebSocketClient.class);

    private final int maxFramePayloadLength;

    public NettyWebClient(int maxFramePayloadLength) {
        super(HttpClient.create());
        this.maxFramePayloadLength = maxFramePayloadLength;
    }

    public NettyWebClient(HttpClient httpClient, int maxFramePayloadLength) {
        super(httpClient);
        this.maxFramePayloadLength = maxFramePayloadLength;
    }

    @Override
    public Mono<Void> execute(URI url, HttpHeaders requestHeaders, WebSocketHandler handler) {
        return getHttpClient()
                .headers(nettyHeaders -> setNettyHeaders(requestHeaders, nettyHeaders))
                .websocket(StringUtils.collectionToCommaDelimitedString(handler.getSubProtocols()))
                .uri(url.toString())
                .handle((inbound, outbound) -> {
                    HttpHeaders responseHeaders = toHttpHeaders(inbound);
                    String protocol = responseHeaders.getFirst("Sec-WebSocket-Protocol");
                    HandshakeInfo info = new HandshakeInfo(url, responseHeaders, Mono.empty(), protocol);
                    NettyDataBufferFactory factory = new NettyDataBufferFactory(outbound.alloc());
                    WebSocketSession session = new ReactorNettyWebSocketSession(inbound, outbound, info, factory, maxFramePayloadLength);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Started session '" + session.getId() + "' for " + url);
                    }
                    return handler.handle(session);
                })
                .doOnRequest(n -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Connecting to " + url);
                    }
                })
                .next();
    }

    private void setNettyHeaders(HttpHeaders httpHeaders, io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        httpHeaders.forEach(nettyHeaders::set);
    }

    private HttpHeaders toHttpHeaders(WebsocketInbound inbound) {
        HttpHeaders headers = new HttpHeaders();
        io.netty.handler.codec.http.HttpHeaders nettyHeaders = inbound.headers();
        nettyHeaders.forEach(entry -> {
            String name = entry.getKey();
            headers.put(name, nettyHeaders.getAll(name));
        });
        return headers;
    }
}
