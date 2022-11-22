import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class XChess extends Chess
{
    private ImageView chess;
    private static Image image = new Image("棋盘素材/X.jpg");
    @Override
    public ImageView setXY(PointXY xy) //下棋点击然后错位
    {
        chess = new ImageView(image);
        chess.setX(xy.getX()-90);
        chess.setY(xy.getY()-90);
        return chess;
    }
}
