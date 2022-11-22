import java.util.HashMap;

public class ChessFactory
{
    private static ChessFactory chessFactory = new ChessFactory();
    //享元池
    private static HashMap<String, Chess> hashMap;

    private ChessFactory()
    {
        hashMap = new HashMap<>();
        Chess XChess, OChess;
        XChess = new XChess();
        OChess = new OChess();

        hashMap.put("X", XChess);
        hashMap.put("O", OChess);
    }

    public static ChessFactory getInstance()
    {
        return chessFactory;
    }

    public Chess getChess(String type)
    {
        return hashMap.get(type);
    }

}
