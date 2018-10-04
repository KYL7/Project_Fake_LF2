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

/* 使用方法:
 * 生成:
 * 自己-收到鍵盤的遠攻之後，new 出一個Attack2 加入子彈Map並送出訊息給Server。
 * 
 * 別人-在ClientCenter接受到子彈出現的訊息之後，new出一個Attack2。
 * 		把子彈加入一個Map<int,Attack2>裡面，前面的int 放收到的子彈編號。
 * 
 * 死亡:
 * 自己-跟子彈發生碰撞，發出自己受傷跟子彈死亡的訊息，把子彈從Map中清掉
 * 別人-收到子彈死亡的訊息的時候，從Map去找Key=死亡子彈標號的那格，呼叫那個Attack2.bulletDeath();
 *		 然後讓那個Attack2=null;(Option: System.gc();建議系統去清掃垃圾)
 * 		把<Key,Value>=<死亡子彈編號,子彈>從Map裡面去掉。
 */

public class Attack3_2 {
	public double startx, starty;
	public boolean boom = false;
	public Client client;
	public ClientCenter clientCenter;
	private int range = 20;
	ImageView imv;
	Image imageArray[] = new Image[30];
	Timeline timeline;
	int direction;
	int count = 0;
	int frame = 27;
	private double sizex = 500;
	private int bulletID = 0;
	private StackPane sp;
	private Map<Integer, Attack3_2> bulletlist;
	private double clientCenterStart_X, clientCenterStart_Y;
	private double role_size = 25;
	private double atk_size = 25;
	private boolean toldDeath = false;

	public Attack3_2(Client client, ClientCenter clientCenter, double x, double y, StackPane sp, int direction,
			int bulletID, Map<Integer, Attack3_2> bulletlist) {
		this.client = client;
		this.clientCenter = clientCenter;
		startx = x;
		starty = y;
		this.sp = sp;
		this.direction = direction;
		this.bulletID = bulletID;
		this.bulletlist = bulletlist;
		clientCenterStart_X = clientCenter.Start_X;
		clientCenterStart_Y = clientCenter.Start_Y;
		timeline = new Timeline();
		for (int i = 0; i < frame; i++) {
			imageArray[i] = new Image("atk3_2/atk" + Integer.toString(i + 1) + ".png");
		}
		setGraph(sp, direction);
		// 讓子彈飛一會兒
		bulletFly();

	}

	public void bulletFly() {

		// 這個動畫播的次數
		timeline.setCycleCount(55);
		// Duration每次多少秒,然後觸發事件
		final KeyFrame kf = new KeyFrame(Duration.millis(30), new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				// 偵測collision
				collision(imv.getTranslateX(), imv.getTranslateY());
				if (toldDeath) {
					// System.out.println("toldDeath");
				} else if (imv.getTranslateX() > (sizex * -1) && imv.getTranslateX() < sizex && !boom) {
					// 在橫向邊界之內而且未爆炸
					switch (direction) {
					case 1:
						imv.setImage(imageArray[count]);
						imv.setTranslateX(imv.getTranslateX() + range);
						break;
					case -1:
						imv.setImage(imageArray[count]);
						imv.setTranslateX(imv.getTranslateX() - range);
						break;
					}
					count++;
					if (count > frame) {
						count = 0;
					}
				} else {
					// 加速動畫播完
					timeline.setRate(100);

				}
			}
		});
		timeline.getKeyFrames().add(kf);
		timeline.play();
		timeline.setOnFinished(e -> {
			System.out.println(imv.getTranslateX());
			FadeTransition fade = new FadeTransition(Duration.millis(10.0D), imv);
			fade.setFromValue(1.0D);
			fade.setToValue(0.0D);
			fade.setOnFinished(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent t) {
					// 慢慢消失結束後把自己從sp移除
					sp.getChildren().remove(imv);
					System.out.println(bulletID + " death");
					bulletlist.remove(bulletID);
					// TODO: boom?
					if (boom == true && !toldDeath) {
						clientCenter.atk2ed_method(imv.getTranslateX(), imv.getTranslateY(), bulletID);
					}
					System.out.println(this.getClass().toString()+"fade play");
				}
			});
			fade.play();
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
				imv.setTranslateX(startx + role_size + atk_size - 5);// 起始位置
			} else if (direction == -1) {
				imv.setTranslateX(startx - role_size - atk_size + 5);// 起始位置
			}

			imv.setTranslateY(starty);// 起始位置
			sp.getChildren().add(imv);
		});
	}

	public void collision(double x, double y) {
		// TODO: 偵測碰撞
		// 這邊的參數xy是子彈的位置，因為時變，Client位置需要呼叫
		// Client儲存自己的位置來跟子彈進行判斷
		if (x < clientCenterStart_X && x + 12.5 >= clientCenterStart_X - 25 && direction == 1
				&& Math.abs(clientCenterStart_Y - y) <= 37.5) {
			boom = true;
		} else if (x > clientCenterStart_X && x - 12.5 <= clientCenterStart_X + 25 && direction == -1
				&& Math.abs(clientCenterStart_Y - y) <= 37.5) {
			boom = true;
		} else {
			boom = false;
		}

	}

	public void bulletDeath(double bulletdeathX, double bulletdeathY) {
		Platform.runLater(() -> {
			try {
				imv.setTranslateX(bulletdeathX);
			} catch (Exception ex) {
				System.out.println(ex.toString());
			}
		});
		System.out.println("bulletDeath");
		boom = true;
		toldDeath = true;
		timeline.setRate(100);

	}
}