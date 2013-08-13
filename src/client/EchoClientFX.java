package client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;

/**
 * Client for the Echo Server
 * 
 * @author tomo
 */
public class EchoClientFX extends Application {
    private Session connectSession;
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
                ClientManager client = ClientManager.createClient();
                ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
                try {
                    URI uri = new URI("ws://localhost:8080/ws/echo");
                    // connect the echo server
                    connectSession = client.connectToServer(new EchoClienEndpoint(messageList), cec, uri);
                } catch (DeploymentException | URISyntaxException | IOException ex) {
                    Logger.getLogger(EchoClientFX.class.getName()).log(Level.SEVERE, null, ex);
                }
                connectionProp.setValue(true);
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
                try {
                    connectSession.close();
                    connectionProp.setValue(false);
                } catch (IOException ex) {
                    Logger.getLogger(EchoClientFX.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                try {
                    // send the message to the echo server
                    connectSession.getBasicRemote().sendText(messageTxt.getText());
                    messageTxt.clear();
                } catch (IOException ex) {
                    Logger.getLogger(EchoClientFX.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        hbox.getChildren().addAll(messageLbl, messageTxt, sendBtn);
        return hbox;
    }
    public static void main(String[] args) {
        launch();
    }
}
