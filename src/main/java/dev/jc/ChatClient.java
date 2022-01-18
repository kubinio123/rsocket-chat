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

import static java.nio.file.Files.readAllLines;
import static java.nio.file.Paths.get;

public class ChatClient {

    public static void start(String name) {
        try {
            Logger logger = LoggerFactory.getLogger(ChatClient.class);

            Flux<Payload> messages = Flux.fromIterable(readAllLines(get("src/main/resources/chat.txt")))
                    .delayElements(Duration.ofSeconds(5))
                    .map(line -> DefaultPayload.create(name + ": " + line));

            RSocket socket = RSocketConnector.connectWith(TcpClientTransport.create("localhost", 7000)).block();

            socket.requestChannel(messages)
                    .doFinally(signalType -> socket.dispose())
                    .subscribe(msg -> logger.info(msg.getDataUtf8()));

            Thread.currentThread().join();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
