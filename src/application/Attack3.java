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

		// �o�Ӱʵe��������
		timeline.setCycleCount(frame);
		// Duration�C���h�֬�,�M��Ĳ�o�ƥ�
		final KeyFrame kf = new KeyFrame(Duration.millis(20), new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				// ����collision
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
				imv.setTranslateX(startx + role_size+atk_size-5);// �_�l��m
			} else if (direction == -1) {
				imv.setTranslateX(startx - role_size-atk_size+5);// �_�l��m
			}

			imv.setTranslateY(starty);// �_�l��m
			sp.getChildren().add(imv);
		});
	}

	public void collision(double x, double y) {
		// TODO: �����I��
		// �o�䪺�Ѽ�xy�O�l�u����m�A�]�����ܡAClient��m�ݭn�I�s
		// Client�x�s�ۤv����m�Ӹ�l�u�i��P�_
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
		// ��ClientCenter�եΤl�u���`
		// �[�t���ʵe����

	}
}