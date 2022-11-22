import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.*;

public class GameClient extends Application implements GameConstants {

  private static LinePane linePane = LinePane.getInstance();

  private Button btBegin = new Button("开始游戏");
  private Button btExit = new Button("退出游戏");
  private Button btBack = new Button("退出");
  private Label myTimeChess =  new Label("");
  private Label yourChess = new Label("");

  //主窗口的坐标
  public static double x;
  public static double y;

  //聊天内容
  private String str = null;
  //等待接收数据
  private boolean stop = true;
  //判断游戏进程
  private int data;

  //棋盘
  private static int[][] chess = new int[3][3];
  //棋子
  private ImageView[][] circles = new ImageView[3][3];
  //确认是哪个玩家
  private int player = 0;
  //我的棋子颜色
  private int myChess = 0;
  //对手棋子颜色
  private int otherChess = 0;
  //我的步骤
  private boolean myTurn = false;
  //等待玩家下棋
  private boolean waiting = true;
  //当前棋子坐标
  private int rowNow;
  private int colNow;
  //游戏是否继续
  private boolean continueToPlay = true;

  private DataInputStream fromServer;
  private DataOutputStream toServer;

  public static void main(String[] args) {
        launch(args);
    }

  @Override
  public void start(Stage primaryStage) {

    //提示语字体粗细
    myTimeChess.setFont(new Font(30));
    myTimeChess.setTextFill(Color.BLACK);
    yourChess.setFont(new Font(30));
    yourChess.setTextFill(Color.BLACK);

    //游戏进入界面
    //Stage primaryStage = new Stage();
    Label label = new Label("\n  Tic-tac-toe\n  ");
    label.setFont(new Font(100));
    btBegin.setFont(new Font(50));
    btExit.setFont(new Font(50));
    btBack.setFont(new Font(30));

    HBox hBox1 = new HBox(15);
    hBox1.getChildren().add(btBegin);
    hBox1.setPadding(new Insets(0,0,0,190));

    HBox hBox2 = new HBox(15);
    hBox2.getChildren().add(btExit);
    hBox2.setPadding(new Insets(0,0,0,190));

    VBox vBox1 = new VBox(15);
    vBox1.getChildren().addAll(label,hBox1,hBox2);

    Pane pane1 = new Pane(); //pane1是初始页面
    pane1.getChildren().add(vBox1);

    Scene scene1 = new Scene(pane1,640,740);
    primaryStage.setResizable(false);
    primaryStage.setTitle("Tic-tac-toe");
    primaryStage.setScene(scene1);
    primaryStage.show();

    //获取主窗口的坐标
    new Thread(() ->
      {
        try {
          while (true) {
            x = primaryStage.getX();
            y = primaryStage.getY();
            Thread.sleep(100);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }

      }).start();

      //游戏界面
      HBox hBox = new HBox(20);
      hBox.getChildren().addAll(btBack, new StackPane(yourChess), new StackPane(myTimeChess));
      hBox.setPadding(new Insets(30, 300, 0, 30));

      BorderPane borderPane = new BorderPane();
      borderPane.setCenter(new Label("  "));
      borderPane.setLeft(linePane);
      borderPane.setBottom(hBox);

      Pane pane2 = new Pane();
      pane2.getChildren().add(new ImageView("棋盘素材/棋盘背景.jpg"));
      pane2.getChildren().add(borderPane);


      //开始游戏
      btBegin.setOnAction(e -> {
        //连接服务器
        ConnectServer.connectionAgain();
        //初始化棋盘
        initializeChess();
        //游戏开始
        connectToServer();
        scene1.setRoot(pane2);
      });
      //退出游戏
      btExit.setOnAction(event -> {
        System.exit(0);
      });

      //退出对战
      btBack.setOnAction(e ->{
        continueToPlay = false;
        waiting = false;
        stop = false;
        release();
        scene1.setRoot(pane1);
      });

      //关闭线程
      primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
        @Override
          public void handle(WindowEvent event) {
                System.exit(0);
            }
      });
  }

    //游戏线程
    private void connectToServer()
    {
        try {
            //连接服务器建立IO流
            fromServer = ConnectServer.getInstance().getDataInputStream();
            toServer = ConnectServer.getInstance().getDataOutputStream();
        }
        catch (IOException e) {
        }

        new Thread(() -> {
            try {
                player = fromServer.readInt();


                if (player == player1) {
                    myChess = 1;
                    otherChess = 2;

                    Platform.runLater(() -> {
                        //棋子颜色
                        yourChess.setText("X");

                        new PopUpWindow().display("\n连接成功，请等待匹配\n ");
                    });
                    //等待用户二连接
                    fromServer.readInt();

                    Platform.runLater(() -> {
                        new PopUpWindow().display("\n匹配成功，您是先手，请落子\n ");
                        //界面提示
                        myTimeChess.setText("Your turn");
                    });
                    //接收数据
                    getData();

                    myTurn = true;
                }
                else if (player == player2) {
                    myChess = 2;
                    otherChess = 1;
                    Platform.runLater(() -> {

                        yourChess.setText("O");
                        new PopUpWindow().display("\n匹配成功，您是后手，请等待对手落子\n ");
                        //界面提示
                        myTimeChess.setText("Opponent's turn");
                    });
                    //接收数据
                    getData();

                }

                //游戏继续
                while (continueToPlay)
                {
                    if (player == player1)
                    {
                        enterTheChess(player);
                        waitingForThePlayer();
                        sendMove();
                        receiveInfoFromServer();
                    }
                    else if (player == player2)
                    {
                        receiveInfoFromServer();
                        enterTheChess(player);
                        waitingForThePlayer();
                        //判断玩家是否中途退出
                        sendMove();
                    }
                }
            }
            catch (Exception e) 
            {
            	release();
            }
        }).start();

    }

    //接收数据
    private void getData()
    {
        new Thread(()->{

			while (continueToPlay)
			{
				// 接收数据
				int index;
				try
				{
					index = fromServer.readInt();
					// 游戏数据
					if (index == 1) {
						data = fromServer.readInt();
						stop = false;
						if (data == player1_won)
						{
							if (myChess == 2)
							{
								receiveMove();
							}
							// 退出
							break;
						} 
						else if (data == player2_won)
						{
							if (myChess == 1)
							{
								receiveMove();
							}
							break;
						} 
						else if (data == draw)
						{
							if (myChess == 2)
							{
								receiveMove();
							}
							break;
						} else
						{
							receiveMove();
						}
					}

					else if (index == 20) {
						if (continueToPlay) {
							Platform.runLater(() ->
							{
								new PopUpWindow().display("\n对手已断开连接\n ");
							});
							myTurn = false;
							// 释放所有等待后结束游戏
							continueToPlay = false;
						}
						if (stop)
							stop = false;
						if (waiting)
							waiting = false;
						break;
					}
				}
				catch (IOException e) {
					continueToPlay = false;
					release();
				}
			}
            if (data!=3 && data!=4 && data!=5){
                System.out.println("连接断开，游戏结束");
            }
        }).start();
    }

    //等待玩家下棋
    private void waitingForThePlayer() throws InterruptedException {
        while(waiting) {
            Thread.sleep(100);
        }
        waiting = true;
    }
    //等待接收数据
    private void waitingForElement() throws InterruptedException {
        while(stop) {
            Thread.sleep(100);
        }
        stop = true;
    }

    //传送棋子坐标到服务器
    private void sendMove() throws IOException {
        toServer.writeInt(1);
        toServer.writeInt(rowNow);
        toServer.writeInt(colNow);
    }

    //接收服务器数据判断游戏进程
    private void receiveInfoFromServer() throws Exception {

        //等待接收服务器的数据
        waitingForElement();
        if(!continueToPlay){
            return;
        }
        if (data == player1_won) {
            //结束游戏
            continueToPlay = false;
            //不再等待玩家下棋
            waiting = false;
            toServer.writeInt(8);
            if (myChess == 1) {
                Platform.runLater(() -> {
                    new PopUpWindow().display("\n你赢了\n ");
                });
            } else if (myChess == 2) {
                //receiveMove();
                Platform.runLater(() -> {
                    new PopUpWindow().display("\n你输了\n ");
                });
            }
        }
        else if (data == player2_won) {
            continueToPlay = false;
            //不再等待玩家下棋
            waiting = false;
            if (myChess == 2) {
                Platform.runLater(() -> {
                    new PopUpWindow().display("\n你赢了\n ");
                });
            } else if (myChess == 1) {
                //receiveMove();
                Platform.runLater(() -> {
                    new PopUpWindow().display("\n你输了\n ");
                });
            }
        }
        else if (data == draw) {
            continueToPlay = false;
            //不再等待玩家下棋
            waiting = false;
            Platform.runLater(() -> {
                new PopUpWindow().display("\n平局\n ");
            });
        } else {
            //receiveMove();
            myTurn = true;
        }
    }

    //接收服务器传输的对手棋子坐标
    private void receiveMove() throws IOException
    {
        //获取对手棋子坐标
        int row = fromServer.readInt();
        int col = fromServer.readInt();

        //打印对手新下的棋子
        otherEnterChess(row, col);
    }

    //我的落子
    private void enterTheChess(int player)
    {
        linePane.setOnMouseClicked(e1 -> {

            if (myTurn) {
                flag:
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        //利用棋子坐标与鼠标当前坐标的距离判断落子
                        double distance = Math.sqrt(Math.pow((e1.getSceneX() - linePane.getChessX(i, j)), 2)
                                + Math.pow((e1.getSceneY() - linePane.getChessY(i, j)), 2));
                        if (distance < 100 && chess[i][j] == 0) {

                            if (player == player1)
                            {
                                circles[i][j] = ChessFactory.getInstance().getChess("X")
                                        .setXY(new PointXY(linePane.getChessX(i, j),linePane.getChessY(i, j)));
                                chess[i][j] = 1;
                            }
                            else if(player == player2)
                            {
                                circles[i][j] = ChessFactory.getInstance().getChess("O")
                                        .setXY(new PointXY(linePane.getChessX(i, j),linePane.getChessY(i, j)));
                                chess[i][j] = 2;
                            }
                            linePane.getChildren().add(circles[i][j]);

                            //储存当前棋子坐标
                            rowNow = i;
                            colNow = j;
                            myTurn = false;
                            waiting = false;
                            myTimeChess.setText("Opponent's turn");
                            break flag;
                        }
                    }
                }
            }

        });
    }

    //对手落子
    private void otherEnterChess(int row, int col)
    {
        Platform.runLater(() -> {
            if (myChess == 1) {
                circles[row][col] = ChessFactory.getInstance().getChess("O")
                        .setXY(new PointXY(linePane.getChessX(row, col),linePane.getChessY(row, col)));
                chess[row][col] = 2;
            }
            else {
                circles[row][col] = ChessFactory.getInstance().getChess("X")
                        .setXY(new PointXY(linePane.getChessX(row, col),linePane.getChessY(row, col)));
                chess[row][col] = 1;
            }
            linePane.getChildren().add(circles[row][col]);
            //标记新下的棋子

            myTimeChess.setText("Your turn");
        });
    }

    //重新开始游戏时初始化棋盘
    private void initializeChess()
    {
        //清空棋盘
        linePane.getChildren().clear();
        linePane.display();

        //初始化棋盘数据
        for(int i =0; i < chess.length; i++)
        {
            for(int j = 0; j < chess[0].length; j++)
            {
                if(chess[i][j] != 0)
                {
                    circles[i][j] = null;
                    chess[i][j] = 0;
                }
            }
        }
        //初始化游戏控制量
        continueToPlay = true;
        player = 0;
        myChess = 0;
        stop = true;
        myTurn = false;
        waiting = true;
        myTimeChess.setText("");
        otherChess = 0;
    }

    //关闭流
	private void release()
	{
		continueToPlay = false;
		UtilsConnect.close(ConnectServer.getSocket(), toServer, fromServer);
	}
}
