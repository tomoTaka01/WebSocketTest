package server;

import org.glassfish.tyrus.server.Server;

/**
 * run the Echo Server
 * 
 * @author tomo
 */
public class EchoMain {

    public static void main(String[] args) throws Exception {
        Server server = new Server("localhost", 8080, "/ws", EchoServerEndpoint.class);
        try {
            server.start();
            System.in.read();
        } finally {
            server.stop();
        }
    }
}
