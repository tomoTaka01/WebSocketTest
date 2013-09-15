package client;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;

/**
 * Client for the Echo Server
 * 
 * @author tomo
 */
public class EchoClientFX extends Application {

    private BlockingQueue<String> queue = new LinkedBlockingDeque<>();
    private EchoClientService service = new EchoClientService();
    private Timeline timer;
    private ObservableList<String> messageList = FXCollections.observableArrayList();
    private BooleanProperty connectionProp = new SimpleBooleanProperty(false);

    @Override
    public void start(Stage stage) throws Exception {
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        ListView messageView = new ListView(messageList);
        root.getChildren().addAll(addLine1(), addLine2(), messageView);
        stage.setScene(new Scene(root, 500, 300));
        stage.setTitle("Echo Client");
        stage.show();
    }
    /*
     * add the line1(HBox)
     */
    private Node addLine1(){
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(5);
        Button openBtn = new Button("open");
        Button closeBtn = new Button("close");
        openBtn.disableProperty().bind(connectionProp);
        closeBtn.disableProperty().bind(Bindings.not(connectionProp));
        openConnection(openBtn);
        closeConnection(closeBtn);
        hbox.getChildren().addAll(openBtn, closeBtn);
        return hbox;
    }
    /*
     * open the connection to the echo server
     */
    private void openConnection(final Button openBtn) {
        openBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                service.reset();
                service.start();
                timer = new Timeline(new KeyFrame(Duration.millis(100),
                        new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        while (!queue.isEmpty()) {
                            try {
                                String message = queue.take();
                                messageList.add(0, message);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(EchoClientFX.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }));
                timer.setCycleCount(Timeline.INDEFINITE);
            }
        });
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                timer.play();
                connectionProp.setValue(Boolean.TRUE);
            }
        });
    }
    /*
     * close the connection
     */
    private void closeConnection(Button closeBtn){
        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                connectionProp.setValue(Boolean.FALSE);
                service.closeSession();
            }
        });
    }
    /*
     * add the line2(HBox)
     */
    private Node addLine2(){
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(5);
        Label messageLbl = new Label("Message:");
        final TextField messageTxt = new TextField();
        messageTxt.setPrefWidth(200);
        messageTxt.disableProperty().bind(Bindings.not(connectionProp));
        Button sendBtn = new Button("send");
        sendBtn.disableProperty().bind(Bindings.not(connectionProp));
        sendBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                service.sendText(messageTxt.getText());
                messageTxt.clear();
            }
        });
        hbox.getChildren().addAll(messageLbl, messageTxt, sendBtn);
        return hbox;
    }

    @Override
    public void stop() throws Exception {
        if (timer != null) {
            timer.stop();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }

    class EchoClientService extends Service<Session> {
        private Session session;
        @Override
        protected Task<Session> createTask() {
            Task<Session> task = new Task<Session>() {
                @Override
                protected Session call() throws Exception {
                    ClientManager client = ClientManager.createClient();
                    ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
                    URI uri = new URI("ws://localhost:8080/ws/echo");
                    session = client.connectToServer(new EchoClienEndpoint(queue), cec, uri);
                    return session;
                }
            };
            return task;
        }

        void sendText(String message) {
            if (session != null) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException ex) {
                    Logger.getLogger(EchoClientFX.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        void closeSession() {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException ex) {
                    Logger.getLogger(EchoClientFX.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
