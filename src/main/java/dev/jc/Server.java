package dev.jc;

import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Chat chat = new Chat();

    public static void main(String[] args) throws InterruptedException {

        SocketAcceptor sa = SocketAcceptor.forRequestChannel(payloads -> {
            payloads.subscribe(chat);
            return chat.messages();
        });

        RSocketServer.create(sa).bindNow(TcpServerTransport.create("localhost", 7000));

        logger.info("started chat server");

        Thread.currentThread().join();
    }
}
