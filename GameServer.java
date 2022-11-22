import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class GameServer extends Application implements GameConstants {

	TextArea textArea;
	static int cnt=1;

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage)
	{
		//界面
		textArea = new TextArea();
		textArea.setEditable(false);

		Scene scene = new Scene(new Pane(textArea), 550, 200);

		primaryStage.setScene(scene);
		primaryStage.setTitle("Tic-tac-toe GameServer");
		primaryStage.show();

		// 关闭线程
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				System.exit(0);
			}
		});

		new Thread(() ->
		{
			ServerSocket serverSocket = null;
			try
			{
				// 一个服务器端口
				serverSocket = new ServerSocket(1234);
				// 等待用户连接
				while (true)
				{
					Platform.runLater(() ->
					{
						textArea.appendText("GAME"+cnt+"等待玩家连接\n ");
						System.out.println("GAME"+cnt+"等待玩家连接");
						System.out.println();
					});

					// 等待用户一连接
					Socket user1 = serverSocket.accept();
					Platform.runLater(() ->
					{
						textArea.appendText("GAME"+cnt+"玩家一连接成功\n ");
						System.out.println("GAME"+cnt+"玩家一连接成功");
					});
					// IO流
					DataOutputStream toUser1 = new DataOutputStream(user1.getOutputStream());
					// 提示玩家一其为X
					toUser1.writeInt(player1);

					// 等待用户二连接
					Socket user2 = serverSocket.accept();
					Platform.runLater(() ->
					{
						textArea.appendText("GAME"+cnt+"玩家二连接成功\n ");
						textArea.appendText("匹配成功\n ");
						System.out.println("GAME"+cnt+"玩家二连接成功");
						System.out.println("GAME"+cnt+"匹配成功");
					});
					// IO流
					DataOutputStream toUser2 = new DataOutputStream(user2.getOutputStream());
					// 提示玩家二其为O
					toUser2.writeInt(player2);

					// 开始一局游戏
					Thread.sleep(100);
					ServerThread serverThread = new ServerThread(user1, user2, cnt);
					serverThread.gameServer=this;
					new Thread(serverThread).start();
					Thread.sleep(100);
					cnt++;
				}

			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}).start();
	}
}
