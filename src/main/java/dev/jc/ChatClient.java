package dev.jc;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Supplier;

public class ChatClient {

    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    public static void fireAndForget(String name) {
        RSocket rs = rsocket();
        forever(() -> rs.fireAndForget(DefaultPayload.create(name + ": Hi")).block());
    }

    public static void requestResponse(String name) {
        RSocket rs = rsocket();
        forever(() -> {
            Payload resp = rs.requestResponse(DefaultPayload.create(name + ": Hi, reply?")).block();
            logger.info(resp.getDataUtf8());
            return null;
        });
    }

    public static void requestStream(String name) {
        RSocket rs = rsocket();
        rs.requestStream(DefaultPayload.create(name + ": Hi, I am listening..."))
                .map(Payload::getDataUtf8)
                .doOnNext(logger::info)
                .doFinally(s -> rs.dispose())
                .blockLast();
    }

    public static void requestChannel(String name) {
        RSocket rs = rsocket();
        rs.requestChannel(Flux.interval(Duration.ofSeconds(5)).map(i -> DefaultPayload.create(name + ": spam " + i)))
                .map(Payload::getDataUtf8)
                .doOnNext(logger::info)
                .doFinally(s -> rs.dispose())
                .blockLast();
    }

    private static RSocket rsocket() {
        return RSocketConnector.connectWith(TcpClientTransport.create("localhost", 7000)).block();
    }

    private static void forever(Supplier<Void> sp) {
        while (true) {
            sp.get();
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
