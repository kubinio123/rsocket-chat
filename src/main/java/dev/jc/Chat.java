package dev.jc;

import io.rsocket.Payload;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class Chat implements Subscriber<Payload> {

    private static final Logger logger = LoggerFactory.getLogger(Chat.class);
    private static final Sinks.Many<Payload> chatroom = Sinks.many().multicast().onBackpressureBuffer();

    public Flux<Payload> messages() {
        return chatroom.asFlux();
    }

    @Override
    public void onSubscribe(Subscription s) {
        logger.info("on subscribe");

        // no backpressure
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Payload payload) {
        logger.info("new message " + payload.getDataUtf8());
        chatroom.tryEmitNext(payload);
    }

    @Override
    public void onError(Throwable t) {
        logger.error("error occurred", t);
    }

    @Override
    public void onComplete() {
        logger.info("on complete");
    }
}
