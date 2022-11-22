import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.scene.shape.Line;

//棋盘类
public class LinePane extends Pane
{
    private final static LinePane linePane = new LinePane();

    private int[][] chessXY = new int[9][2];

    private LinePane()
    {
        this.display();
    }

    public static LinePane getInstance()
    {
        return linePane;
    }

    public void display()
    {
        //棋子横坐标
        int chessX = 120;
        for(int i =0; i < 3 ; i++){
            //棋子纵坐标
            int chessY = 120;
            for(int j =0; j < 3; j++){
                //存储各个棋子的坐标
                chessXY[i*3+j][0] = chessX;
                chessXY[i*3+j][1] = chessY;
                chessY += 200;
            }
            chessX += 200;
        }

        //横
        Line[] line = new Line[4];
        //竖
        Line[] line1 = new Line[4];

        //棋盘
        int startX = 20, startY = 20, endX = 620, endY = 20;
        int startX1= 20, startY1 = 20, endX1 = 20, endY1 = 620;
        for(int i = 0; i < 4; i++) {
            line[i] = new Line(startX,startY,endX,endY);
            line[i].setStroke(Color.BLACK);
            getChildren().add(line[i]);
            startY += 200;
            endY += 200;

            line1[i] = new Line(startX1, startY1, endX1, endY1);
            line1[i].setStroke(Color.BLACK);
            getChildren().add(line1[i]);
            startX1 +=200;
            endX1 +=200;
        }

    }

    public int getChessX(int row, int col)
    {
        return chessXY[row*3+col][0];
    }

    public int getChessY(int row, int col)
    {
        return chessXY[row*3+col][1];
    }


}
