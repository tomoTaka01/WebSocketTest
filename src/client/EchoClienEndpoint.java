package client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
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
    private ObservableList<String> messageList;

    public EchoClienEndpoint(ObservableList<String> messageList) {
        this.messageList = messageList;
    }
    
    @Override
    public void onOpen(Session session, EndpointConfig ec) {
        try {
            session.getBasicRemote().sendText("start connection");
        } catch (IOException ex) {
            Logger.getLogger(EchoClienEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                messageList.add(0, String.format("received message is %s", message));
            }
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        messageList.add(0, "close session");
    }    
}
