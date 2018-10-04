package application;

import java.util.Map;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;



public class Attack3 {
	public double startx, starty;
	public boolean hit = false;
	public Client client;
	public ClientCenter clientCenter;
	ImageView imv;
	Image imageArray[] = new Image[35];
	Timeline timeline;
	int direction;
	int count = 0;
	int frame=30;
	private double sizex = 500;
	private int bulletID = 0;
	private StackPane sp;
	private Map<Integer, Attack3> Attacklist;
	private double clientCenterStart_X, clientCenterStart_Y;
	private double role_size=25;
	private double atk_size=25;

	public Attack3(Client client, ClientCenter clientCenter, double x, double y, StackPane sp, int direction,
			int bulletID, Map<Integer, Attack3> Attacklist) {
		this.client = client;
		this.clientCenter = clientCenter;
		startx = x;
		starty = y;
		this.sp = sp;
		this.direction = direction;
		this.bulletID = bulletID;
		this.Attacklist = Attacklist;
		clientCenterStart_X = clientCenter.Start_X;
		clientCenterStart_Y = clientCenter.Start_Y;
		timeline = new Timeline();
		for (int i = 0; i < frame; i++) {
			imageArray[i] = new Image("atk3/atk" + Integer.toString(i + 1) + ".png");
		}
		setGraph(sp, direction);
		Atk();

	}

	public void Atk() {

		// 這個動畫播的次數
		timeline.setCycleCount(frame);
		// Duration每次多少秒,然後觸發事件
		final KeyFrame kf = new KeyFrame(Duration.millis(20), new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				// 偵測collision
				collision(imv.getTranslateX(), imv.getTranslateY());
				
					Platform.runLater(() -> {
						imv.setImage(imageArray[count]);
					});
					count++;

				
			}
		});
		timeline.getKeyFrames().add(kf);
		timeline.play();
		timeline.setOnFinished(e -> {
					sp.getChildren().remove(imv);
					System.out.println(bulletID + " death");
					Attacklist.remove(bulletID);
					// TODO: boom?
					if (hit == true) {
						clientCenter.atked_method(imv.getTranslateX() , imv.getTranslateY());
					}
		});

	}

	public void setGraph(StackPane sp, int getdirection) {

		if (getdirection == 1) {
			imv = new ImageView();
			imv.setImage(imageArray[0]);
		} else if (getdirection == -1) {
			imv = new ImageView();
			imv.setImage(imageArray[0]);
		}
		Platform.runLater(() -> {
			if (direction == 1) {
				imv.setTranslateX(startx + role_size+atk_size-5);// 起始位置
			} else if (direction == -1) {
				imv.setTranslateX(startx - role_size-atk_size+5);// 起始位置
			}

			imv.setTranslateY(starty);// 起始位置
			sp.getChildren().add(imv);
		});
	}

	public void collision(double x, double y) {
		// TODO: 偵測碰撞
		// 這邊的參數xy是子彈的位置，因為時變，Client位置需要呼叫
		// Client儲存自己的位置來跟子彈進行判斷
		if (x < clientCenterStart_X && x + atk_size >= clientCenterStart_X - role_size && direction == 1
				&& Math.abs(clientCenterStart_Y - y) <= role_size+atk_size) {
			hit = true;
		} else if (x > clientCenterStart_X && x - atk_size <= clientCenterStart_X + role_size && direction == -1
				&& Math.abs(clientCenterStart_Y - y) <= role_size+atk_size) {
			hit = true;
		} else {
			hit = false;
		}

	}

	public void bulletDeath() {
		// 讓ClientCenter調用子彈死亡
		// 加速讓動畫播完

	}
}