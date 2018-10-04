package application;

//--------------------------------------------------------------//
// MyClient.java 2011年04月15日
//--------------------------------------------------------------//
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

//--------------------------------------------------------------//
//程式流程
//---MyClient   --主類別檔
//-1-main()   --主程式進入點
//-2-MyClient()   --   --設定及宣告  
//-3-EstablishConnection() --方法   --建立連線
//-4-class IncomingReader --內部類別  --接收資料
//-5-actionPerformed()  --方法   --按下之動作 
//--------------------------------------------------------------//
//MyClient主類別檔
//--------------------------------------------------------------//
public class MyClient extends JFrame implements ActionListener {
	// 宣告區
	// 設定名子及ip
	String name, ip = "";
	BufferedReader reader;
	PrintStream writer;
	// 建立Socket變數
	Socket sock;
	// 顯示區域
	JTextArea incoming = new JTextArea(15, 50);
	// 輸入區域
	JTextField outgoing = new JTextField(20);
	JLabel jlmane = new JLabel("你的名字：");
	JLabel jlip = new JLabel("輸入ip：");
	JTextField jfmane = new JTextField("無名", 10);
	JTextField jfip = new JTextField("127.0.0.1", 10);
	JLabel state = new JLabel("請輸入你的名字及你的名字");

	MenuBar mBar = new MenuBar();
	// File
	Menu mFile = new Menu("檔案");
	// Save
	MenuItem mFileSave = new MenuItem("儲存檔案");

	// --------------------------------------------------------------//
	// -1-主程式進入點
	// --------------------------------------------------------------//
	public static void main(String[] args) {
		MyClient client = new MyClient(); //
	}

	// --------------------------------------------------------------//
	// -2-設定及宣告視窗物件
	// --------------------------------------------------------------//
	MyClient() {
		// 設定及宣告視窗物件
		// 建立視窗JFrame
		super("多入連線Client端");
		// 用來放mane及ip--設定區域
		JPanel maneipPanel = new JPanel();
		// 建來設定按鍵
		JButton setmaneip = new JButton("連線設定");
		// 按下設定
		setmaneip.addActionListener(this);
		// 加入到JPanel
		maneipPanel.add(jlmane);
		// 名字
		maneipPanel.add(jfmane);
		maneipPanel.add(jlip);
		// 位子
		maneipPanel.add(jfip);
		// 設定
		maneipPanel.add(setmaneip);
		// 排版BorderLayout設定區域在上方----
		getContentPane().add(BorderLayout.NORTH, maneipPanel);
		// JButton("送出")
		JButton sendButton = new JButton("送出");
		// 按下
		sendButton.addActionListener(this);
		// 對話區域-----
		// 設置為 true，則當行的長度大於所分派的寬度時，將換行
		incoming.setLineWrap(true);
		// 設置為 true，則當行的長度大於所分派的寬度時，將在單詞邊界（空白）處換行
		incoming.setWrapStyleWord(true);
		// 不可編輯的
		incoming.setEditable(false);
		// JScrollPane
		JScrollPane qScroller = new JScrollPane(incoming);
		// 垂直滾動
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// 水平滾動
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel mainPanel = new JPanel();
		mainPanel.add(qScroller);
		mainPanel.add(outgoing);
		mainPanel.add(sendButton);
		// 對話區域在中間------
		getContentPane().add(BorderLayout.CENTER, mainPanel);

		// Menu事件
		mFileSave.addActionListener(this);
		// 加入MenuItem
		mFile.add(mFileSave);
		// 加入Menu
		mBar.add(mFile);
		// MenuBar
		setMenuBar(mBar);
		// 狀態區域在下方----
		getContentPane().add(BorderLayout.SOUTH, state);
		setSize(600, 450);
		setVisible(true);
		// 離開
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("離開聊天室");
				System.exit(0);
			}
		});
	}

	// --------------------------------------------------------------//
	// -3-建立連線
	// --------------------------------------------------------------//
	private void EstablishConnection() {
		try {
			// 請求建立連線
			sock = new Socket(ip, 8888);
			// 建立I/O資料流
			InputStreamReader streamReader =
					// 取得Socket的輸入資料流
					new InputStreamReader(sock.getInputStream());
			// 放入暫存區
			reader = new BufferedReader(streamReader);
			// 取得Socket的輸出資料流

			writer = new PrintStream(sock.getOutputStream());
			// 連線成功
			state.setText("網路建立-連線成功");
			System.out.println("網路建立-連線成功");

		} catch (IOException ex) {
			System.out.println("建立連線失敗");
		}
	}

	// --------------------------------------------------------------//
	// -4-接收資料
	// --------------------------------------------------------------//
	public class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					incoming.append(message + '\n');
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// --------------------------------------------------------------//
	// -5-按下之動作
	// --------------------------------------------------------------//
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		// 按下設定
		if (str.equals("連線設定")) {
			// 設定名字
			name = jfmane.getText();
			// 設定ip，此程式目前無作用
			ip = jfip.getText();
			// 狀態
			state.setText("設定" + name + ":" + ip);
			// 建立連線----
			EstablishConnection();
			// 建立接收資料執行緒----
			Thread readerThread = new Thread(new IncomingReader());
			readerThread.start();
			// 按下送出
		} else if (str.equals("送出")) {
			// 不可沒有ip及送出空白
			if ((ip != null) && (outgoing.getText() != "")) {
				try {// 送出資料
					writer.println((outgoing.getText()));
					// 刷新該串流的緩衝。
					writer.flush();
				} catch (Exception ex) {
					System.out.println("送出資料失敗");
				}
				// 清完輸入欄位
				outgoing.setText("");
			}
		} else if (str.equals("儲存檔案")) {
			try {
				FileWriter f = new FileWriter("log.txt");
				f.write(incoming.getText());
				f.close();
				state.setText("儲存檔案成功");
			} catch (IOException e2) {
				state.setText("儲存檔案失敗");
			}
		}
	}
}