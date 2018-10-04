package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;

public class ServerCenter implements Runnable {
	// 暫存資料的Buffered
	BufferedReader reader;
	// 建立一個Socket變數
	Socket sock;
	private int myTid = -1;
	// 倒數秒數
	private int time = 5;
	// 預設沒有名字
	private String myName = "no name";
	// PrintStream列表
	Vector<PrintStream> output;

	// Key:tid,Value:PrintStream
	// Tid跟PrintStream
	Map<Integer, PrintStream> tmap;
	Map<PrintStream, Integer> pmap;
	// Map Tid跟選角, Tid跟名字
	Map<Integer, Integer> tchoose = new HashMap<>();
	Map<Integer, String> tname = new HashMap<>();
	// 視窗大小
	private int sizex = 1024;
	private int sizey = 768;
	// 遊戲開始了?
	boolean started = false;
	// setLive:連上線的tid
	Set<Integer> setLive;
	// setLocked:已經鎖定的tid
	Set<Integer> setLocked;
	// setDeath:已經死亡的tid
	Set<Integer> setDeath;
	// true:tellAll,false:tellOthers
	boolean tellWho = true;
	// 拆解跟結合使用的訊息個別參數
	// For Instructions
	private int state;
	private int Tid;
	private String function;
	private int source;
	private int dest;
	private int type;
	private double X;
	private double Y;
	private int direction;
	private String Stype;
	// 需不需要接收前人訊息
	private boolean getHistory = false;

	public ServerCenter(Socket acceptSocket, int playerTid, Vector<PrintStream> x, Map<Integer, PrintStream> inserttMap,
			Map<PrintStream, Integer> insertpMap, Set<Integer> Live, Set<Integer> Locked, Set<Integer> Death,
			Map<Integer, Integer> tc, Map<Integer, String> tn, boolean flag) {
		try {
			sock = acceptSocket;
			// 取得Socket的輸入資料流
			InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(isReader);

			setLive = Live;
			setLocked = Locked;
			setDeath = Death;
			tmap = inserttMap;
			pmap = insertpMap;
			myTid = playerTid;
			output = x;
			tchoose = tc;
			tname = tn;
			started = flag;
			setLive.add(myTid);
			System.out.print("Center 線上名單 for " + myTid + ":" + setLive);
			// 第一次連線回傳給Client 他專屬的Thread id
			refreshInst();
			tidSend();
			if (setLive.size() > 0) {
				getHistory = true;
			}

		} catch (Exception ex) {
			Main.appendTa("連接失敗 in Center");
		}
	}

	@Override
	public void run() {
		String message;
		try {
			// 讀取資料
			while ((message = reader.readLine()) != null) {
				Main.appendTa("收到" + message);
				if (message.contains("#")) {
					// 這是因為暫時測試用的的client會把名字:打在前面

					decoder(message);
					handle();
					Main.appendTa("Live: "+setLive.size()+" Locked : "+setLocked.size());
					Main.appendTa("state: "+state+" started : "+started);
					if(setLocked.size()!=setLive.size()){
						started=false;
					}
					if (state == 0 && setLocked.size() == setLive.size() && started==false && setLive.size() > 1) {
						gotofight();

					} else if (setLive.size() > 1 && setLive.size() - 1 == setDeath.size() && setDeath.size() >= 1) {
						Main.appendTa("贏家出現");
						tellOthers();
						// 如果贏家出現了
						// win 告訴自己誰贏了
						inst13(whoWon());
						tellAll();
						// back1 告訴自己開始倒數回到大廳
						inst14();
						tellAll();
						// 開始倒數
						doCountDown();
						// 結束倒數
						// back2 告訴自己回到大廳
						inst15();
						tellAll();
						// 已鎖定的清單清空->大家重新選角
						// 已經死亡的名單清空
						started = false;
						setLocked.clear();
						setDeath.clear();
						getHistory = true;
					} else if (state == 0 && setLive.size() > 0 && getHistory) {
						// 第一次連入接收前人的資訊S
						tellOthers();
						getChoose();
						getName();
						getHistory = false;
					} else {
						tellOthers();
					}
				} else {
					// 保留正常聊天室功能debug 可忽略

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			dcHandle();
			Main.appendTa("Tid:" + myTid + "連接離開" + ex.toString());
			if (state == 0 && setLocked.size() == setLive.size() && !started && setLive.size() > 1) {
				gotofight();

			}

		}
	}

	// --------------------------------------------------------------------//
	// 訊息派送Send Messages //
	// --------------------------------------------------------------------//
	public void tellAll() {
		// 產生iterator可以存取集合內的元素資料
		// 這是聊天室用的寫法 也可以參考 是送給所有的人
		Iterator<PrintStream> it = output.iterator();
		while (it.hasNext()) {
			try {
				PrintStream writer = (PrintStream) it.next();
				if (setLive.contains(pmap.get(writer))) {
					writer.println(encoder(pmap.get(writer)));
					writer.flush();
					Main.appendTa(myTid + "tellAll");
				}

			} catch (Exception ex) {
				Main.appendTa("tellAll 噴錯");
			}
		}
		// 送完之後把訊息參數初始化
		refreshInst();
	}

	public void tellOthers() {
		// 產生iterator可以存取集合內的元素資料
		Iterator<PrintStream> it = output.iterator();
		while (it.hasNext()) {
			try {
				PrintStream writer = (PrintStream) it.next();
				// 把PS跟tmap<Integer,PrintStream>中拿到的對應Tid去比對
				// 如果剛好是自己的話不發送訊息
				if (writer != tmap.get(myTid)) {
					// 把pmap<PrintStream,Integer>中拿到的跟PS相對應的Tid餵給encoder
					// 讓Encoder去填寫接收者的Tid
					writer.println(encoder(pmap.get(writer)));
					// 刷新該串流的緩衝。
					writer.flush();
				} else {
					writer = null;
				}

			} catch (Exception ex) {
				Main.appendTa("tellOthers 噴錯");
			}
		}
		// 送完之後把訊息參數初始化
		refreshInst();
	}

	// 當玩家第一次連線時候
	public void tidSend() {
		try {
			// 用map去get PrintStream
			PrintStream writer = tmap.get(myTid);
			inst1(myTid);
			// 第一次傳送Tid時Client還不知道自己的tid
			String message = encoder(-1);
			writer.println(message);
			writer.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 送完之後把訊息參數初始化
		refreshInst();
	}

	public void tellMyself() {
		try {
			// 用map去get PrintStream
			PrintStream writer = tmap.get(myTid);
			inst2(myTid);
			// 第一次傳送Tid時Client還不知道自己的tid
			String message = encoder(myTid);
			writer.println(message);
			writer.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 送完之後把訊息參數初始化
		refreshInst();
	}

	// --------------------------------------------------------------------//
	// 訊息處理區Deal Formats and Messages //
	// --------------------------------------------------------------------//
	public void decoder(String message) {
		// 把message用#分開
		String ta[] = message.split("#");
		// System.out.print("解開訊息:");
		// for (int i = 0; i < ta.length; i++) {
		// System.out.print(ta[i] + "+");
		// }
		// Main.appendTa();
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
	}

	// 把訊息壓成格式
	public String encoder(int forwardTid) {
		// 給的參數是收件者的tid
		// 用一個陣列把所有訊息湊起來
		String ta[] = new String[10];
		ta[0] = Integer.toString(state);
		ta[1] = Integer.toString(forwardTid);
		ta[2] = function;
		ta[3] = Integer.toString(source);
		ta[4] = Integer.toString(dest);
		ta[5] = Integer.toString(type);
		ta[6] = Double.toString(X);
		ta[7] = Double.toString(Y);
		ta[8] = Integer.toString(direction);
		ta[9] = Stype;
		// 利用StringBuilder把所有訊息一個一個串起來
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < ta.length; i++) {
			strBuilder.append(ta[i]);
			if (i != ta.length - 1) {
				strBuilder.append("#");
			}
		}
		//
		String message = strBuilder.toString();
		return message;
	}

	// 重新洗掉所有訊息拆解下來的參數
	public void refreshInst() {
		tellWho = true;
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

	// --------------------------------------------------------------------//
	// 資料處理區Data Dealing //
	// --------------------------------------------------------------------//

	// 處理所有接收到的訊息
	synchronized public void handle() {

		if (state == 0) {
			switch (function) {
			case "connect":
				Main.appendTa("handle 線上名單 for " + myTid + ":" + setLive);
				//

				if (Stype.equals("@")) {
					myName = "no name" + Tid;
				} else {
					myName = Stype;
				}
				tname.put(Tid, Stype);
				inst2(Tid);
				break;
			case "disconnect":
				setLive.remove(Tid);
				inst3(Tid);
				break;
			case "choose":
				tchoose.put(Tid, type);
				Main.appendTa("tchoose名單 for " + myTid + ":" + tchoose);
				inst4(Tid, type);
				break;
			case "lock":
				setLocked.add(Tid);
				Main.appendTa("鎖定名單 for " + myTid + ":" + setLocked);
				inst5(myTid, type);
				break;
			}

		} else if (state == 1) {
			switch (function) {
			case "atk":
				inst9(Tid, X, Y, direction);
				break;
			case "atk2":
				inst10(Tid, X, Y, direction);
				break;
			case "atked":
				inst11(Tid, X, Y, type);
				break;
			case "death":
				setDeath.add(Tid);
				inst12(Tid);
				break;
			case "moveup":
				instUp(myTid);
				break;
			case "movedown":
				instDown(myTid);
				break;
			case "moveleft":
				instLeft(myTid);
				break;
			case "moveright":
				instRight(myTid);
				break;
			case "bulletdeath":
				bulletdeath(Tid,type);
				break;
			}

		}
	}

	// --------------------------------------------------------------------//
	// 特殊情況的處理 Special data dealing //
	// --------------------------------------------------------------------//
	// 計算出誰贏了
	public void gotofight() {
		// 如果大家都鎖定了
		Main.appendTa("鎖定已滿遊戲準備開始");
		// go1 告訴自己準備要開始
		tellOthers();
		inst6();
		tellAll();
		// initial傳遞 自己算出來自己tid的亂數位置給別人
		calculateAll();
		// 開始倒數
		doCountDown();
		// 結束倒數
		// 告訴自己遊戲開始
		inst8();
		tellAll();
		started = true;
	}

	synchronized public int whoWon() {
		Iterator<Integer> it = setLocked.iterator();
		int wtid = -1;
		while (it.hasNext() && wtid == -1) {
			try {
				wtid = (int) it.next();
				if (setDeath.contains(wtid)) {
					wtid = -1;
				} else {
				}

			} catch (Exception ex) {
				Main.appendTa("計算誰贏噴錯");
			}
		}
		return wtid;
	}

	// 利用Thread Sleep來倒數五秒
	public void doCountDown() {
		// Timer timer = new Timer();
		Main.appendTa("Delay:" + time + "秒");
		try {
			Thread.sleep(time * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Main.appendTa(myTid + "離開TimerTask");
	}

	// 計算亂數起始位置
	public void randomPosition() {
		Random r1 = new Random();
		int x = r1.nextInt(1024);
		int y = r1.nextInt(320);
		X = (double) x - 1024 / 2;
		Y = (double) y + 64;
	}

	// 把所有有紀錄的choose讀入
	public void getChoose() {
		Iterator it = tchoose.entrySet().iterator();
		while (it.hasNext()) {
			try {
				PrintStream writer = tmap.get(myTid);
				Entry thisEntry = (Entry) it.next();
				int key = (int) thisEntry.getKey();
				if (key != myTid && setLive.contains(key)) {
					if (setLocked.contains(key)) {
						inst5(key, tchoose.get(key));
					} else {
						inst4(key, tchoose.get(key));
					}
					writer.println(encoder(myTid));
					// 刷新該串流的緩衝。
					writer.flush();
				}
			} catch (Exception ex) {
				Main.appendTa("getChoose 噴錯");
			}
		}
		// 送完之後把訊息參數初始化
		refreshInst();
	}

	// 把所有有記錄下來的Tid對應名字讀入
	public void getName() {
		Iterator it = tname.entrySet().iterator();
		while (it.hasNext()) {
			try {
				PrintStream writer = tmap.get(myTid);
				Entry thisEntry = (Entry) it.next();
				int key = (int) thisEntry.getKey();
				if (key != myTid && setLive.contains(key)) {
					inst2(key);
					Stype = tname.get(key);
					writer.println(encoder(myTid));
					// 刷新該串流的緩衝。
					writer.flush();
				}
			} catch (Exception ex) {
				Main.appendTa("getName 噴錯");
			}
		}
		// 送完之後把訊息參數初始化
		refreshInst();
	}
	public void giveAllInfo(){
		getName();
		inst2(myTid);
		tellMyself();
		
	}
	// 把所有亂數參數依序送給Live裡面的人
	synchronized public void calculateAll() {
		Iterator<Integer> it = setLocked.iterator();
		while (it.hasNext()) {
			try {
				int key = it.next();
				inst7(key);
				randomPosition();
				tellAll();
			} catch (Exception ex) {
				Main.appendTa("計算誰贏噴錯");
			}
		}
	}

	public void dcHandle() {
		// 線上的Tid名單除掉斷線者
		// 並告訴其他人我自己斷線了
		Main.appendTa("開始清理" + myTid + "斷線殘存的垃圾......");
		setLive.remove(myTid);
		setLocked.remove(myTid);
		setDeath.remove(myTid);
		tname.remove(myTid);
		tchoose.remove(myTid);
		output.remove(tmap.get(myTid));
		pmap.remove(tmap.get(myTid));
		tmap.remove(myTid);
		inst3(myTid);
		tellOthers();
		Main.appendTa(myTid + " dcHandle:");
		Main.appendTa("線上名單:" + setLive);
		Main.appendTa("鎖定名單:" + setLocked);
		Main.appendTa("線上名單:" + setDeath);
	}

	// --------------------------------------------------------------------//
	// 輸出指令格式Forward Formats //
	// --------------------------------------------------------------------//
	// 這邊Tid全給-1的原因是因為要送出去的時候Encoder會自己給定
	public void inst1(int givetid) {
		// Instuction #1 connect,first time connect give tid
		state = 0;
		Tid = -1;
		function = "connect";
		source = -1;
		dest = -1;
		type = givetid;
		X = -1;
		Y = -1;
		direction = setLive.size();
		Stype = "@";
	}

	public void inst2(int givetid) {
		// Instuction #2 connected,tell others name
		state = 0;
		Tid = -1;
		function = "connected";
		source = -1;
		dest = -1;
		type = givetid;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = myName;
	}

	public void inst3(int dctid) {
		// Instuction #3 disconnected,tell all
		state = 0;
		Tid = -1;
		function = "disconnected";
		source = -1;
		dest = -1;
		type = dctid;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst4(int who, int which) {
		// Instuction #4 choosed,tell all who choose which champions
		state = 0;
		Tid = -1;
		function = "choosed";
		source = -1;
		dest = who;
		type = which;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst5(int who, int which) {
		// Instuction #5 locked,tell all who locked which champions
		state = 0;
		Tid = -1;
		function = "locked";
		source = -1;
		dest = who;
		type = which;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst6() {
		// Instuction #6 go1,tell all to count down
		state = 0;
		Tid = -1;
		function = "go1";
		source = -1;
		dest = -1;
		type = time;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst7(int whose) {
		// Instuction #7 initial,tell all where i am
		// XY數值調整在randomPosition裡面
		state = 0;
		Tid = -1;
		function = "initial";
		source = -1;
		dest = whose;
		type = -1;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst8() {
		// Instuction #8 go2,tell all game start
		state = 0;
		Tid = -1;
		function = "go2";
		source = -1;
		dest = -1;
		type = -1;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst9(int atktid, double x, double y, int dir) {
		// Instuction #9 atk,tell other i swing my sword
		state = 1;
		Tid = -1;
		function = "atk";
		source = -1;
		dest = atktid;
		type = type;
		X = x;
		Y = y;
		direction = dir;
		Stype = "@";
	}

	public void inst10(int atk2tid, double x, double y, int dir) {
		// Instuction #10 atk2,tell other i shoot shit
		state = 1;
		Tid = -1;
		function = "atk2";
		source = -1;
		dest = atk2tid;
		type = type;
		X = x;
		Y = y;
		direction = dir;
		Stype = "@";
	}

	public void inst11(int atkedtid, double x, double y, int hpnow) {
		// Instuction #11 atked,tell other i got hurt
		state = 1;
		Tid = -1;
		function = "atked";
		source = -1;
		dest = atkedtid;
		type = hpnow;
		X = x;
		Y = y;
		direction = 0;
		Stype = "@";
	}

	public void inst12(int deathtid) {
		// Instuction #12 atked,tell other i got hurt
		state = 1;
		Tid = -1;
		function = "death";
		source = -1;
		dest = deathtid;
		type = -1;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst13(int wontid) {
		// Instuction #13 win,tell all who won
		state = 2;
		Tid = -1;
		function = "win";
		source = -1;
		dest = -1;
		type = wontid;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst14() {
		// Instuction #14 back1,tell all to count down (back)
		state = 2;
		Tid = -1;
		function = "back1";
		source = -1;
		dest = -1;
		type = time;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void inst15() {
		// Instuction #15 back2,tell all to back
		state = 2;
		Tid = -1;
		function = "back2";
		source = -1;
		dest = -1;
		type = -1;
		X = -1;
		Y = -1;
		direction = 0;
		Stype = "@";
	}

	public void instUp(int moveid) {
		state = 1;
		Tid = -1;
		function = "movedup";
		source = -1;
		dest = moveid;
		type = -1;
		direction = 0;
		Stype = "@";
	}

	public void instDown(int moveid) {
		state = 1;
		Tid = -1;
		function = "moveddown";
		source = -1;
		dest = moveid;
		type = -1;
		direction = 0;
		Stype = "@";
	}

	public void instLeft(int moveid) {
		state = 1;
		Tid = -1;
		function = "movedleft";
		source = -1;
		dest = moveid;
		type = -1;
		direction = -1;
		Stype = "@";
	}

	public void instRight(int moveid) {
		state = 1;
		Tid = -1;
		function = "movedright";
		source = -1;
		dest = moveid;
		type = -1;
		direction = 1;
		Stype = "@";

	}
	public void bulletdeath(int atk2tid,int bulletid) {
		state = 1;
		Tid = -1;
		function = "bulletdeath";
		source = -1;
		dest = atk2tid;
		type = bulletid;
		direction = 1;
		Stype = "@";

	}

}