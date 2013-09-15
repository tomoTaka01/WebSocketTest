package client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * Client Endpoint
 * 
 * @author tomo
 */
public class EchoClienEndpoint extends Endpoint{
    private BlockingQueue<String> queue;

    public EchoClienEndpoint(BlockingQueue<String> queue) {
        this.queue = queue;
    }
    
    @Override
    public void onOpen(Session session, EndpointConfig ec) {
        try {
            session.getBasicRemote().sendText("open connection");
        } catch (IOException ex) {
            Logger.getLogger(EchoClienEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                queue.offer(String.format("received message is %s", message));
            }
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        queue.offer("*** close connection ***");
    }
}
