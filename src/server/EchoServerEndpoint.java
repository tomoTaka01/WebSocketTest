package server;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Server Endpoint
 * 
 * @author tomo
 */
@ServerEndpoint("/echo")
public class EchoServerEndpoint {
    @OnOpen
    public void open(Session session){
        System.out.println(String.format("[open]%s", session));
    }
    @OnClose
    public void close(Session session){
        System.out.println(String.format("[close]%s", session));
    }
    @OnMessage
    public String onMessage(String message, Session session){
        System.out.println(String.format("[%s]%s", message, session));
        return message;
    }
}
