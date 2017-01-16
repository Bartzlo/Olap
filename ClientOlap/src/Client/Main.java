package Client;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

    public static NetWork netWork;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Olap");
        primaryStage.setScene(scene);
        primaryStage.show();

        Controller con = loader.getController();

        netWork = new NetWork();
        con.fillQueryLists(netWork.getProdCategory(), netWork.getShipCountry());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
