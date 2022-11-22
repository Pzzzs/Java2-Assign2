import java.io.*;
import java.net.Socket;

//获取服务器连接
public class ConnectServer {
    //单例模式
    private final static ConnectServer connect = new ConnectServer();
    private static Socket socket;
    private ConnectServer() {
    }

    //连接服务器
    public static void connectionAgain() {
        try {
            //建立连接（本地连接）
            socket = new Socket("localhost", 1234);
        }
        catch (Exception e){
            new PopUpWindow().display("\n连接服务器失败\n ");
        	System.out.println("连接服务器失败");
        	receive();
        }
    }

    public static ConnectServer getInstance() {
        return connect;
    }

    //获取IO连接
    public DataInputStream getDataInputStream() throws IOException {
        return new DataInputStream(socket.getInputStream());
    }

    //获取IO连接
    public DataOutputStream getDataOutputStream() throws IOException {
        return new DataOutputStream(socket.getOutputStream());
    }

	public static Socket getSocket() {
		return socket;
	}
	
	private static void receive() {
		UtilsConnect.close(socket);
	}
}








