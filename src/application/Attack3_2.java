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

/* �ϥΤ�k:
 * �ͦ�:
 * �ۤv-������L�����𤧫�Anew �X�@��Attack2 �[�J�l�uMap�ðe�X�T����Server�C
 * 
 * �O�H-�bClientCenter������l�u�X�{���T������Anew�X�@��Attack2�C
 * 		��l�u�[�J�@��Map<int,Attack2>�̭��A�e����int �񦬨쪺�l�u�s���C
 * 
 * ���`:
 * �ۤv-��l�u�o�͸I���A�o�X�ۤv���˸�l�u���`���T���A��l�u�qMap���M��
 * �O�H-����l�u���`���T�����ɭԡA�qMap�h��Key=���`�l�u�и�������A�I�s����Attack2.bulletDeath();
 *		 �M��������Attack2=null;(Option: System.gc();��ĳ�t�Υh�M���U��)
 * 		��<Key,Value>=<���`�l�u�s��,�l�u>�qMap�̭��h���C
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
		// ���l�u���@�|��
		bulletFly();

	}

	public void bulletFly() {

		// �o�Ӱʵe��������
		timeline.setCycleCount(55);
		// Duration�C���h�֬�,�M��Ĳ�o�ƥ�
		final KeyFrame kf = new KeyFrame(Duration.millis(30), new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				// ����collision
				collision(imv.getTranslateX(), imv.getTranslateY());
				if (toldDeath) {
					// System.out.println("toldDeath");
				} else if (imv.getTranslateX() > (sizex * -1) && imv.getTranslateX() < sizex && !boom) {
					// �b��V��ɤ����ӥB���z��
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
					// �[�t�ʵe����
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
					// �C�C�����������ۤv�qsp����
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
				imv.setTranslateX(startx + role_size + atk_size - 5);// �_�l��m
			} else if (direction == -1) {
				imv.setTranslateX(startx - role_size - atk_size + 5);// �_�l��m
			}

			imv.setTranslateY(starty);// �_�l��m
			sp.getChildren().add(imv);
		});
	}

	public void collision(double x, double y) {
		// TODO: �����I��
		// �o�䪺�Ѽ�xy�O�l�u����m�A�]�����ܡAClient��m�ݭn�I�s
		// Client�x�s�ۤv����m�Ӹ�l�u�i��P�_
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