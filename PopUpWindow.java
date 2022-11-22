import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PopUpWindow
{
    private Stage window = new Stage();
    private Scene scene =  new Scene(new Pane(),300,130);
    private Label label = new Label();
    private Button btYes = new Button("确定");

    public PopUpWindow()
    {
        window.setResizable(false);

        window.initStyle(StageStyle.UNDECORATED);
        //居中
        window.setX(GameClient.x + 180);
        window.setY(GameClient.y + 300);
        window.setScene(scene);
        window.initModality(Modality.APPLICATION_MODAL);
    }

    public void display(String s)
    {
        label.setText(s);
        btYes.setOnAction(e -> window.close());
        VBox vBox = new VBox(new StackPane(label),new StackPane(btYes));
        vBox.setPadding(new Insets(10,0,10,0));
        scene.setRoot(new StackPane(vBox));
        window.showAndWait();
    }

}
