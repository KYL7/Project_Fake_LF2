package application;

import java.awt.event.KeyAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

public class Client extends Application {
	public Scene scene_login, scene_room, scene_game;
	public Label label_login_ip, label_login_name, label_room_headpicture1, label_room_headpicture2,
			label_room_headpicture3, label_room_headpicture4, label_room_name1, label_room_name2, label_room_name3,
			label_room_name4, label_room_systemmessage, label_game_backgroundblood, label_game_backgroundsky,
			label_game_backgroundground, label_game_headpicture, label_game_character1, label_game_character2,
			label_game_character3, label_game_character4, label_game_name1, label_game_name2, label_game_name3,
			label_game_name4;
	public ImageView image_login_logo, image_room_player_1, image_room_player_2, image_room_player_3,
			image_room_player_4;
	public ProgressBar progressbar_game_blood, progressbar_game_characterblood1, progressbar_game_characterblood2,
			progressbar_game_characterblood3, progressbar_game_characterblood4;
	public EventHandler<KeyEvent> keyevent_game;
	public float myHp_max = 0, c1Hp_max = 0, c2Hp_max = 0, c3Hp_max = 0, c4Hp_max = 0;

	private ClientCenter clientCenter;
	private final int sence_width = 1024;
	private final int sence_height = 768;
	private Client client;
	public Socket sock;
	private GridPane gridpane_login_root, gridpane_room_root;
	private StackPane stackpane_game_root, stackpane_game_backgroundblood, stackpane_game_backgroundsky,
			stackpane_game_backgroundground;
	private TextField textfield_login_ip, textfield_login_name;
	private Button button_login_connect, button_login_exit, button_room_character1, button_room_character2,
			button_room_character3, button_room_character4, button_room_character5, button_room_ready;
	private HBox hbox_room_button;
	private String name;
	private float my_hp = 0, other_after_hp = 0;
	// login

	// region

	public Client(Stage primaryStage) {
		// TODO Auto-generated constructor stub
		start(primaryStage);
	}

	public void login_init() {
		gridpane_login_root = new GridPane();
		gridpane_login_root.setPadding(new Insets(25, 25, 25, 25));
		image_login_logo = new ImageView("logo.png");
		textfield_login_ip = new TextField("127.0.0.1");
		textfield_login_name = new TextField();
		label_login_ip = new Label("IP: ");
		label_login_name = new Label("Name: ");
		button_login_connect = new Button("Connect");
		button_login_exit = new Button("Exit");
	}

	public void login_setupUI() {
		label_login_ip.setPrefSize(100, 10);
		textfield_login_ip.setPrefSize(1100, 10);
		label_login_name.setPrefSize(100, 10);
		textfield_login_name.setPrefSize(1100, 10);
		button_login_connect.setPrefSize(100, 10);
		button_login_exit.setPrefSize(100, 10);
		gridpane_login_root.setHgap(10);
		gridpane_login_root.setVgap(10);
		gridpane_login_root.add(image_login_logo, 1, 1, 10, 1);
		gridpane_login_root.add(label_login_ip, 1, 2, 5, 1);
		gridpane_login_root.add(textfield_login_ip, 1, 3, 5, 1);
		gridpane_login_root.add(label_login_name, 6, 2, 5, 1);
		gridpane_login_root.add(textfield_login_name, 6, 3, 5, 1);
		gridpane_login_root.add(button_login_connect, 1, 10, 5, 1);
		gridpane_login_root.add(button_login_exit, 10, 10, 5, 1);
		scene_login = new Scene(gridpane_login_root, 400, 300);
		scene_login.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	}

	public void login_setupListener(Stage primaryStage) {
		button_login_connect.setOnAction(e -> {
			name = textfield_login_name.getText().toString();
			String ip = textfield_login_ip.getText().toString();
			clientCenter = new ClientCenter(client, sock, ip, name);
			Thread t = new Thread(clientCenter);
			t.start();
			room_init();
			room_setupUI();
			room_setupListener(primaryStage);
			primaryStage.setScene(scene_room);
		});
		button_login_exit.setOnAction(e -> {
			System.exit(1);
		});
	}
	// endregion

	// room

	// region

	private void room_init() {
		image_room_player_1 = new ImageView("first.jpg");
		image_room_player_2 = new ImageView("first.jpg");
		image_room_player_3 = new ImageView("first.jpg");
		image_room_player_4 = new ImageView("first.jpg");
		label_room_headpicture1 = new Label();
		label_room_headpicture2 = new Label();
		label_room_headpicture3 = new Label();
		label_room_headpicture4 = new Label();
		label_room_name1 = new Label();
		label_room_name2 = new Label();
		label_room_name3 = new Label();
		label_room_name4 = new Label();
		label_room_systemmessage = new Label("請選擇角色...");
		hbox_room_button = new HBox();
		button_room_character1 = new Button();
		button_room_character2 = new Button();
		button_room_character3 = new Button();
		button_room_character4 = new Button();
		button_room_character5 = new Button();
		button_room_ready = new Button("Ready");
		gridpane_room_root = new GridPane();
		gridpane_room_root.setPadding(new Insets(25, 25, 25, 25));
	}

	private void room_reinit(Label label_room_name1, Label label_room_name2, Label label_room_name3,
			Label label_room_name4) {
		image_room_player_1 = new ImageView("role_1.png");
		image_room_player_2 = new ImageView("role_1.png");
		image_room_player_3 = new ImageView("role_1.png");
		image_room_player_4 = new ImageView("role_1.png");
		label_room_headpicture1 = new Label();
		label_room_headpicture2 = new Label();
		label_room_headpicture3 = new Label();
		label_room_headpicture4 = new Label();
		this.label_room_name1 = label_room_name1;
		this.label_room_name2 = label_room_name2;
		this.label_room_name3 = label_room_name3;
		this.label_room_name4 = label_room_name4;
		label_room_systemmessage = new Label("請選擇角色...");
		hbox_room_button = new HBox();
		button_room_character1 = new Button();
		button_room_character2 = new Button();
		button_room_character3 = new Button();
		button_room_character4 = new Button();
		button_room_character5 = new Button();
		button_room_ready = new Button("Ready");
		gridpane_room_root = new GridPane();
		gridpane_room_root.setPadding(new Insets(25, 25, 25, 25));
	}

	private void room_setupUI() {
		gridpane_room_root.setHgap(10);
		gridpane_room_root.setVgap(10);
		label_room_headpicture1.setGraphic(image_room_player_1);
		label_room_headpicture2.setGraphic(image_room_player_2);
		label_room_headpicture3.setGraphic(image_room_player_3);
		label_room_headpicture4.setGraphic(image_room_player_4);
		label_room_headpicture1.setPrefSize(1100, 100);
		label_room_headpicture2.setPrefSize(1100, 100);
		label_room_headpicture3.setPrefSize(1100, 100);
		label_room_headpicture4.setPrefSize(1100, 100);
		label_room_name1.setPrefSize(150, 30);
		label_room_name2.setPrefSize(150, 30);
		label_room_name3.setPrefSize(150, 30);
		label_room_name4.setPrefSize(150, 30);
		button_room_character1.setPrefSize(50, 50);
		button_room_character2.setPrefSize(50, 50);
		button_room_character3.setPrefSize(50, 50);
		button_room_character4.setPrefSize(50, 50);
		button_room_character5.setPrefSize(50, 50);
		button_room_ready.setPrefSize(100, 30);
		label_room_systemmessage.setPrefSize(300, 30);

		button_room_character1.setGraphic(new ImageView("button_role_1.png"));
		button_room_character2.setGraphic(new ImageView("button_role_2.png"));
		button_room_character3.setGraphic(new ImageView("button_role_3.png"));
		button_room_character4.setGraphic(new ImageView("button_role_4.png"));
		button_room_character5.setGraphic(new ImageView("button_role_5.png"));

		hbox_room_button.setPadding(new Insets(25, 25, 25, 25));
		hbox_room_button.setSpacing(10);
		hbox_room_button.getChildren().add(button_room_character1);
		hbox_room_button.getChildren().add(button_room_character2);
		hbox_room_button.getChildren().add(button_room_character3);
		hbox_room_button.getChildren().add(button_room_character4);
		hbox_room_button.getChildren().add(button_room_character5);

		gridpane_room_root.add(label_room_headpicture1, 1, 1, 1, 1);
		gridpane_room_root.add(label_room_headpicture2, 3, 1, 1, 1);
		gridpane_room_root.add(label_room_headpicture3, 5, 1, 1, 1);
		gridpane_room_root.add(label_room_headpicture4, 7, 1, 1, 1);
		gridpane_room_root.add(label_room_name1, 1, 2, 1, 1);
		gridpane_room_root.add(label_room_name2, 3, 2, 1, 1);
		gridpane_room_root.add(label_room_name3, 5, 2, 1, 1);
		gridpane_room_root.add(label_room_name4, 7, 2, 1, 1);
		gridpane_room_root.add(hbox_room_button, 3, 5, 8, 1);
		gridpane_room_root.add(button_room_ready, 1, 6, 3, 1);
		gridpane_room_root.add(label_room_systemmessage, 4, 6, 6, 1);

		scene_room = new Scene(gridpane_room_root, 800, 600);
		scene_room.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	}

	private void room_setupListener(Stage primaryStage) {
		button_room_character1.setOnAction(e -> {
			clientCenter.selecteRole(1);
		});
		button_room_character2.setOnAction(e -> {
			clientCenter.selecteRole(2);
		});
		button_room_character3.setOnAction(e -> {
			clientCenter.selecteRole(3);
		});
		button_room_character4.setOnAction(e -> {
			clientCenter.selecteRole(4);
		});
		button_room_character5.setOnAction(e -> {
			clientCenter.selecteRole(5);
		});
		button_room_ready.setOnAction(e -> {
			clientCenter.lockRole(primaryStage);
			this.setDisable();
		});
	}

	// endregion
	public void toGame(Stage primaryStage) {
		Platform.runLater(() -> {
			try {
				game_init();
				clientCenter.setGameView();
				game_setupListener(primaryStage);
				primaryStage.setScene(scene_game);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private void setDisable() {
		button_room_character1.setDisable(true);
		button_room_character2.setDisable(true);
		button_room_character3.setDisable(true);
		button_room_character4.setDisable(true);
		button_room_character5.setDisable(true);
		button_room_ready.setDisable(true);
	}
	// game

	// region
	private void game_init() {
		stackpane_game_root = new StackPane();
		stackpane_game_backgroundblood = new StackPane();
		stackpane_game_backgroundground = new StackPane();
		stackpane_game_backgroundsky = new StackPane();
		label_game_backgroundblood = new Label();
		label_game_backgroundsky = new Label();
		label_game_backgroundground = new Label();
		label_game_headpicture = new Label();
		label_game_character1 = new Label();
		label_game_character2 = new Label();
		label_game_character3 = new Label();
		label_game_character4 = new Label();
		label_game_name1 = new Label();
		label_game_name2 = new Label();
		label_game_name3 = new Label();
		label_game_name4 = new Label();
		progressbar_game_blood = new ProgressBar(1);
		progressbar_game_characterblood1 = new ProgressBar(1);
		progressbar_game_characterblood2 = new ProgressBar(1);
		progressbar_game_characterblood3 = new ProgressBar(1);
		progressbar_game_characterblood4 = new ProgressBar(1);
	}

	public void game_setupUI(double c1_x, double c1_y, double c2_x, double c2_y, double c3_x, double c3_y, double c4_x,
			double c4_y, String c1_png, String c2_png, String c3_png, String c4_png, String my_png, String name1,
			String name2, String name3, String name4) {

		label_game_headpicture.setPrefSize(150, 100);
		// label_headpicture.setStyle("-fx-background-color: #33CCFF");
		label_game_headpicture.setTranslateX(-330);
		label_game_headpicture.setTranslateY(-285);
		ImageView imageview_headpicture = new ImageView(my_png);
		label_game_headpicture.setGraphic(imageview_headpicture);
		label_game_backgroundblood.setPrefSize(sence_width, sence_height / 12 * 3);
		label_game_backgroundblood.setStyle("-fx-background-color: #000000");
		label_game_backgroundblood.setTranslateX(0);
		label_game_backgroundblood.setTranslateY(-290);

		label_game_backgroundsky.setPrefSize(sence_width, sence_height / 12 * 4);
		// label_backgroundsky.setStyle("-fx-background-color: #77FFCC");
		label_game_backgroundsky.setTranslateX(0);
		label_game_backgroundsky.setTranslateY(-70);
		ImageView imageview_backgroundsky = new ImageView("sky4.jpg");
		label_game_backgroundsky.setGraphic(imageview_backgroundsky);

		label_game_backgroundground.setPrefSize(sence_width, sence_height / 12 * 5);
		// label_backgroundground.setStyle("-fx-background-color: #008800");
		label_game_backgroundground.setTranslateX(0);
		label_game_backgroundground.setTranslateY(220);
		ImageView imageview_backgroundground = new ImageView("ground4.jpg");
		label_game_backgroundground.setGraphic(imageview_backgroundground);

		progressbar_game_blood.setStyle("-fx-accent: red");
		progressbar_game_blood.setPrefSize(500, 25);
		progressbar_game_blood.setTranslateX(10);
		progressbar_game_blood.setTranslateY(-285);

		label_game_character1.setPrefSize(30, 30);
		label_game_character1.setTranslateX(c1_x);
		label_game_character1.setTranslateY(c1_y);
		ImageView imageview_character1 = new ImageView(c1_png);
		label_game_character1.setGraphic(imageview_character1);
		progressbar_game_characterblood1.setStyle("-fx-accent: red");
		progressbar_game_characterblood1.setPrefSize(100, 1);
		progressbar_game_characterblood1.setTranslateX(c1_x);
		progressbar_game_characterblood1.setTranslateY(c1_y - 30);
		label_game_name1.setPrefSize(100, 30);
		label_game_name1.setFont(Font.font("Arial", FontPosture.ITALIC, 22));
		label_game_name1.setAlignment(Pos.CENTER);
		label_game_name1.setTranslateX(c1_x);
		label_game_name1.setTranslateY(c1_y + 40);
		label_game_name1.setText(name1);

		label_game_character2.setPrefSize(30, 30);
		label_game_character2.setTranslateX(c2_x);
		label_game_character2.setTranslateY(c2_y);
		ImageView imageview_character2 = new ImageView(c2_png);
		label_game_character2.setGraphic(imageview_character2);
		progressbar_game_characterblood2.setStyle("-fx-accent: red");
		progressbar_game_characterblood2.setPrefSize(100, 1);
		progressbar_game_characterblood2.setTranslateX(c2_x);
		progressbar_game_characterblood2.setTranslateY(c2_y - 30);
		label_game_name2.setPrefSize(100, 30);
		label_game_name2.setFont(Font.font("Arial", FontPosture.ITALIC, 22));
		label_game_name2.setAlignment(Pos.CENTER);
		label_game_name2.setTranslateX(c2_x);
		label_game_name2.setTranslateY(c2_y + 40);
		label_game_name2.setText(name2);

		label_game_character3.setPrefSize(50, 50);
		label_game_character3.setTranslateX(c3_x);
		label_game_character3.setTranslateY(c3_y);
		ImageView imageview_character3 = new ImageView(c3_png);
		label_game_character3.setGraphic(imageview_character3);
		progressbar_game_characterblood3.setStyle("-fx-accent: red");
		progressbar_game_characterblood3.setPrefSize(100, 1);
		progressbar_game_characterblood3.setTranslateX(c3_x);
		progressbar_game_characterblood3.setTranslateY(c3_y - 30);
		label_game_name3.setPrefSize(100, 30);
		label_game_name3.setFont(Font.font("Arial", FontPosture.ITALIC, 22));
		label_game_name3.setAlignment(Pos.CENTER);
		label_game_name3.setTranslateX(c3_x);
		label_game_name3.setTranslateY(c3_y + 40);
		label_game_name3.setText(name3);

		label_game_character4.setPrefSize(50, 50);
		label_game_character4.setTranslateX(c4_x);
		label_game_character4.setTranslateY(c4_y);
		ImageView imageview_character4 = new ImageView(c4_png);
		label_game_character4.setGraphic(imageview_character4);
		progressbar_game_characterblood4.setStyle("-fx-accent: red");
		progressbar_game_characterblood4.setPrefSize(100, 1);
		progressbar_game_characterblood4.setTranslateX(c4_x);
		progressbar_game_characterblood4.setTranslateY(c4_y - 30);
		label_game_name4.setPrefSize(100, 30);
		label_game_name4.setFont(Font.font("Arial", FontPosture.ITALIC, 22));
		label_game_name4.setAlignment(Pos.CENTER);
		label_game_name4.setTranslateX(c4_x);
		label_game_name4.setTranslateY(c4_y + 40);
		label_game_name4.setText(name4);

		stackpane_game_root.getChildren().addAll(stackpane_game_backgroundblood, stackpane_game_backgroundsky,
				stackpane_game_backgroundground);
		stackpane_game_backgroundblood.getChildren().addAll(label_game_backgroundblood, label_game_headpicture,
				label_game_backgroundsky, label_game_backgroundground);
		stackpane_game_backgroundblood.getChildren().addAll(progressbar_game_blood);
		stackpane_game_backgroundground.getChildren().addAll(label_game_character1, progressbar_game_characterblood1,
				label_game_name1);
		stackpane_game_backgroundground.getChildren().addAll(label_game_character2, progressbar_game_characterblood2,
				label_game_name2);
		stackpane_game_backgroundground.getChildren().addAll(label_game_character3, progressbar_game_characterblood3,
				label_game_name3);
		stackpane_game_backgroundground.getChildren().addAll(label_game_character4, progressbar_game_characterblood4,
				label_game_name4);

		scene_game = new Scene(stackpane_game_root, sence_width, sence_height);
		scene_game.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	}

	public void game_setMyHP(int role, int type, ProgressBar myblood) {
		Platform.runLater(() -> {
			try {
				my_hp = ((clientCenter.switch_myRole_to_get_HP() - 50) / myHp_max);
				if (my_hp >= 0) {
					myblood.setProgress(my_hp);
					progressbar_game_blood.setProgress(my_hp);
					clientCenter.switch_myRole_to_set_HP((int) (my_hp * myHp_max));
				} else {
					myblood.setProgress(0);
					progressbar_game_blood.setProgress(0);
					clientCenter.death_method();
				}
				System.out.println("===================");
				System.out.println("my_hp: " + my_hp);
				System.out.println("type: " + clientCenter.switch_myRole_to_get_HP());
				System.out.println("===================");

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	public void game_setOtherHP(int Tid, int type, ProgressBar blood) {
		Platform.runLater(() -> {
			try {
				switch (Tid) {
				case 1:
					other_after_hp = (type - 50) / c1Hp_max;
					break;
				case 2:
					other_after_hp = (type - 50) / c2Hp_max;
					break;
				case 3:
					other_after_hp = (type - 50) / c3Hp_max;
					break;
				case 4:
					other_after_hp = (type - 50) / c4Hp_max;
					break;
				}

				if (other_after_hp >= 0) {
					blood.setProgress(other_after_hp);
				} else {
					blood.setProgress(0);
				}

				System.out.println("===================");
				System.out.println("other_after_hp: " + other_after_hp);
				System.out.println("type: " + type);
				System.out.println("===================");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

	}

	public StackPane return_game_backgroundground() {
		return stackpane_game_backgroundground;
	}

	private void game_setupListener(Stage stage) {
		keyevent_game = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				Platform.runLater(() -> {
					try {
						if (e.getCode() != null) {
							KeyCode k = e.getCode();
							if (k == KeyCode.UP) {
								clientCenter.moveup();
							} else if (k == KeyCode.DOWN) {
								clientCenter.movedown();
							} else if (k == KeyCode.LEFT) {
								clientCenter.moveleft();
							} else if (k == KeyCode.RIGHT) {
								clientCenter.moveright();
							} else if (k == KeyCode.S) {
								clientCenter.attack2_method();
								CD();
								delay_to_refreshCD();
							} else if (k == KeyCode.A) {
								clientCenter.attack_method();
								CD();
								delay_to_refreshCD();
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
			}
		};
		scene_game.setOnKeyPressed(keyevent_game);
	}

	public void delay_to_refreshCD() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				keyevent_game = new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent e) {
						Platform.runLater(() -> {
							try {
								if (e.getCode() != null) {
									KeyCode k = e.getCode();
									if (k == KeyCode.UP) {
										clientCenter.moveup();
									} else if (k == KeyCode.DOWN) {
										clientCenter.movedown();
									} else if (k == KeyCode.LEFT) {
										clientCenter.moveleft();
									} else if (k == KeyCode.RIGHT) {
										clientCenter.moveright();
									} else if (k == KeyCode.S) {
										clientCenter.attack2_method();
										CD();
										delay_to_refreshCD();
									} else if (k == KeyCode.A) {
										clientCenter.attack_method();
										CD();
										delay_to_refreshCD();
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						});
					}
				};
				scene_game.setOnKeyPressed(keyevent_game);
				this.cancel();
			}
		}, 1000);

	}

	public void CD() {
		keyevent_game = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				Platform.runLater(() -> {
					try {
						if (e.getCode() != null) {
							KeyCode k = e.getCode();
							if (k == KeyCode.UP) {
								clientCenter.moveup();
							} else if (k == KeyCode.DOWN) {
								clientCenter.movedown();
							} else if (k == KeyCode.LEFT) {
								clientCenter.moveleft();
							} else if (k == KeyCode.RIGHT) {
								clientCenter.moveright();
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
			}
		};
		scene_game.setOnKeyPressed(keyevent_game);
	}

	public void setLocation(Label role, Label name, ProgressBar blood, double X, double Y) {
		Platform.runLater(() -> {
			try {
				role.setTranslateX(X);
				role.setTranslateY(Y);
				blood.setTranslateX(X);
				blood.setTranslateY(Y - 40);
				name.setTranslateX(X);
				name.setTranslateY(Y + 40);
			} catch (Exception ex) {
				System.out.println(ex.toString());
			}
		});
	}

	public void toRoom(Stage primaryStage, Label label_room_name1, Label label_room_name2, Label label_room_name3,
			Label label_room_name4) {
		Platform.runLater(() -> {
			try {
				room_reinit(label_room_name1, label_room_name2, label_room_name3, label_room_name4);
				room_setupUI();
				room_setupListener(primaryStage);
				primaryStage.setScene(scene_room);
			} catch (Exception ex) {

			}
		});
	}

	// endregion

	// main

	// region
	public void start(Stage primaryStage) {
		client = this;
		login_init();
		login_setupUI();
		login_setupListener(primaryStage);
		primaryStage.setTitle("Capoo Battle");
		primaryStage.setScene(scene_login);
		primaryStage.setResizable(false);
		primaryStage.show();

	}
}