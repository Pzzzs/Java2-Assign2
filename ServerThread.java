import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

//一个游戏进程
class ServerThread extends Thread implements Runnable, GameConstants {
    private int row, col;
    private boolean waiting = true;
    private boolean chessBack = false;

    //游戏线程结束标志
    private boolean exit = false;

    //两个玩家
    private Socket user1;
    private Socket user2;
	int cnt;
    
    //容器
    private CopyOnWriteArrayList<Socket> all = new CopyOnWriteArrayList<Socket>();
    //棋盘
    private int[][] cell = new int[3][3];

    private  DataOutputStream toUser1;
    private  DataOutputStream toUser2;

    public ServerThread(Socket user1, Socket user2, int cnt) {

        this.user1 = user1;
        this.user2 = user2;
		this.cnt = cnt;

        all.add(user1);
        all.add(user2);
        try {
            toUser1 = new DataOutputStream(this.user1.getOutputStream());
            toUser2 = new DataOutputStream(this.user2.getOutputStream());
        }
        catch (IOException e){
        	System.out.println("-----2-----");
        	release();
        }


        //两个接收玩家信息的类定义
        new Thread(new ChatWith(user1, user2)).start();
        new Thread(new ChatWith(user2,user1)).start();

        //初始化棋盘
        for (int i = 0; i < cell.length; i++) {
            for (int j = 0; j < cell[0].length; j++) {
                cell[i][j] = 0;
            }
        }
    }

    @Override
	public void run() {
		System.out.println("GAME"+cnt+" X's turn");
		System.out.println();
		try {
			// 告诉玩家 1 开始
			toUser1.writeInt(0);
		} catch (Exception e) {
			release();
		}
		// 开始一场游戏
		while (!exit) {
			try {
				// 判断是否需要等待
				waitingForPlayer();
				// 玩家一棋子落子
				cell[row][col] = 1;

				// 判断玩家一是否胜出
				if (isWon(1)) {
					System.out.println("GAME"+cnt+" X获胜");
					toUser1.writeInt(1);
					toUser1.writeInt(player1_won);
					toUser2.writeInt(1);
					toUser2.writeInt(player1_won);
					sendMove(toUser2, row, col);
					// 游戏结束
					exit = true;
					break;
				} else if (isFull()) {
					System.out.println("GAME"+cnt+" 平局");
					toUser1.writeInt(1);
					toUser1.writeInt(draw);
					toUser2.writeInt(1);
					toUser2.writeInt(draw);
					sendMove(toUser2, row, col);
					// 游戏结束
					exit = true;
					break;
				} else {
					// 没有玩家胜出，继续游戏
					if (!exit){
						System.out.println("GAME"+cnt+" O's turn");
					}
					toUser2.writeInt(1);
					toUser2.writeInt(keep);
					sendMove(toUser2, row, col);
				}

				// 判断是否需要等待
				waitingForPlayer();
				//玩家二落子
				cell[row][col] = 2;

				if (isWon(2)) {
					System.out.println("GAME"+cnt+" O获胜");
					toUser1.writeInt(1);
					toUser1.writeInt(player2_won);
					toUser2.writeInt(1);
					toUser2.writeInt(player2_won);
					sendMove(toUser1, row, col);
					// 游戏结束
					exit = true;
					break;
				} else {
					// 没有玩家胜出，继续游戏
					if (!exit){
						System.out.println("GAME"+cnt+" X's turn");
					}
					toUser1.writeInt(1);
					toUser1.writeInt(keep);
					sendMove(toUser1, row, col);
				}
			} 
			catch (Exception e) {
				release();
			}
		}
		try {
			toUser2.writeInt(20);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    //判断是否需要等待
    private void waitingForPlayer() throws InterruptedException{
        while(waiting){
            //判断是否悔棋
            if(chessBack){
                cell[row][col] = 0;
                chessBack = false;
            }
            Thread.sleep(100);
        }
        waiting = true;
    }

    //判断是否胜出
    private boolean isWon(int now) {
		for (int i = 0; i < 3; i++) {
			if (cell[i][0] == now && cell[i][1] == now && cell[i][2] == now){
				return true;
			}
		}
		for (int i = 0; i < 3; i++) {
			if (cell[0][i] == now && cell[1][i] == now && cell[2][i] == now){
				return true;
			}
		}
		if (cell[0][0] == now && cell[1][1] == now && cell[2][2] == now){
			return true;
		}
		if (cell[2][0] == now && cell[1][1] == now && cell[0][2] == now){
			return true;
		}
		return false;
    }//寻找相同的棋子

    //移动函数
    private void sendMove(DataOutputStream out, int row, int col) throws IOException {
        out.writeInt(row);
        out.writeInt(col);
    }

    //判断棋盘是否已满
    private boolean isFull() {
        for (int i = 0; i < cell.length; i++) {
            for (int j = 0; j < cell[0].length; j++) {
                if (cell[i][j] == 0) {
                    return false;
                }
            }
        }
        //已满
        return true;
    }

	//关闭流
	private void release() {
		exit = true;
		UtilsConnect.close(user1, user2, toUser1, toUser2);
	}
	

	//用户信息交互
	class ChatWith implements Runnable {
		// 判断数据的类型
		private int x;
		// 获取聊天内容
		private String str1 = "";
		
		private Socket user1;
		private Socket user2;

		private boolean exit;

		private DataInputStream fromUser1;
		private DataOutputStream toUser2;

		public ChatWith(Socket user1, Socket user2) {
			this.user1 = user1;
			this.user2 = user2;
			try {
				fromUser1 = new DataInputStream(this.user1.getInputStream());
				toUser2 = new DataOutputStream(this.user2.getOutputStream());
			} catch (IOException e) {
				release();
			}
		}

		@Override
		public void run() {
			while (!exit) {
				try {
					x = fromUser1.readInt();
					if (x == 2) {
						str1 = fromUser1.readUTF();
						toUser2.writeInt(2);
						toUser2.writeUTF(str1);
					} else if (x == 1) {
						row = fromUser1.readInt();
						col = fromUser1.readInt();
						waiting = false;
					}

				} catch (Exception e) {
					// 玩家异常退出或者逃跑
					System.out.println("GAME"+cnt+" 结束");
					all.remove(user1);
					waiting = false;
					
					try {
						for (Socket socket : all) {
							if (socket == user2)
							    toUser2.writeInt(20);
						}

					} catch (IOException e1) {
						e1.printStackTrace();
					}
					// 释放资源
					release();
				}
			}
			try {
				toUser2.writeInt(20);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//关闭流
		private void release() {
			exit = true;
			UtilsConnect.close(user1, user2, fromUser1, toUser2);
		}

		public int getRow()
		{
			return row;
		}

		public int getCol()
		{
			return col;
		}

		public boolean isChessBack()
		{
			return chessBack;
		}

	}
}
