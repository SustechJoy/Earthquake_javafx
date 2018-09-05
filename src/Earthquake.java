import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * the model class in the project <br>
 *
 * @version 0.4.0
 */
public class Earthquake {
    private SimpleStringProperty id = null;
    private SimpleStringProperty UTC_date = null;
    private SimpleStringProperty latitude = null;
    private SimpleStringProperty longitude = null;
    private SimpleStringProperty depth = null;
    private SimpleStringProperty magnitude = null;
    private SimpleStringProperty region = null;
    private SimpleStringProperty name = null;


    private static ObservableList<Earthquake> data =
            FXCollections.observableArrayList(
            );


    public Earthquake() {

    }

    public Earthquake(String id, String UTC_date, String latitude, String longitude, String depth, String magnitude, String region, String name) {
        this.id = new SimpleStringProperty(id);
        this.UTC_date = new SimpleStringProperty(UTC_date);
        this.latitude = new SimpleStringProperty(latitude);
        this.longitude = new SimpleStringProperty(longitude);
        this.depth = new SimpleStringProperty(depth);
        this.magnitude = new SimpleStringProperty(magnitude);
        this.region = new SimpleStringProperty(region);
        this.name = new SimpleStringProperty(name);
    }

    /**
     * A method used to get id in the model class
     *
     * @return the id of the earthquake data<br>
     */
    public String getId() {
        return id.get();
    }

    /**
     * A method used to get UTC_date in the model class
     *
     * @return the UTC_date of the earthquake data<br>
     */
    public String getUTC_date() {
        return UTC_date.get();
    }

    /**
     * A method used to get Latitude in the model class
     *
     * @return the Latitude of the earthquake data<br>
     */
    public String getLatitude() {
        return latitude.get();
    }

    /**
     * A method used to get Longitude in the model class
     *
     * @return the Longitude of the earthquake data<br>
     */
    public String getLongitude() {
        return longitude.get();
    }

    /**
     * A method used to get Depth in the model class
     *
     * @return the Depth of the earthquake data<br>
     */
    public String getDepth() {
        return depth.get();
    }

    /**
     * A method used to get Magnitude in the model class
     *
     * @return the Magnitude of the earthquake data<br>
     */
    public String getMagnitude() {
        return magnitude.get();
    }

    /**
     * A method used to get Region in the model class
     *
     * @return the Region of the earthquake data<br>
     */
    public String getRegion() {
        return region.get();
    }

    /**
     * A method used to get area_Name in the model class
     *
     * @return the area_Name of the earthquake data<br>
     */
    public String getName() {
        return name.get();
    }

    /**
     * A method used to init the earthquake data
     *
     * @return the observable list that contains data<br>
     */
    static ObservableList<Earthquake> getInit() {
        data.clear();
        Connection conn = SQLiteUtils.getConnection();
        try {
            Statement st = conn.createStatement();
            String dql = "select * from quakes join plate_areas on " +
                    "quakes.area_id=plate_areas.id join plates " +
                    "on plate_areas.plate1=plates.code or " +
                    "plate_areas.plate2=plates.code";
            ResultSet rs = st.executeQuery(dql);
            addData(data, rs);
        } catch (SQLException e) {
            System.err.println("There is an error with sql, check it please.");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("There is an error with sql close, check it please.");
            }
        }
        return data;
    }

    /**
     * A method used to add queried data into observable list
     */
    private static void addData(ObservableList<Earthquake> data, ResultSet rs) throws SQLException {
        data.clear();
        while (rs.next()) {
            String id = rs.getString("id");
            String UTC_date = rs.getString("UTC_date");
            String latitude = rs.getString("latitude");
            String longitude = rs.getString("longitude");
            String depth = rs.getString("depth");
            String magnitude = rs.getString("magnitude");
            String region = rs.getString("region");
            String area = rs.getString("name");
            data.add(new Earthquake(id, UTC_date, latitude, longitude, depth, magnitude, region, area));
        }
    }

    /**
     * A method used to do search in the earthquake data
     *
     */
    static void getSearch(CheckBox withMag, Slider magSlider, CheckBox withPlate, ComboBox<String> plates,
                          CheckBox withTime, Date SQLcheckInDate, Date SQLcheckOutDate) {
        Connection conn = SQLiteUtils.getConnection();
        ArrayList<String> arr = new ArrayList<>();
        PreparedStatement ps;
        try {
            String sql = "select * from quakes join plate_areas on " +
                    "quakes.area_id=plate_areas.id join plates " +
                    "on plate_areas.plate1=plates.code or " +
                    "plate_areas.plate2=plates.code where ";
            if (withMag.isSelected()) {
                sql += "and magnitude >= ? ";
                DecimalFormat df = new DecimalFormat("#.0");
                double mag = Double.parseDouble(df.format(magSlider.getValue()));
                arr.add(String.valueOf(mag));
            }
            if (withPlate.isSelected()) {
                sql += "and plates.name = ? ";
                arr.add(plates.getValue());
            }
            if (withTime.isSelected()) {
                sql += "and date(SUBSTR(UTC_date,1,10)) BETWEEN ? AND ? ";
                arr.add(SQLcheckInDate.toString());
                arr.add(SQLcheckOutDate.toString());
            }
            sql += ";";
            sql = sql.replaceFirst("and", "");
            if (arr.size() == 0) {
                sql = sql.replaceFirst("where", "");
            }
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < arr.size(); i++) {
                ps.setString(i + 1, arr.get(i));
            }
            ResultSet rs = ps.executeQuery();
            addData(data, rs);

        } catch (SQLException e) {
            System.err.println("There is an error with sql, check it please.");
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A method used to do update in the earthquake data
     *
     */
    static void update(int page) {
        long prev = System.currentTimeMillis();
        try {
            java.sql.Connection conn = SQLiteUtils.getConnection();
            Statement st = conn.createStatement();

            Document doc;
            doc = Jsoup.connect("https://www.emsc-csem.org/Earthquake/?view=" + page).get();
            StringBuilder sql = new StringBuilder("INSERT OR IGNORE INTO quakes(id,UTC_date," +
                    "latitude,longitude," +
                    "depth,magnitude,region) ");
            Element table = doc.getElementById("tbody");

            Elements quakes = table.select("tr");

            for (Element quake : quakes) {
                if (quake.attr("id").startsWith("t")) continue;

                Elements dirs = quake.getElementsByClass("tabev2");
                Elements infos = quake.getElementsByClass("tabev1");
                Element depthElement = quake.getElementsByClass("tabev3").first();
                Element area = quake.getElementsByClass("tb_region").first();
                Element utc_date = quake.select("td.tabev6 > b > a").first();

                String id = quake.attr("id");
                String UTC_date = utc_date.text();
                String latitude;
                if ((dirs.first().text().equals("N"))) {
                    latitude = infos.first().text();
                } else {
                    latitude = "-" + infos.first().text();
                }
                String longitude;
                if (dirs.get(1).text().equals("E")) {
                    longitude = infos.get(1).text();
                } else {
                    longitude = "-" + infos.get(1).text();
                }
                String depth = depthElement.text();
                String magnitude = dirs.last().text();
                String region = area.text();
                if (quake != quakes.first()) {
                    sql.append("UNION ");
                }
                sql.append("SELECT ").append(id).append(",'").append(UTC_date).append("',").append(latitude).append(",").append(longitude).append(","
                ).append(depth).append(",").append(magnitude).append(",\"").append(region).append("\" ");
            }
            System.out.println(sql);
            st.executeUpdate(sql.toString());
            conn.commit();
        } catch (IOException | SQLException e) {
            System.err.println("There is an error with input or sql, check it please.");
            e.printStackTrace();
        } finally {
            long curr = System.currentTimeMillis();
            System.err.println((curr - prev) + "ms");
        }
    }

}