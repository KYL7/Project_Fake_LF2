package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

public class Main extends Application {
	// region
	public Scene scene_main, scene_server;
	public GridPane gridpane_main_root, server_pane;
	public ImageView image_main_logo, image_server_logo;
	public Button button_main_client, button_main_server;
	public static TextArea ta;
	public AudioClip clip;
	private void main_init() {
		gridpane_main_root = new GridPane();
		gridpane_main_root.setPadding(new Insets(25, 25, 25, 25));
		server_pane = new GridPane();
		server_pane.setPadding(new Insets(25, 25, 25, 25));
		image_main_logo = new ImageView("logo.png");
		image_server_logo = new ImageView("logo.png");
		button_main_client = new Button("Client");
		button_main_server = new Button("Server");
		ta = new TextArea("Welcome to Server console mode : \n");
		ta.setEditable(false);
	}

	private void main_setupUI() {

		button_main_client.setPrefSize(100, 10);
		button_main_server.setPrefSize(100, 10);
		gridpane_main_root.setHgap(10);
		gridpane_main_root.setVgap(10);
		gridpane_main_root.add(image_main_logo, 1, 1, 10, 1);
		gridpane_main_root.add(button_main_client, 1, 10, 5, 1);
		gridpane_main_root.add(button_main_server, 10, 10, 5, 1);
		scene_main = new Scene(gridpane_main_root, 400, 300);
		scene_main.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		server_pane.setHgap(10);
		server_pane.setVgap(10);
		server_pane.add(image_server_logo, 1, 1, 10, 1);
		server_pane.add(ta, 1, 4, 10, 15);
		ta.setPrefHeight(400);
		scene_server = new Scene(server_pane, 400, 600);

	}

	private void main_setupListener(Stage primaryStage) {
		button_main_client.setOnAction(e -> {
			Client client = new Client(primaryStage);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					System.out.println("socket close");
					System.exit(0);
					try {
						client.sock.close();
						client.sock = null;
					} catch (IOException e) {
					}
					primaryStage.close();
				}
			});
		});
		button_main_server.setOnAction(e -> {
			primaryStage.setTitle("Server is running..當機是正常現象請不要關閉");
			clip.stop();
			Thread serverThread = new Thread(new Server());
			serverThread.start();
			primaryStage.setScene(scene_server);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					ta.appendText("Server is closing \n");
					System.exit(0);
					try {
						Server.closeSocket();
					} catch (IOException e) {
					}
					primaryStage.close();
				}
			});

		});

	}

	public void playmusic() {
		final Task task = new Task() {
			@Override
			protected Object call() throws Exception {
				clip = new AudioClip(getClass().getResource("victory.wav").toExternalForm());
				clip.setCycleCount(AudioClip.INDEFINITE);
				clip.play(1.0);
				return null;
			}
		};
		
		Thread thread = new Thread(task);
		thread.start();
		
	}

	public static void appendTa(String msg) {
		Platform.runLater(() -> {
			try {
				ta.appendText(msg + "\n");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	public void start(Stage primaryStage) {
		main_init();
		main_setupUI();
		main_setupListener(primaryStage);
		primaryStage.setTitle("Project_Fake_LF2");
		primaryStage.setScene(scene_main);
		primaryStage.setResizable(false);
		primaryStage.show();
		playmusic();
	}

	public static void main(String[] args) {

		Application.launch(args);
	}
	// endregion

}