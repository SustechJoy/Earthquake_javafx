import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * the class aims to start the program <br>
 *
 * @version 0.4.0
 */
public class Main extends Application {
    /**
     * A method used to start the program and set the size of the program
     *
     */
    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("View.fxml"));
            Scene scene = new Scene(root);
            stage.setTitle("Earthquake");
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
