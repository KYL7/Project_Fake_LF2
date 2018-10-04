package application;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class Role_Capoo_1 {
	private Client client;
	private ClientCenter clientCenter;
	private ImageView img_room;
	private Label label_room_headpicture;
	private int hp, speed;
	private double x, y;
	private String my_png = "role_1.png";

	public Role_Capoo_1(Client client, ClientCenter clientCenter, ImageView img_room, Label label_room_headpicture) {
		this.client = client;
		this.clientCenter = clientCenter;
		this.img_room = img_room;
		this.label_room_headpicture = label_room_headpicture;

		setImage();
		setLabel();
		setHp(1000);
		setSpeed();
	}

	private void setImage() {
		Platform.runLater(() -> {
			try {
				img_room = new ImageView("lock_role_1.png");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	public ImageView getImage() {
		return img_room;
	}

	private void setLabel() {
		Platform.runLater(() -> {
			try {
				label_room_headpicture.setGraphic(img_room);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	public Label getLabel() {
		return label_room_headpicture;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public int getHP() {
		return hp;
	}

	private void setSpeed() {
		speed = 2;
	}

	public int getSpeed() {
		return speed;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return y;
	}

	private void setGameMyImage() {

	}

	public String getGameMyImage() {
		return my_png;
	}
}
