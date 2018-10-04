package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClientCenter implements Runnable {
	public static double Start_X = 0, Start_Y = 0; // 會跟著腳色移動即時更新X，Y

	private static Map<Integer, Attack1> attacklist = new HashMap<Integer, Attack1>();
	private static Map<Integer, Attack2> attack2list = new HashMap<Integer, Attack2>();
	private static Map<Integer, Attack3> attack3list = new HashMap<Integer, Attack3>();
	private static Map<Integer, Attack4> attack4list = new HashMap<Integer, Attack4>();
	private static Map<Integer, Attack5> attack5list = new HashMap<Integer, Attack5>();
	private static Map<Integer, Attack1_2> bulletlist = new HashMap<Integer, Attack1_2>();
	private static Map<Integer, Attack2_2> bullet2list = new HashMap<Integer, Attack2_2>();
	private static Map<Integer, Attack3_2> bullet3list = new HashMap<Integer, Attack3_2>();
	private static Map<Integer, Attack4_2> bullet4list = new HashMap<Integer, Attack4_2>();
	private static Map<Integer, Attack5_2> bullet5list = new HashMap<Integer, Attack5_2>();
	private static int bulletcounter, Offset_bullet, state, Tid, myTid, source, dest, type, role, my_role,
			role_data[] = new int[4], direction, direction_atk = 1, tmp = 0, my_hp;
	private static String function, Stype, myName, ta[], name[] = new String[4], c1_png = "", c2_png = "", c3_png = "",
			c4_png = "", my_png = "";
	private static double X, Y, position[][] = new double[4][2], my_speed, tmpX, tmpY;
	private static Socket sock;
	private static BufferedReader reader;
	private static PrintStream writer;
	private static Client client;
	private static Stage stage;
	private static Role_Capoo_1 my_Capoo_1 = null;
	private static Role_Capoo_2 my_Capoo_2 = null;
	private static Role_Capoo_3 my_Capoo_3 = null;
	private static Role_Capoo_4 my_Capoo_4 = null;
	private static Role_Capoo_5 my_Capoo_5 = null;
	private boolean Collision = false;
	double wh = 30;
	double x1 = 0;
	double y1 = 0;
	double x2 = 0;
	double y2 = 0;
	double x3 = 0;
	double y3 = 0;
	double x4 = 0;
	double y4 = 0;

	public ClientCenter(Client client, Socket socket, String ip, String name) {
		try {
			EstablishConnection(ip, 8888);
			this.client = client;
			myName = name;
			bulletcounter = 0;
		} catch (Exception ex) {
			System.out.println("連接失敗 in ClientCenter");
		}
	}

	private void EstablishConnection(String ip, int port) {
		try {
			// 請求建立連線
			sock = new Socket(ip, port);
			// 建立I/O資料流
			InputStreamReader streamReader =
					// 取得Socket的輸入資料流
					new InputStreamReader(sock.getInputStream());
			// 放入暫存區
			reader = new BufferedReader(streamReader);
			// 取得Socket的輸出資料流

			writer = new PrintStream(sock.getOutputStream());
			// 連線成功
			System.out.println("網路建立-連線成功");

		} catch (IOException ex) {
			System.out.println("建立連線失敗");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String message;
		try {
			while ((message = reader.readLine()) != null) {
				System.out.println("收到" + message);
				if (message.contains("#")) {
					// 這是因為暫時測試用的的client會把名字:打在前面
					decoder(message);
					handle();
					if (Tid == -1) {
						initConnection();
						Offset_bullet = myTid * 10000;
						writermsg();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Event Handler
	// region
	private void handle() {
		if (state == 0) {
			switch_function_to_case_state_0();
		} else if (state == 1) {
			switch_function_to_case_state_1();
		} else if (state == 2) {
			switch_function_to_case_state_2();
		}
	}

	public void selecteRole(int role) {
		Platform.runLater(() -> {
			try {
				this.role = role;
				my_role = role;
				switch_myTid_to_switch_role_to_select();
			} catch (Exception ex) {
				System.out.println("送出資料失敗");
			}
		});
	}

	private void selectedRole(String png, Label label) {
		ImageView image = new ImageView(png);
		label.setGraphic(image);
	}

	public void lockRole(Stage primaryStage) {
		stage = primaryStage;
		Platform.runLater(() -> {
			try {
				client.label_room_systemmessage.setText("準備中...");
				role_data[myTid - 1] = role;
				switch_myTid_to_switch_role_to_lock();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void lockedRole(String png, Label label) {
		ImageView image = new ImageView(png);
		Platform.runLater(() -> {
			label.setGraphic(image);
		});
	}

	// endregion
	// method
	// region

	private void select_role_method(ImageView image, String png, Label label, int role) {
		image = new ImageView(png);
		label.setGraphic(image);
	}

	public void setGameView() {

		switch_myRole_to_get_GameMyImage();
		switch_roledata_to_set_other_game_image();
		try {
			client.game_setupUI(position[0][0], position[0][1], position[1][0], position[1][1], position[2][0],
					position[2][1], position[3][0], position[3][1], c1_png, c2_png, c3_png, c4_png, my_png, name[0],
					name[1], name[2], name[3]);
			System.out.println("name[0]: " + name[0]);
			System.out.println("name[1]: " + name[1]);
			System.out.println("name[2]: " + name[2]);
			System.out.println("name[3]: " + name[3]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void bulletdeath(int bulletID, double bulletx, double bullety) {
		initbulletdeath(bulletID, bulletx, bullety);
		writermsg();
	}

	public void attack_method() {
		bulletcounter++;
		initAttack(Offset_bullet + bulletcounter);
		writermsg();
		switch (my_role) {
		case 1:
			Attack1 attack = new Attack1(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, attacklist);
			attacklist.put(Offset_bullet + bulletcounter, attack);
			attack = null;
			break;
		case 2:
			Attack2 attack2 = new Attack2(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, attack2list);
			attack2list.put(Offset_bullet + bulletcounter, attack2);
			attack2 = null;
			break;
		case 3:
			Attack3 attack3 = new Attack3(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, attack3list);
			attack3list.put(Offset_bullet + bulletcounter, attack3);
			attack3 = null;
			break;
		case 4:
			Attack4 attack4 = new Attack4(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, attack4list);
			attack4list.put(Offset_bullet + bulletcounter, attack4);
			attack4 = null;
			break;
		case 5:
			Attack5 attack5 = new Attack5(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, attack5list);
			attack5list.put(Offset_bullet + bulletcounter, attack5);
			attack5 = null;
			break;
		}

		System.gc();
		if (bulletcounter == 9999) {
			bulletcounter = 0;
		}

	}

	public void attack2_method() {
		bulletcounter++;
		initAttack2(Offset_bullet + bulletcounter);
		writermsg();

		switch (my_role) {
		case 1:
			Attack1_2 attack1_2 = new Attack1_2(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, bulletlist);
			bulletlist.put(Offset_bullet + bulletcounter, attack1_2);
			attack1_2 = null;
			break;
		case 2:
			Attack2_2 attack2_2 = new Attack2_2(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, bullet2list);
			bullet2list.put(Offset_bullet + bulletcounter, attack2_2);
			attack2_2 = null;
			break;
		case 3:
			Attack3_2 attack3_2 = new Attack3_2(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, bullet3list);
			bullet3list.put(Offset_bullet + bulletcounter, attack3_2);
			attack3_2 = null;
			break;
		case 4:
			Attack4_2 attack4_2 = new Attack4_2(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, bullet4list);
			bullet4list.put(Offset_bullet + bulletcounter, attack4_2);
			attack4_2 = null;
			break;
		case 5:
			Attack5_2 attack5_2 = new Attack5_2(client, this, Start_X, Start_Y, client.return_game_backgroundground(),
					direction_atk, Offset_bullet + bulletcounter, bullet5list);
			bullet5list.put(Offset_bullet + bulletcounter, attack5_2);
			attack5_2 = null;
			break;
		}

		System.gc();
		if (bulletcounter == 9999) {
			bulletcounter = 0;
		}
	}

	public void atked_method(double x, double y) {
		my_hp -= 50;
		switch (myTid) {
		case 1:
			client.game_setMyHP(my_role, type, client.progressbar_game_characterblood1);
			break;
		case 2:
			client.game_setMyHP(my_role, type, client.progressbar_game_characterblood2);
			break;
		case 3:
			client.game_setMyHP(my_role, type, client.progressbar_game_characterblood3);
			break;
		case 4:
			client.game_setMyHP(my_role, type, client.progressbar_game_characterblood4);
			break;
		}
		initAtked();
		writermsg();

	}

	public void atk2ed_method(double x, double y, int bulletID) {
		bulletdeath(bulletID, x, y);
		my_hp -= 50;
		switch (myTid) {
		case 1:
			client.game_setMyHP(my_role, type, client.progressbar_game_characterblood1);
			break;
		case 2:
			client.game_setMyHP(my_role, type, client.progressbar_game_characterblood2);
			break;
		case 3:
			client.game_setMyHP(my_role, type, client.progressbar_game_characterblood3);
			break;
		case 4:
			client.game_setMyHP(my_role, type, client.progressbar_game_characterblood4);
			break;
		}

		initAtk2ed();
		writermsg();

	}

	private void win_method() {

	}

	public void death_method() {
		switch (myTid) {
		// 觸發Tid
		// TODO: 依照Tid設定誰死亡
		case 1:
			client.label_game_character1.setDisable(true);
			client.label_game_name1.setDisable(true);
			client.progressbar_game_characterblood1.setDisable(true);
			break;
		case 2:
			client.label_game_character2.setDisable(true);
			client.label_game_name2.setDisable(true);
			client.progressbar_game_characterblood2.setDisable(true);
			break;
		case 3:
			client.label_game_character3.setDisable(true);
			client.label_game_name3.setDisable(true);
			client.progressbar_game_characterblood3.setDisable(true);
			break;
		case 4:
			client.label_game_character4.setDisable(true);
			client.label_game_name4.setDisable(true);
			client.progressbar_game_characterblood4.setDisable(true);
			break;
		}
		initDeath();
		writermsg();
	}

	public void moveup() {

		Collision_moveup();
		switch_myRole_to_set_Y(Start_Y);
		switch_myTid_to_moveup();
		initMoveUp(Start_X, Start_Y);
		writermsg();
	}

	public void movedown() {
		Collision_movedown();
		switch_myRole_to_set_Y(Start_Y);
		switch_myTid_to_movedown();
		initMoveDown(Start_X, Start_Y);
		writermsg();
	}

	public void moveleft() {
		Collision_moveleft();
		switch_myRole_to_set_X(Start_X);
		switch_myTid_to_moveleft();
		initMoveLeft(Start_X, Start_Y);
		writermsg();
		direction_atk = -1;
	}

	public void moveright() {
		Collision_moveright();
		switch_myRole_to_set_X(Start_X);
		switch_myTid_to_moveright();
		initMoveRight(Start_X, Start_Y);
		writermsg();
		direction_atk = 1;
	}

	private void Collision_moveup() {
		// TODO Auto-generated method stub
		x1 = client.label_game_character1.getTranslateX();
		y1 = client.label_game_character1.getTranslateY();
		x2 = client.label_game_character2.getTranslateX();
		y2 = client.label_game_character2.getTranslateY();
		x3 = client.label_game_character3.getTranslateX();
		y3 = client.label_game_character3.getTranslateY();
		x4 = client.label_game_character4.getTranslateX();
		y4 = client.label_game_character4.getTranslateY();
		switch (myTid) {
		case 1:
			if (x1 >= x2 - wh && x1 <= x2 + wh && y1 - my_speed >= y2 && y1 - my_speed <= y2 + wh) {
				Collision = true;
			} else if (x1 >= x3 - wh && x1 <= x3 + wh && y1 - my_speed >= y3 && y1 - my_speed <= y3 + wh) {
				Collision = true;
			} else if (x1 >= x4 - wh && x1 <= x4 + wh && y1 - my_speed >= y4 && y1 - my_speed <= y4 + wh) {
				Collision = true;
			} else if (Start_Y - my_speed < 76) {
				Start_Y = 76;
			} else {
				Start_Y -= my_speed;
			}
			break;
		case 2:
			if (x2 >= x1 - wh && x2 <= x1 + wh && y2 - my_speed >= y1 && y2 - my_speed <= y1 + wh) {
				Collision = true;
			} else if (x2 >= x3 - wh && x2 <= x3 + wh && y2 - my_speed >= y3 && y2 - my_speed <= y3 + wh) {
				Collision = true;
			} else if (x2 >= x4 - wh && x2 <= x4 + wh && y2 - my_speed >= y4 && y2 - my_speed <= y4 + wh) {
				Collision = true;
			} else if (Start_Y - my_speed < 76) {
				Start_Y = 76;
			} else {
				Start_Y -= my_speed;
			}
			break;
		case 3:
			if (x3 >= x2 - wh && x3 <= x2 + wh && y3 - my_speed >= y2 && y3 - my_speed <= y2 + wh) {
				Collision = true;
			} else if (x3 >= x1 - wh && x3 <= x1 + wh && y3 - my_speed >= y1 && y3 - my_speed <= y1 + wh) {
				Collision = true;
			} else if (x3 >= x4 - wh && x3 <= x4 + wh && y3 - my_speed >= y4 && y3 - my_speed <= y4 + wh) {
				Collision = true;
			} else if (Start_Y - my_speed < 76) {
				Start_Y = 76;
			} else {
				Start_Y -= my_speed;
			}
			break;
		case 4:
			if (x4 >= x2 - wh && x4 <= x2 + wh && y4 - my_speed >= y2 && y4 - my_speed <= y2 + wh) {
				Collision = true;
			} else if (x4 >= x1 - wh && x4 <= x1 + wh && y4 - my_speed >= y1 && y4 - my_speed <= y1 + wh) {
				Collision = true;
			} else if (x4 >= x3 - wh && x4 <= x3 + wh && y4 - my_speed >= y3 && y4 - my_speed <= y3 + wh) {
				Collision = true;
			} else if (Start_Y - my_speed < 76) {
				Start_Y = 76;
			} else {
				Start_Y -= my_speed;
			}
			break;
		}
	}

	private void Collision_movedown() {
		// TODO Auto-generated method stub
		x1 = client.label_game_character1.getTranslateX();
		y1 = client.label_game_character1.getTranslateY();
		x2 = client.label_game_character2.getTranslateX();
		y2 = client.label_game_character2.getTranslateY();
		x3 = client.label_game_character3.getTranslateX();
		y3 = client.label_game_character3.getTranslateY();
		x4 = client.label_game_character4.getTranslateX();
		y4 = client.label_game_character4.getTranslateY();
		switch (myTid) {
		case 1:
			if (x1 >= x2 - wh && x1 <= x2 + wh && y1 + my_speed >= y2 - wh && y1 + my_speed <= y2) {
				Collision = true;
			} else if (x1 >= x3 - wh && x1 <= x3 + wh && y1 + my_speed >= y3 - wh && y1 + my_speed <= y3) {
				Collision = true;
			} else if (x1 >= x4 - wh && x1 <= x4 + wh && y1 + my_speed >= y4 - wh && y1 + my_speed <= y4) {
				Collision = true;
			} else if (Start_Y + my_speed > 362) {
				Start_Y = 362;
			} else {
				Start_Y += my_speed;
			}
			break;
		case 2:
			if (x2 >= x1 - wh && x2 <= x1 + wh && y2 + my_speed >= y1 - wh && y2 + my_speed <= y1) {
				Collision = true;
			} else if (x2 >= x3 - wh && x2 <= x3 + wh && y2 + my_speed >= y3 - wh && y2 + my_speed <= y3) {
				Collision = true;
			} else if (x2 >= x4 - wh && x2 <= x4 + wh && y2 + my_speed >= y4 - wh && y2 + my_speed <= y4) {
				Collision = true;
			} else if (Start_Y + my_speed > 362) {
				Start_Y = 362;
			} else {
				Start_Y += my_speed;
			}
			break;
		case 3:
			if (x3 >= x2 - +wh && x3 <= x2 + wh && y3 + my_speed >= y2 - wh && y3 + my_speed <= y2) {
				Collision = true;
			} else if (x3 >= x1 - wh && x3 <= x1 + wh && y3 + my_speed >= y1 - wh && y3 + my_speed <= y1) {
				Collision = true;
			} else if (x3 >= x4 - wh && x3 <= x4 + wh && y3 + my_speed >= y4 - wh && y3 + my_speed <= y4) {
				Collision = true;
			} else if (Start_Y + my_speed > 362) {
				Start_Y = 362;
			} else {
				Start_Y += my_speed;
			}
			break;
		case 4:
			if (x4 >= x2 - +wh && x4 <= x2 + wh && y4 + my_speed >= y2 - wh && y4 + my_speed <= y2) {
				Collision = true;
			} else if (x4 >= x1 - wh && x4 <= x1 + wh && y4 + my_speed >= y1 - wh && y4 + my_speed <= y1) {
				Collision = true;
			} else if (x4 >= x3 - wh && x4 <= x3 + wh && y4 + my_speed >= y3 - wh && y4 + my_speed <= y3) {
				Collision = true;
			} else if (Start_Y + my_speed > 362) {
				Start_Y = 362;
			} else {
				Start_Y += my_speed;
			}
			break;
		}
	}

	private void Collision_moveright() {
		x1 = client.label_game_character1.getTranslateX();
		y1 = client.label_game_character1.getTranslateY();
		x2 = client.label_game_character2.getTranslateX();
		y2 = client.label_game_character2.getTranslateY();
		x3 = client.label_game_character3.getTranslateX();
		y3 = client.label_game_character3.getTranslateY();
		x4 = client.label_game_character4.getTranslateX();
		y4 = client.label_game_character4.getTranslateY();
		switch (myTid) {
		case 1:
			if (x1 + my_speed >= x2 - wh && x1 + my_speed <= x2 && y1 >= y2 - wh && y1 <= y2 + wh) {
				Collision = true;
			} else if (x1 + my_speed >= x3 - wh && x1 + my_speed <= x3 && y1 >= y3 - wh && y1 <= y3 + wh) {
				Collision = true;
			} else if (x1 + my_speed >= x4 - wh && x1 + my_speed <= x4 && y1 >= y4 - wh && y1 <= y4 + wh) {
				Collision = true;
			} else if (Start_X + my_speed > 492) {
				Start_X = 492;
			} else {
				Start_X += my_speed;
			}
			break;
		case 2:
			if (x2 + my_speed >= x1 - wh && x2 + my_speed <= x1 && y2 >= y1 - wh && y2 <= y1 + wh) {
				Collision = true;
			} else if (x2 + my_speed >= x3 - wh && x2 + my_speed <= x3 && y2 >= y3 - wh && y2 <= y3 + wh) {
				Collision = true;
			} else if (x2 + my_speed >= x4 - wh && x2 + my_speed <= x4 && y2 >= y4 - wh && y2 <= y4 + wh) {
				Collision = true;
			} else if (Start_X + my_speed > 492) {
				Start_X = 492;
			} else {
				Start_X += my_speed;
			}
			break;
		case 3:
			if (x3 + my_speed >= x2 - wh && x3 + my_speed <= x2 && y3 >= y2 - wh && y3 <= y2 + wh) {
				Collision = true;
			} else if (x3 + my_speed >= x1 - wh && x3 + my_speed <= x1 && y3 >= y1 - wh && y3 <= y1 + wh) {
				Collision = true;
			} else if (x3 + my_speed >= x4 - wh && x3 + my_speed <= x4 && y3 >= y4 - wh && y3 <= y4 + wh) {
				Collision = true;
			} else if (Start_X + my_speed > 492) {
				Start_X = 492;
			} else {
				Start_X += my_speed;
			}
			break;
		case 4:
			if (x4 + my_speed >= x2 - wh && x4 + my_speed <= x2 && y4 >= y2 - wh && y4 <= y2 + wh) {
				Collision = true;
			} else if (x4 + my_speed >= x3 - wh && x4 + my_speed <= x3 && y4 >= y3 - wh && y4 <= y3 + wh) {
				Collision = true;
			} else if (x4 + my_speed >= x1 - wh && x4 + my_speed <= x1 && y4 >= y1 - wh && y4 <= y1 + wh) {
				Collision = true;
			} else if (Start_X + my_speed > 492) {
				Start_X = 492;
			} else {
				Start_X += my_speed;
			}
			break;
		}
	}

	private void Collision_moveleft() {
		x1 = client.label_game_character1.getTranslateX();
		y1 = client.label_game_character1.getTranslateY();
		x2 = client.label_game_character2.getTranslateX();
		y2 = client.label_game_character2.getTranslateY();
		x3 = client.label_game_character3.getTranslateX();
		y3 = client.label_game_character3.getTranslateY();
		x4 = client.label_game_character4.getTranslateX();
		y4 = client.label_game_character4.getTranslateY();
		switch (myTid) {
		case 1:
			if (x1 - my_speed >= x2 && x1 - my_speed <= x2 + wh && y1 >= y2 - wh && y1 <= y2 + wh) {
				Collision = true;
			} else if (x1 - my_speed >= x3 && x1 - my_speed <= x3 + wh && y1 >= y3 - wh && y1 <= y3 + wh) {
				Collision = true;
			} else if (x1 - my_speed >= x4 && x1 - my_speed <= x4 + wh && y1 >= y4 - wh && y1 <= y4 + wh) {
				Collision = true;
			} else if (Start_X - my_speed < -492) {
				Start_X = -492;
			} else {
				Start_X -= my_speed;
			}
			break;
		case 2:
			if (x2 - my_speed >= x1 && x2 - my_speed <= x1 + wh && y2 >= y1 - wh && y2 <= y1 + wh) {
				Collision = true;
			} else if (x2 - my_speed >= x3 && x2 - my_speed <= x3 + wh && y2 >= y3 - wh && y2 <= y3 + wh) {
				Collision = true;
			} else if (x2 - my_speed >= x4 && x2 - my_speed <= x4 + wh && y2 >= y4 - wh && y2 <= y4 + wh) {
				Collision = true;
			} else if (Start_X - my_speed < -492) {
				Start_X = -492;
			} else {
				Start_X -= my_speed;
			}
			break;
		case 3:
			if (x3 - my_speed >= x1 && x3 - my_speed <= x1 + wh && y3 >= y1 - wh && y3 <= y1 + wh) {
				Collision = true;
			} else if (x3 - my_speed >= x2 && x3 - my_speed <= x2 + wh && y3 >= y2 - wh && y3 <= y2 + wh) {
				Collision = true;
			} else if (x3 - my_speed >= x4 && x3 - my_speed <= x4 + wh && y3 >= y4 - wh && y3 <= y4 + wh) {
				Collision = true;
			} else if (Start_X - my_speed < -492) {
				Start_X = -492;
			} else {
				Start_X -= my_speed;
			}
			break;
		case 4:
			if (x4 - my_speed >= x1 && x4 - my_speed <= x1 + wh && y4 >= y1 - wh && y4 <= y1 + wh) {
				Collision = true;
			} else if (x4 - my_speed >= x2 && x4 - my_speed <= x2 + wh && y4 >= y2 - wh && y4 <= y2 + wh) {
				Collision = true;
			} else if (x4 - my_speed >= x3 && x4 - my_speed <= x3 + wh && y4 >= y3 - wh && y4 <= y3 + wh) {
				Collision = true;
			} else if (Start_X - my_speed < -492) {
				Start_X = -492;
			} else {
				Start_X -= my_speed;
			}
			break;
		}
	}
	// endregion
	// New Role
	// region

	private void new_Role_1(Client client, ClientCenter clientCenter, ImageView img_room,
			Label label_room_headpicture) {
		my_Capoo_1 = new Role_Capoo_1(client, clientCenter, img_room, label_room_headpicture);
		my_hp = my_Capoo_1.getHP();
		client.myHp_max = my_Capoo_1.getHP();
		my_speed = my_Capoo_1.getSpeed();
	}

	private void new_Role_2(Client client, ClientCenter clientCenter, ImageView img_room,
			Label label_room_headpicture) {
		my_Capoo_2 = new Role_Capoo_2(client, clientCenter, img_room, label_room_headpicture);
		my_hp = my_Capoo_2.getHP();
		client.myHp_max = my_Capoo_2.getHP();
		my_speed = my_Capoo_2.getSpeed();
	}

	private void new_Role_3(Client client, ClientCenter clientCenter, ImageView img_room,
			Label label_room_headpicture) {
		my_Capoo_3 = new Role_Capoo_3(client, clientCenter, img_room, label_room_headpicture);
		my_hp = my_Capoo_3.getHP();
		client.myHp_max = my_Capoo_3.getHP();
		my_speed = my_Capoo_3.getSpeed();
	}

	private void new_Role_4(Client client, ClientCenter clientCenter, ImageView img_room,
			Label label_room_headpicture) {
		my_Capoo_4 = new Role_Capoo_4(client, clientCenter, img_room, label_room_headpicture);
		my_hp = my_Capoo_4.getHP();
		client.myHp_max = my_Capoo_4.getHP();
		my_speed = my_Capoo_4.getSpeed();
	}

	private void new_Role_5(Client client, ClientCenter clientCenter, ImageView img_room,
			Label label_room_headpicture) {
		my_Capoo_5 = new Role_Capoo_5(client, clientCenter, img_room, label_room_headpicture);
		my_hp = my_Capoo_5.getHP();
		client.myHp_max = my_Capoo_5.getHP();
		my_speed = my_Capoo_5.getSpeed();
	}

	// endregion
	// Switch Function To Case
	// region

	private void switch_function_to_case_state_0() {
		switch (function) {
		case "connect":
			switch_myTid_to_connect();
			break;
		case "connected":
			switch_type_to_connected();
			break;
		case "disconnected":
			switch_type_to_disconnected();
			break;
		case "choosed":
			switch_dest_to_switch_type_to_choosed();
			break;
		case "locked":
			role_data[dest - 1] = type;
			switch_dest_to_switch_type_to_locked();
			break;
		case "go1":
			break;
		case "initial":
			switch_dest_to_initial();
			break;
		case "go2":
			client.toGame(stage);
			break;
		case "full":
			System.out.println("人數已滿，關閉連線");
			break;
		}
	}

	private void switch_function_to_case_state_1() {
		switch (function) {
		case "bulletdeath":
			switch_bulletID_to_switch_roledata();
			break;
		case "atk":
			switch_dest_to_other_atk();
			break;
		case "atk2":
			switch_dest_to_other_atk2();
			break;
		case "atked":
			switch_dest_to_other_atked();
			break;
		case "death":
			switch_dest_to_other_death();
			break;
		case "movedup":
			switch_dest_to_movedup();
			break;
		case "moveddown":
			switch_dest_to_moveddown();
			break;
		case "movedleft":
			switch_dest_to_movedleft();
			break;
		case "movedright":
			switch_dest_to_movedright();
			break;
		}
	}

	private void switch_function_to_case_state_2() {
		switch (function) {
		case "win":
			client.keyevent_game = null;
			client.scene_game.setOnKeyPressed(client.keyevent_game);
			if (my_hp > 0) {
				new Win(client.return_game_backgroundground(), name[type - 1]);
				new Firework(client.return_game_backgroundground(), name[type - 1]);
			} else {
				new Lose(client.return_game_backgroundground(), name[type - 1]);
				new Firework(client.return_game_backgroundground(), name[type - 1]);
			}
			break;
		case "back1":
			break;
		case "back2":
			refreshInst();
			client.toRoom(stage, client.label_room_name1, client.label_room_name2, client.label_room_name3,
					client.label_room_name4);
			break;
		}
	}
	// endregion

	// endregion
	// Switch To Connect
	// region

	private void switch_myTid_to_connect() {
		Platform.runLater(() -> {
			try {
				switch (myTid) {
				case 1:
					client.label_room_name1.setText(myName);
					name[0] = myName;
					break;
				case 2:
					client.label_room_name2.setText(myName);
					name[1] = myName;
					break;
				case 3:
					client.label_room_name3.setText(myName);
					name[2] = myName;
					break;
				case 4:
					client.label_room_name4.setText(myName);
					name[3] = myName;
					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private void switch_type_to_connected() {
		Platform.runLater(() -> {
			try {
				switch (type) {
				case 1:
					client.label_room_name1.setText(Stype);
					name[0] = Stype;
					break;
				case 2:
					client.label_room_name2.setText(Stype);
					name[1] = Stype;
					break;
				case 3:
					client.label_room_name3.setText(Stype);
					name[2] = Stype;
					break;
				case 4:
					client.label_room_name4.setText(Stype);
					name[3] = Stype;
					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private void switch_type_to_disconnected() {

	}

	// endregion
	// Switch To Select
	// region

	private void switch_myTid_to_switch_role_to_select() {
		try {
			switch (myTid) {
			case 1:
				switch_role_to_select_1();
				break;
			case 2:
				switch_role_to_select_2();
				break;
			case 3:
				switch_role_to_select_3();
				break;
			case 4:
				switch_role_to_select_4();
				break;
			}
			initSelect(role);
			writer.println(encoder());
			writer.flush();
			refreshInst();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_role_to_select_1() {
		switch (role) {
		case 1:
			select_role_method(client.image_room_player_1, "role_1.png", client.label_room_headpicture1, role);
			break;
		case 2:
			select_role_method(client.image_room_player_1, "role_2.png", client.label_room_headpicture1, role);
			break;
		case 3:
			select_role_method(client.image_room_player_1, "role_3.png", client.label_room_headpicture1, role);
			break;
		case 4:
			select_role_method(client.image_room_player_1, "role_4.png", client.label_room_headpicture1, role);
			break;
		case 5:
			select_role_method(client.image_room_player_1, "role_5.png", client.label_room_headpicture1, role);
			break;
		}
	}

	private void switch_role_to_select_2() {
		switch (role) {
		case 1:
			select_role_method(client.image_room_player_2, "role_1.png", client.label_room_headpicture2, role);
			break;
		case 2:
			select_role_method(client.image_room_player_2, "role_2.png", client.label_room_headpicture2, role);
			break;
		case 3:
			select_role_method(client.image_room_player_2, "role_3.png", client.label_room_headpicture2, role);
			break;
		case 4:
			select_role_method(client.image_room_player_2, "role_4.png", client.label_room_headpicture2, role);
			break;
		case 5:
			select_role_method(client.image_room_player_2, "role_5.png", client.label_room_headpicture2, role);
			break;
		}
	}

	private void switch_role_to_select_3() {
		switch (role) {
		case 1:
			select_role_method(client.image_room_player_3, "role_1.png", client.label_room_headpicture3, role);
			break;
		case 2:
			select_role_method(client.image_room_player_3, "role_2.png", client.label_room_headpicture3, role);
			break;
		case 3:
			select_role_method(client.image_room_player_3, "role_3.png", client.label_room_headpicture3, role);
			break;
		case 4:
			select_role_method(client.image_room_player_3, "role_4.png", client.label_room_headpicture3, role);
			break;
		case 5:
			select_role_method(client.image_room_player_3, "role_5.png", client.label_room_headpicture3, role);
			break;
		}
	}

	private void switch_role_to_select_4() {
		switch (role) {
		case 1:
			select_role_method(client.image_room_player_4, "role_1.png", client.label_room_headpicture4, role);
			break;
		case 2:
			select_role_method(client.image_room_player_4, "role_2.png", client.label_room_headpicture4, role);
			break;
		case 3:
			select_role_method(client.image_room_player_4, "role_3.png", client.label_room_headpicture4, role);
			break;
		case 4:
			select_role_method(client.image_room_player_4, "role_4.png", client.label_room_headpicture4, role);
			break;
		case 5:
			select_role_method(client.image_room_player_4, "role_5.png", client.label_room_headpicture4, role);
			break;
		}
	}

	private void switch_dest_to_switch_type_to_choosed() {
		try {
			switch (dest) {
			case 1:
				switch_type_to_choosed_1();
				break;
			case 2:
				switch_type_to_choosed_2();
				break;
			case 3:
				switch_type_to_choosed_3();
				break;
			case 4:
				switch_type_to_choosed_4();
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_type_to_choosed_1() {
		Platform.runLater(() -> {
			switch (type) {
			case 1:
				selectedRole("role_1.png", client.label_room_headpicture1);
				client.c1Hp_max = 1000;
				break;
			case 2:
				selectedRole("role_2.png", client.label_room_headpicture1);
				client.c1Hp_max = 900;
				break;
			case 3:
				selectedRole("role_3.png", client.label_room_headpicture1);
				client.c1Hp_max = 800;
				break;
			case 4:
				selectedRole("role_4.png", client.label_room_headpicture1);
				client.c1Hp_max = 700;
				break;
			case 5:
				selectedRole("role_5.png", client.label_room_headpicture1);
				client.c1Hp_max = 600;
				break;
			}
		});
	}

	private void switch_type_to_choosed_2() {
		Platform.runLater(() -> {
			switch (type) {
			case 1:
				selectedRole("role_1.png", client.label_room_headpicture2);
				client.c2Hp_max = 1000;
				break;
			case 2:
				selectedRole("role_2.png", client.label_room_headpicture2);
				client.c2Hp_max = 900;
				break;
			case 3:
				selectedRole("role_3.png", client.label_room_headpicture2);
				client.c2Hp_max = 800;
				break;
			case 4:
				selectedRole("role_4.png", client.label_room_headpicture2);
				client.c2Hp_max = 700;
				break;
			case 5:
				selectedRole("role_5.png", client.label_room_headpicture2);
				client.c2Hp_max = 600;
				break;
			}
		});
	}

	private void switch_type_to_choosed_3() {
		Platform.runLater(() -> {
			switch (type) {
			case 1:
				selectedRole("role_1.png", client.label_room_headpicture2);
				client.c3Hp_max = 1000;
				break;
			case 2:
				selectedRole("role_2.png", client.label_room_headpicture2);
				client.c3Hp_max = 900;
				break;
			case 3:
				selectedRole("role_3.png", client.label_room_headpicture2);
				client.c3Hp_max = 800;
				break;
			case 4:
				selectedRole("role_4.png", client.label_room_headpicture2);
				client.c3Hp_max = 700;
				break;
			case 5:
				selectedRole("role_5.png", client.label_room_headpicture2);
				client.c3Hp_max = 600;
				break;
			}
		});
	}

	private void switch_type_to_choosed_4() {
		Platform.runLater(() -> {
			switch (type) {
			case 1:
				selectedRole("role_1.png", client.label_room_headpicture1);
				client.c4Hp_max = 1000;
				break;
			case 2:
				selectedRole("role_2.png", client.label_room_headpicture1);
				client.c4Hp_max = 900;
				break;
			case 3:
				selectedRole("role_3.png", client.label_room_headpicture1);
				client.c4Hp_max = 800;
				break;
			case 4:
				selectedRole("role_4.png", client.label_room_headpicture1);
				client.c4Hp_max = 700;
				break;
			case 5:
				selectedRole("role_5.png", client.label_room_headpicture1);
				client.c4Hp_max = 600;
				break;
			}
		});
	}

	// endregion
	// Switch To Lock
	// region

	private void switch_myTid_to_switch_role_to_lock() {
		try {
			switch (myTid) {
			case 1:
				switch_role_to_lock_1();
				break;
			case 2:
				switch_role_to_lock_2();
				break;
			case 3:
				switch_role_to_lock_3();
				break;
			case 4:
				switch_role_to_lock_4();
				break;
			}

			initLock();
			writer.println(encoder());
			writer.flush();
			refreshInst();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_role_to_lock_1() {
		switch (role) {
		case 1:
			new_Role_1(client, this, client.image_room_player_1, client.label_room_headpicture1);
			break;
		case 2:
			new_Role_2(client, this, client.image_room_player_1, client.label_room_headpicture1);
			break;
		case 3:
			new_Role_3(client, this, client.image_room_player_1, client.label_room_headpicture1);
			break;
		case 4:
			new_Role_4(client, this, client.image_room_player_1, client.label_room_headpicture1);
			break;
		case 5:
			new_Role_5(client, this, client.image_room_player_1, client.label_room_headpicture1);
			break;
		}
	}

	private void switch_role_to_lock_2() {
		switch (role) {
		case 1:
			new_Role_1(client, this, client.image_room_player_2, client.label_room_headpicture2);
			break;
		case 2:
			new_Role_2(client, this, client.image_room_player_2, client.label_room_headpicture2);
			break;
		case 3:
			new_Role_3(client, this, client.image_room_player_2, client.label_room_headpicture2);
			break;
		case 4:
			new_Role_4(client, this, client.image_room_player_2, client.label_room_headpicture2);
			break;
		case 5:
			new_Role_5(client, this, client.image_room_player_2, client.label_room_headpicture2);
			break;
		}
	}

	private void switch_role_to_lock_3() {
		switch (role) {
		case 1:
			new_Role_1(client, this, client.image_room_player_3, client.label_room_headpicture3);
			break;
		case 2:
			new_Role_2(client, this, client.image_room_player_3, client.label_room_headpicture3);
			break;
		case 3:
			new_Role_3(client, this, client.image_room_player_3, client.label_room_headpicture3);
			break;
		case 4:
			new_Role_4(client, this, client.image_room_player_3, client.label_room_headpicture3);
			break;
		case 5:
			new_Role_5(client, this, client.image_room_player_3, client.label_room_headpicture3);
			break;
		}
	}

	private void switch_role_to_lock_4() {
		switch (role) {
		case 1:
			new_Role_1(client, this, client.image_room_player_4, client.label_room_headpicture4);
			break;
		case 2:
			new_Role_2(client, this, client.image_room_player_4, client.label_room_headpicture4);
			break;
		case 3:
			new_Role_3(client, this, client.image_room_player_4, client.label_room_headpicture4);
			break;
		case 4:
			new_Role_4(client, this, client.image_room_player_4, client.label_room_headpicture4);
			break;
		case 5:
			new_Role_5(client, this, client.image_room_player_4, client.label_room_headpicture4);
			break;
		}
	}

	private void switch_dest_to_switch_type_to_locked() {
		try {
			switch (dest) {
			case 1:
				switch_type_to_locked_1();
				break;
			case 2:
				switch_type_to_locked_2();
				break;
			case 3:
				switch_type_to_locked_3();
				break;
			case 4:
				switch_type_to_locked_4();
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_type_to_locked_1() {
		switch (type) {
		case 1:
			lockedRole("lock_role_1.png", client.label_room_headpicture1);
			break;
		case 2:
			lockedRole("lock_role_2.png", client.label_room_headpicture1);
			break;
		case 3:
			lockedRole("lock_role_3.png", client.label_room_headpicture1);
			break;
		case 4:
			lockedRole("lock_role_4.png", client.label_room_headpicture1);
			break;
		case 5:
			lockedRole("lock_role_5.png", client.label_room_headpicture1);
			break;
		}
	}

	private void switch_type_to_locked_2() {
		switch (type) {
		case 1:
			lockedRole("lock_role_1.png", client.label_room_headpicture2);
			break;
		case 2:
			lockedRole("lock_role_2.png", client.label_room_headpicture2);
			break;
		case 3:
			lockedRole("lock_role_3.png", client.label_room_headpicture2);
			break;
		case 4:
			lockedRole("lock_role_4.png", client.label_room_headpicture2);
			break;
		case 5:
			lockedRole("lock_role_5.png", client.label_room_headpicture2);
			break;
		}
	}

	private void switch_type_to_locked_3() {
		switch (type) {
		case 1:
			lockedRole("lock_role_1.png", client.label_room_headpicture3);
			break;
		case 2:
			lockedRole("lock_role_2.png", client.label_room_headpicture3);
			break;
		case 3:
			lockedRole("lock_role_3.png", client.label_room_headpicture3);
			break;
		case 4:
			lockedRole("lock_role_4.png", client.label_room_headpicture3);
			break;
		case 5:
			lockedRole("lock_role_5.png", client.label_room_headpicture3);
			break;
		}
	}

	private void switch_type_to_locked_4() {
		switch (type) {
		case 1:
			lockedRole("lock_role_1.png", client.label_room_headpicture4);
			break;
		case 2:
			lockedRole("lock_role_2.png", client.label_room_headpicture4);
			break;
		case 3:
			lockedRole("lock_role_3.png", client.label_room_headpicture4);
			break;
		case 4:
			lockedRole("lock_role_4.png", client.label_room_headpicture4);
			break;
		case 5:
			lockedRole("lock_role_5.png", client.label_room_headpicture4);
			break;
		}
	}

	// endregion

	// endregion
	// Switch To initial
	// region

	private void switch_dest_to_initial() {
		try {
			switch (dest) {
			case 1:
				position[0][0] = X;
				position[0][1] = Y;
				if (dest == myTid) {
					switch_myRole_to_set_X(X);
					switch_myRole_to_set_Y(Y);
				}
				break;
			case 2:
				position[1][0] = X;
				position[1][1] = Y;
				if (dest == myTid) {
					switch_myRole_to_set_X(X);
					switch_myRole_to_set_Y(Y);
				}
				break;
			case 3:
				position[2][0] = X;
				position[2][1] = Y;
				if (dest == myTid) {
					switch_myRole_to_set_X(X);
					switch_myRole_to_set_Y(Y);
				}
				break;
			case 4:
				position[3][0] = X;
				position[3][1] = Y;
				if (dest == myTid) {
					switch_myRole_to_set_X(X);
					switch_myRole_to_set_Y(Y);
				}
				break;
			}
			my_speed = switch_myRole_to_get_SPEED();
			Start_X = switch_myRole_to_get_X();
			Start_Y = switch_myRole_to_get_Y();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// endregion

	// endregion
	// Switch To Move
	// region
	private void switch_myTid_to_moveup() {
		try {
			switch (myTid) {
			case 1:
				client.setLocation(client.label_game_character1, client.label_game_name1,
						client.progressbar_game_characterblood1, Start_X, Start_Y);
				break;
			case 2:
				client.setLocation(client.label_game_character2, client.label_game_name2,
						client.progressbar_game_characterblood2, Start_X, Start_Y);
				break;
			case 3:
				client.setLocation(client.label_game_character3, client.label_game_name3,
						client.progressbar_game_characterblood3, Start_X, Start_Y);
				break;
			case 4:
				client.setLocation(client.label_game_character4, client.label_game_name4,
						client.progressbar_game_characterblood4, Start_X, Start_Y);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_myTid_to_movedown() {
		try {
			switch (myTid) {
			case 1:
				client.setLocation(client.label_game_character1, client.label_game_name1,
						client.progressbar_game_characterblood1, Start_X, Start_Y);
				break;
			case 2:
				client.setLocation(client.label_game_character2, client.label_game_name2,
						client.progressbar_game_characterblood2, Start_X, Start_Y);
				break;
			case 3:
				client.setLocation(client.label_game_character3, client.label_game_name3,
						client.progressbar_game_characterblood3, Start_X, Start_Y);
				break;
			case 4:
				client.setLocation(client.label_game_character4, client.label_game_name4,
						client.progressbar_game_characterblood4, Start_X, Start_Y);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_myTid_to_moveleft() {
		try {
			switch (myTid) {
			case 1:
				client.setLocation(client.label_game_character1, client.label_game_name1,
						client.progressbar_game_characterblood1, Start_X, Start_Y);
				break;
			case 2:
				client.setLocation(client.label_game_character2, client.label_game_name2,
						client.progressbar_game_characterblood2, Start_X, Start_Y);
				break;
			case 3:
				client.setLocation(client.label_game_character3, client.label_game_name3,
						client.progressbar_game_characterblood3, Start_X, Start_Y);
				break;
			case 4:
				client.setLocation(client.label_game_character4, client.label_game_name4,
						client.progressbar_game_characterblood4, Start_X, Start_Y);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_myTid_to_moveright() {
		try {
			switch (myTid) {
			case 1:
				client.setLocation(client.label_game_character1, client.label_game_name1,
						client.progressbar_game_characterblood1, Start_X, Start_Y);
				break;
			case 2:
				client.setLocation(client.label_game_character2, client.label_game_name2,
						client.progressbar_game_characterblood2, Start_X, Start_Y);
				break;
			case 3:
				client.setLocation(client.label_game_character3, client.label_game_name3,
						client.progressbar_game_characterblood3, Start_X, Start_Y);
				break;
			case 4:
				client.setLocation(client.label_game_character4, client.label_game_name4,
						client.progressbar_game_characterblood4, Start_X, Start_Y);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_dest_to_movedup() {
		try {
			switch (dest) {
			case 1:
				client.setLocation(client.label_game_character1, client.label_game_name1,
						client.progressbar_game_characterblood1, X, Y);
				break;
			case 2:
				client.setLocation(client.label_game_character2, client.label_game_name2,
						client.progressbar_game_characterblood2, X, Y);
				break;
			case 3:
				client.setLocation(client.label_game_character3, client.label_game_name3,
						client.progressbar_game_characterblood3, X, Y);
				break;
			case 4:
				client.setLocation(client.label_game_character4, client.label_game_name4,
						client.progressbar_game_characterblood4, X, Y);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_dest_to_moveddown() {
		try {
			switch (dest) {
			case 1:
				client.setLocation(client.label_game_character1, client.label_game_name1,
						client.progressbar_game_characterblood1, X, Y);
				break;
			case 2:
				client.setLocation(client.label_game_character2, client.label_game_name2,
						client.progressbar_game_characterblood2, X, Y);
				break;
			case 3:
				client.setLocation(client.label_game_character3, client.label_game_name3,
						client.progressbar_game_characterblood3, X, Y);
				break;
			case 4:
				client.setLocation(client.label_game_character4, client.label_game_name4,
						client.progressbar_game_characterblood4, X, Y);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_dest_to_movedleft() {
		try {
			switch (dest) {
			case 1:
				client.setLocation(client.label_game_character1, client.label_game_name1,
						client.progressbar_game_characterblood1, X, Y);
				break;
			case 2:
				client.setLocation(client.label_game_character2, client.label_game_name2,
						client.progressbar_game_characterblood2, X, Y);
				break;
			case 3:
				client.setLocation(client.label_game_character3, client.label_game_name3,
						client.progressbar_game_characterblood3, X, Y);
				break;
			case 4:
				client.setLocation(client.label_game_character4, client.label_game_name4,
						client.progressbar_game_characterblood4, X, Y);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_dest_to_movedright() {
		try {
			switch (dest) {
			case 1:
				client.setLocation(client.label_game_character1, client.label_game_name1,
						client.progressbar_game_characterblood1, X, Y);
				break;
			case 2:
				client.setLocation(client.label_game_character2, client.label_game_name2,
						client.progressbar_game_characterblood2, X, Y);
				break;
			case 3:
				client.setLocation(client.label_game_character3, client.label_game_name3,
						client.progressbar_game_characterblood3, X, Y);
				break;
			case 4:
				client.setLocation(client.label_game_character4, client.label_game_name4,
						client.progressbar_game_characterblood4, X, Y);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// endregion
	// endregion
	// Switch To Atk
	// region

	private void switch_dest_to_other_atk() {
		try {
			switch (role_data[dest - 1]) {
			case 1:
				attacklist.put(type, new Attack1(client, this, X, Y, client.return_game_backgroundground(), direction,
						type, attacklist));
				break;
			case 2:
				attack2list.put(type, new Attack2(client, this, X, Y, client.return_game_backgroundground(), direction,
						type, attack2list));
				break;
			case 3:
				attack3list.put(type, new Attack3(client, this, X, Y, client.return_game_backgroundground(), direction,
						type, attack3list));
				break;
			case 4:
				attack4list.put(type, new Attack4(client, this, X, Y, client.return_game_backgroundground(), direction,
						type, attack4list));
				break;
			case 5:
				attack5list.put(type, new Attack5(client, this, X, Y, client.return_game_backgroundground(), direction,
						type, attack5list));
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_dest_to_other_atk2() {
		try {
			switch (role_data[dest - 1]) {
			case 1:
				bulletlist.put(type, new Attack1_2(client, this, X, Y, client.return_game_backgroundground(), direction,
						type, bulletlist));
				break;
			case 2:
				bullet2list.put(type, new Attack2_2(client, this, X, Y, client.return_game_backgroundground(),
						direction, type, bullet2list));
				break;
			case 3:
				bullet3list.put(type, new Attack3_2(client, this, X, Y, client.return_game_backgroundground(),
						direction, type, bullet3list));
				break;
			case 4:
				bullet4list.put(type, new Attack4_2(client, this, X, Y, client.return_game_backgroundground(),
						direction, type, bullet4list));
				break;
			case 5:
				bullet5list.put(type, new Attack5_2(client, this, X, Y, client.return_game_backgroundground(),
						direction, type, bullet5list));
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_dest_to_other_atked() {
		try {
			switch (dest) {
			case 1:
				client.game_setOtherHP(dest, type, client.progressbar_game_characterblood1);
				break;
			case 2:
				client.game_setOtherHP(dest, type, client.progressbar_game_characterblood2);
				break;
			case 3:
				client.game_setOtherHP(dest, type, client.progressbar_game_characterblood3);
				break;
			case 4:
				client.game_setOtherHP(dest, type, client.progressbar_game_characterblood4);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// endregion

	// endregion
	// Switch To Death
	// region

	private void switch_dest_to_other_death() {
		try {
			switch (dest) {
			case 1:
				client.label_game_character1.setDisable(true);
				client.label_game_name1.setDisable(true);
				client.progressbar_game_characterblood1.setDisable(true);
				break;
			case 2:
				client.label_game_character2.setDisable(true);
				client.label_game_name2.setDisable(true);
				client.progressbar_game_characterblood2.setDisable(true);
				break;
			case 3:
				client.label_game_character3.setDisable(true);
				client.label_game_name3.setDisable(true);
				client.progressbar_game_characterblood3.setDisable(true);
				break;
			case 4:
				client.label_game_character4.setDisable(true);
				client.label_game_name4.setDisable(true);
				client.progressbar_game_characterblood4.setDisable(true);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// endregion
	// endregion
	// Switch MyRole To Get Value
	// region

	private void switch_myRole_to_get_GameMyImage() {
		try {
			switch (my_role) {
			case 1:
				my_png = my_Capoo_1.getGameMyImage();
				break;
			case 2:
				my_png = my_Capoo_2.getGameMyImage();
				break;
			case 3:
				my_png = my_Capoo_3.getGameMyImage();
				break;
			case 4:
				my_png = my_Capoo_4.getGameMyImage();
				break;
			case 5:
				my_png = my_Capoo_5.getGameMyImage();
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int switch_myRole_to_get_HP() {
		try {
			switch (my_role) {
			case 1:
				tmp = my_Capoo_1.getHP();
				break;
			case 2:
				tmp = my_Capoo_2.getHP();
				break;
			case 3:
				tmp = my_Capoo_3.getHP();
				break;
			case 4:
				tmp = my_Capoo_4.getHP();
				break;
			case 5:
				tmp = my_Capoo_5.getHP();
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return tmp;
	}

	public void switch_myRole_to_set_HP(int hp) {
		try {
			switch (my_role) {
			case 1:
				my_Capoo_1.setHp(hp);
				break;
			case 2:
				my_Capoo_2.setHp(hp);
				break;
			case 3:
				my_Capoo_3.setHp(hp);
				break;
			case 4:
				my_Capoo_4.setHp(hp);
				break;
			case 5:
				my_Capoo_5.setHp(hp);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private int switch_myRole_to_get_SPEED() {
		try {
			switch (my_role) {
			case 1:
				tmp = my_Capoo_1.getSpeed();
				break;
			case 2:
				tmp = my_Capoo_2.getSpeed();
				break;
			case 3:
				tmp = my_Capoo_3.getSpeed();
				break;
			case 4:
				tmp = my_Capoo_4.getSpeed();
				break;
			case 5:
				tmp = my_Capoo_5.getSpeed();
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return tmp;
	}

	private double switch_myRole_to_get_X() {
		try {
			switch (my_role) {
			case 1:
				tmpX = my_Capoo_1.getX();
				break;
			case 2:
				tmpX = my_Capoo_2.getX();
				break;
			case 3:
				tmpX = my_Capoo_3.getX();
				break;
			case 4:
				tmpX = my_Capoo_4.getX();
				break;
			case 5:
				tmpX = my_Capoo_5.getX();
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return tmpX;
	}

	private void switch_myRole_to_set_X(double tmpX) {
		try {
			switch (my_role) {
			case 1:
				my_Capoo_1.setX(tmpX);
				break;
			case 2:
				my_Capoo_2.setX(tmpX);
				break;
			case 3:
				my_Capoo_3.setX(tmpX);
				break;
			case 4:
				my_Capoo_4.setX(tmpX);
				break;
			case 5:
				my_Capoo_5.setX(tmpX);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private double switch_myRole_to_get_Y() {
		try {
			switch (my_role) {
			case 1:
				tmpY = my_Capoo_1.getY();
				break;
			case 2:
				tmpY = my_Capoo_2.getY();
				break;
			case 3:
				tmpY = my_Capoo_3.getY();
				break;
			case 4:
				tmpY = my_Capoo_4.getY();
				break;
			case 5:
				tmpY = my_Capoo_5.getY();
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return tmpY;
	}

	private void switch_myRole_to_set_Y(double tmpY) {
		try {
			switch (my_role) {
			case 1:
				my_Capoo_1.setY(tmpY);
				break;
			case 2:
				my_Capoo_2.setY(tmpY);
				break;
			case 3:
				my_Capoo_3.setY(tmpY);
				break;
			case 4:
				my_Capoo_4.setY(tmpY);
				break;
			case 5:
				my_Capoo_5.setY(tmpY);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	// endregion
	// Switch RoleData To Set Other Image
	// region

	private void switch_roledata_to_set_other_game_image() {
		try {
			switch_roledata_to_set_other_game_image_0();
			switch_roledata_to_set_other_game_image_1();
			switch_roledata_to_set_other_game_image_2();
			switch_roledata_to_set_other_game_image_3();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void switch_roledata_to_set_other_game_image_0() {
		switch (role_data[0]) {
		case 1:
			c1_png = "img_game_role1.png";
			break;
		case 2:
			c1_png = "img_game_role2.png";
			break;
		case 3:
			c1_png = "img_game_role3.png";
			break;
		case 4:
			c1_png = "img_game_role4.png";
			break;
		case 5:
			c1_png = "img_game_role5.png";
			break;
		default:
			c1_png = "img_game_role1.png";
			break;
		}
	}

	private void switch_roledata_to_set_other_game_image_1() {
		switch (role_data[1]) {
		case 1:
			c2_png = "img_game_role1.png";
			break;
		case 2:
			c2_png = "img_game_role2.png";
			break;
		case 3:
			c2_png = "img_game_role3.png";
			break;
		case 4:
			c2_png = "img_game_role4.png";
			break;
		case 5:
			c2_png = "img_game_role5.png";
			break;
		default:
			c2_png = "img_game_role1.png";
			break;
		}
	}

	private void switch_roledata_to_set_other_game_image_2() {
		switch (role_data[2]) {
		case 1:
			c3_png = "img_game_role1.png";
			break;
		case 2:
			c3_png = "img_game_role2.png";
			break;
		case 3:
			c3_png = "img_game_role3.png";
			break;
		case 4:
			c3_png = "img_game_role4.png";
			break;
		case 5:
			c3_png = "img_game_role5.png";
			break;
		default:
			c3_png = "img_game_role1.png";
			break;
		}
	}

	private void switch_roledata_to_set_other_game_image_3() {
		switch (role_data[3]) {
		case 1:
			c4_png = "img_game_role1.png";
			break;
		case 2:
			c4_png = "img_game_role2.png";
			break;
		case 3:
			c4_png = "img_game_role3.png";
			break;
		case 4:
			c4_png = "img_game_role4.png";
			break;
		case 5:
			c4_png = "img_game_role5.png";
			break;
		default:
			c4_png = "img_game_role1.png";
			break;
		}
	}
	// endregion

	// endregion
	// coder
	// region

	private void switch_bulletID_to_switch_roledata() {
		switch (role_data[(type / 10000) - 1]) {
		case 1:
			bulletlist.get(type).bulletDeath(X, Y);
			bulletlist.remove(type);
			break;
		case 2:
			bullet2list.get(type).bulletDeath(X, Y);
			bullet2list.remove(type);
			break;
		case 3:
			bullet3list.get(type).bulletDeath(X, Y);
			bullet3list.remove(type);
			break;
		case 4:
			bullet4list.get(type).bulletDeath(X, Y);
			bullet4list.remove(type);
			break;
		case 5:
			bullet5list.get(type).bulletDeath(X, Y);
			bullet5list.remove(type);
			break;
		}
	}

	private String encoder() {
		String ta[] = new String[10];
		ta[0] = Integer.toString(state);
		ta[1] = Integer.toString(myTid);
		ta[2] = function;
		ta[3] = Integer.toString(source);
		ta[4] = Integer.toString(dest);
		ta[5] = Integer.toString(type);
		ta[6] = Double.toString(X);
		ta[7] = Double.toString(Y);
		ta[8] = Integer.toString(direction);
		ta[9] = Stype;
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < ta.length; i++) {
			strBuilder.append(ta[i]);
			if (i != ta.length - 1) {
				strBuilder.append("#");
			}
		}
		String message = strBuilder.toString();
		return message;
	}

	private void decoder(String message) {
		// 把message用#分開
		ta = message.split("#");
		// System.out.print("解開訊息:");
		// for (int i = 0; i < ta.length; i++) {
		// System.out.print(ta[i] + "+");
		// }
		// System.out.println();
		// 把所有拆開的訊息依序填入thread中的參數
		state = Integer.parseInt(ta[0]);
		Tid = Integer.parseInt(ta[1]);
		function = ta[2];
		source = Integer.parseInt(ta[3]);
		dest = Integer.parseInt(ta[4]);
		type = Integer.parseInt(ta[5]);
		X = Double.parseDouble(ta[6]);
		Y = Double.parseDouble(ta[7]);
		direction = Integer.parseInt(ta[8]);
		Stype = ta[9];
		if (function.equals("connect"))
			myTid = Integer.parseInt(ta[5]);
		System.out.println("state: " + state);
		System.out.println("Tid: " + Tid);
		System.out.println("function: " + function);
		System.out.println("source: " + source);
		System.out.println("dest: " + dest);
		System.out.println("type: " + type);
		System.out.println("X: " + X);
		System.out.println("Y: " + Y);
		System.out.println("direction: " + direction);
		System.out.println("Stype: " + Stype);
		System.out.println("myTid: " + myTid);
	}

	// endregion
	// init
	// region

	private void initConnection() {
		state = 0;
		function = "connect";
		source = -1;
		dest = -1;
		type = -1;
		X = -1.0;
		Y = -1.0;
		direction = 0;
		Stype = myName;
	}

	private void initSelect(int role) {
		state = 0;
		function = "choose";
		source = -1;
		dest = -1;
		type = role;
		X = -1.0;
		Y = -1.0;
		direction = 0;
		Stype = "@";
	}

	private void initLock() {
		state = 0;
		function = "lock";
		source = -1;
		dest = -1;
		type = role;
		X = -1.0;
		Y = -1.0;
		direction = 0;
		Stype = "@";
	}

	private void initMoveUp(double x, double y) {
		state = 1;
		function = "moveup";
		source = -1;
		dest = -1;
		type = -1;
		X = x;
		Y = y;
		direction = 0;
		Stype = "@";
	}

	private void initMoveDown(double x, double y) {
		state = 1;
		function = "movedown";
		source = -1;
		dest = -1;
		type = -1;
		X = x;
		Y = y;
		direction = 0;
		Stype = "@";
	}

	private void initMoveLeft(double x, double y) {
		state = 1;
		function = "moveleft";
		source = -1;
		dest = -1;
		type = -1;
		X = x;
		Y = y;
		direction = direction_atk;
		Stype = "@";
	}

	private void initMoveRight(double x, double y) {
		state = 1;
		function = "moveright";
		source = -1;
		dest = -1;
		type = -1;
		X = x;
		Y = y;
		direction = direction_atk;
		Stype = "@";
	}

	private void initAttack(int bulletNumber) {
		state = 1;
		function = "atk";
		source = -1;
		dest = -1;
		type = bulletNumber;
		X = Start_X;
		Y = Start_Y;
		direction = direction_atk;
		Stype = "@";
	}

	private void initAttack2(int bulletNumber) {
		state = 1;
		function = "atk2";
		source = -1;
		dest = -1;
		type = bulletNumber;
		X = Start_X;
		Y = Start_Y;
		direction = direction_atk;
		Stype = "@";
	}

	private void initAtked() {
		state = 1;
		function = "atked";
		source = -1;
		dest = -1;
		type = my_hp;
		X = Start_X;
		Y = Start_Y;
		direction = 0;
		Stype = "@";
	}

	private void initAtk2ed() {
		state = 1;
		function = "atked";
		source = -1;
		dest = -1;
		type = my_hp;
		X = Start_X;
		Y = Start_Y;
		direction = 0;
		Stype = "@";
	}

	private void initbulletdeath(int bulletID, double bulletX, double bulletY) {

		state = 1;
		Tid = -1;
		function = "bulletdeath";
		source = -1;
		dest = -1;
		type = bulletID;
		X = bulletX;
		Y = bulletY;
		direction = 0;
		Stype = "@";
	}

	private void initDeath() {
		state = 1;
		function = "death";
		source = -1;
		dest = -1;
		type = -1;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	// endregion
	// refresh
	// region

	private void refreshInst() {

		state = -1;
		Tid = -1;
		function = "";
		source = -1;
		dest = -1;
		type = -1;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	// endregion
	private void writermsg() {
		writer.println(encoder());
		writer.flush();
		refreshInst();
	}

	// endregion
}