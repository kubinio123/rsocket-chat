package dev.jc;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Chat chat = new Chat();

    public static void main(String[] args) throws InterruptedException {

        RSocket rSocket = new RSocket() {
            @Override
            public Mono<Void> fireAndForget(Payload payload) {
                chat.sendMessage(payload);
                return Mono.empty();
            }

            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                chat.sendMessage(payload);
                return Mono.just(Chat.ACK);
            }

            @Override
            public Flux<Payload> requestStream(Payload payload) {
                chat.sendMessage(payload);
                return chat.messages();
            }

            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                payloads.subscribe(chat);
                return chat.messages();
            }
        };

        RSocketServer.create(SocketAcceptor.with(rSocket)).bindNow(TcpServerTransport.create("localhost", 7000));

        logger.info("started chat server");

        Thread.currentThread().join();
    }
}
