import java.io.Closeable;
import java.io.IOException;

//关闭连接的工具类
public class UtilsConnect {
	public static void close(Closeable ... connections) {
		for (Closeable connect : connections) {
			if(connect != null) {
				try {
					connect.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
