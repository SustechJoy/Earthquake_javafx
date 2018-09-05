import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.net.URL;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * the controller class in the project <br>
 * that implements the detailed functions
 *
 * @version 1.0.0
 */
public class Controller implements Initializable {


    private static ObservableList<Earthquake> data =
            FXCollections.observableArrayList(
            );
    private static ObservableList<String> plate = FXCollections.observableArrayList(
            "Scotia", "Antarctic", "African", "Arabian", "Eurasian", "Indian", "Australian", "Juan de Fuca",
            "Caribbean", "North-American", "Nazca", "Pacific", "Filipino", "South-American");
    private static ObservableList<String> magnitudes = FXCollections.observableArrayList(
            "1", "2", "3", "4", "5", "6", "7", "8");
    @FXML
    private TableView<Earthquake> quakeTable;
    @FXML
    private TableColumn<Earthquake, String> idCol;
    @FXML
    private TableColumn<Earthquake, String> UTC_dateCol;
    @FXML
    private TableColumn<Earthquake, String> latitudeCol;
    @FXML
    private TableColumn<Earthquake, String> longitudeCol;
    @FXML
    private TableColumn<Earthquake, String> depthCol;
    @FXML
    private TableColumn<Earthquake, String> magnitudeCol;
    @FXML
    private TableColumn<Earthquake, String> regionCol;
    @FXML
    private TableColumn<Earthquake, String> areaCol;
    @FXML
    private DatePicker checkInDatePicker;
    @FXML
    private DatePicker checkOutDatePicker;
    @FXML
    private ImageView quakeImage;
    @FXML
    private Canvas canvas;
    @FXML
    private Slider magSlider;
    @FXML
    private Label magLabel;
    @FXML
    private CheckBox withMag;
    @FXML
    private CheckBox withPlate;
    @FXML
    private CheckBox withTime;
    @FXML
    private ComboBox<String> plates;
    @FXML
    private CategoryAxis magAxis;
    @FXML
    private BarChart<String, Integer> magChart;
    @FXML
    private CategoryAxis areaAxis;
    @FXML
    private LineChart<String, Integer> areaChart;
    @FXML
    private PieChart seasonChart;

    /**
     * A method used to initialize all the widgets in the controller class
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        init();
        quakeTable.setEditable(true);
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        UTC_dateCol.setCellValueFactory(
                new PropertyValueFactory<>("UTC_date"));
        latitudeCol.setCellValueFactory(
                new PropertyValueFactory<>("latitude"));
        longitudeCol.setCellValueFactory(
                new PropertyValueFactory<>("longitude"));
        depthCol.setCellValueFactory(
                new PropertyValueFactory<>("depth"));
        magnitudeCol.setCellValueFactory(
                new PropertyValueFactory<>("magnitude"));
        regionCol.setCellValueFactory(
                new PropertyValueFactory<>("region"));
        areaCol.setCellValueFactory(
                new PropertyValueFactory<>("name"));
        idCol.setCellFactory(TextFieldTableCell.forTableColumn());
        UTC_dateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        latitudeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        longitudeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        depthCol.setCellFactory(TextFieldTableCell.forTableColumn());
        magnitudeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        regionCol.setCellFactory(TextFieldTableCell.forTableColumn());
        areaCol.setCellFactory(TextFieldTableCell.forTableColumn());
        magAxis.setCategories(magnitudes);
        areaAxis.setCategories(plate);
        plates.setItems(plate);
        checkInDatePicker.setValue(LocalDate.now());
        checkOutDatePicker.setValue(checkInDatePicker.getValue().plusDays(1));
    }

    /**
     * A method used to initialize data<br>
     * in the TableView in the controller class
     */
    private void init() {
        data = Earthquake.getInit();
        quakeTable.setItems(data);
        paint();
    }

    /**
     * A method used to paint data<br>
     * on the canvas in the controller class
     */
    private void mapPaint() {
        double width = quakeImage.getFitWidth();
        double height = quakeImage.getFitHeight();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(new Color(1, 0, 0, 0.3));


        //clear the canvas
        gc.clearRect(0, 0, width, height);

        for (int i = 0; i < data.size(); i++) {
            Earthquake eq = quakeTable.getItems().get(i);
            double latitude = Double.parseDouble(eq.getLatitude());
            double longitude = Double.parseDouble(eq.getLongitude());

            double mapLatitude = height / 2 - latitude * height / 180;
            double mapLongitude = width / 2 + longitude * width / 360;
            gc.fillOval(mapLongitude, mapLatitude, 5, 5);
        }
    }

    /**
     * A method used to paint data<br>
     * on the BarChart in the controller class
     */
    private void magPaint() {
        int[] magCounter = new int[8];
        for (int i = 0; i < data.size(); i++) {
            Earthquake eq = quakeTable.getItems().get(i);
            int magnitude = Integer.parseInt(String.valueOf(eq.getMagnitude().charAt(0)));
            magCounter[magnitude - 1]++;
        }
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        for (int i = 0; i < magCounter.length; i++) {
            series.getData().add(new XYChart.Data<>(magnitudes.get(i), magCounter[i]));
        }
        magChart.getData().clear();
        magChart.getData().add(series);
    }

    /**
     * A method used to paint data<br>
     * on the PieChart in the controller class
     */
    private void seasonPaint() {
        int[] seasonCounter = new int[4];
        String season[] = {"Spring(Month 1-3)", "Summer(Month 4-6)", "Fall(Month 7-9)", "Winter(Month 10-12)"};
        for (int i = 0; i < data.size(); i++) {
            Earthquake eq = quakeTable.getItems().get(i);
            int month = Integer.parseInt(eq.getUTC_date().substring(5, 7)) - 1;
            seasonCounter[month / 3]++;
        }
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();
        for (int i = 0; i < 4; i++) {
            pieChartData.add(new PieChart.Data(season[i], seasonCounter[i]));
        }
        seasonChart.setData(pieChartData);
    }

    /**
     * A method used to paint data<br>
     * on the LineChart in the controller class
     */
    private void areaPaint() {
        int[] areaCounter = new int[plate.size()];
        for (int i = 0; i < data.size(); i++) {
            Earthquake eq = quakeTable.getItems().get(i);
            ArrayList<String> list = new ArrayList<>(plate);
            for (int j = 0; j < list.size(); j++) {
                if (eq.getName().equals(list.get(j))) {
                    areaCounter[j]++;
                }
            }
        }
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        for (int i = 0; i < areaCounter.length; i++) {
            series.getData().add(new XYChart.Data<>(plate.get(i), areaCounter[i]));
        }
        areaChart.getData().clear();
        areaChart.getData().add(series);
    }

    /**
     * A method used to change the text of the label<br>
     * that displays the selected magnitude
     */
    @FXML
    public void changeText() {
        DecimalFormat df = new DecimalFormat("#.0");
        double mag = Double.parseDouble(df.format(magSlider.getValue()));
        magLabel.setText(">=" + String.valueOf(mag));
    }

    /**
     * A method used to search data<br>
     * and display data in the TableView in the controller class
     */
    @FXML
    public void search() {
        LocalDate checkInDate = checkInDatePicker.getValue();
        LocalDate checkOutDate = checkOutDatePicker.getValue();
        Date SQLcheckInDate = Date.valueOf(checkInDate);
        Date SQLcheckOutDate = Date.valueOf(checkOutDate);
        Earthquake.getSearch(withMag, magSlider, withPlate, plates, withTime, SQLcheckInDate, SQLcheckOutDate);
        paint();
    }

    /**
     * A method used to clear all the search<br>
     * and display original data in the TableView in the controller class
     */
    @FXML
    public void clear() {
        init();
        paint();
    }

    /**
     * A method used to encapsulate painting methods
     */
    private void paint() {
        mapPaint();
        magPaint();
        seasonPaint();
        areaPaint();
    }

    /**
     * A method used to refresh data<br>
     * and display data in the TableView in the controller class
     */
    @FXML
    public void refresh() {
        ChoiceDialog dialog = new ChoiceDialog<>();
        ProgressBar p = new ProgressBar();
        Service<Integer> service = new Service<Integer>() {

            @Override
            protected Task<Integer> createTask() {
                return new Task<Integer>() {

                    @Override
                    protected Integer call() throws Exception {
                        int i = 1;
                        while (i < 6) {
                            Earthquake.update(i);
                            updateProgress((i++) * 20, 100);
                        }
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        dialog.setHeaderText("Done!");
                        super.succeeded();
                    }
                };
            }
        };

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Do you want to refresh the data?");
        alert.setContentText("It would takes a few seconds.");
        Optional result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == ButtonType.OK) {
                dialog.setHeaderText("Wait...");
                p.setProgress(0);
                p.setPrefWidth(220);
                p.progressProperty().bind(service.progressProperty());
                dialog.getDialogPane().setContent(p);
                dialog.show();
                service.restart();
            } else {
                alert.close();
            }
        }
    }
}
