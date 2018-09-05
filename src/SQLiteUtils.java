import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * the class aims to encapsulation <br>
 * the connection by JDBC
 *
 * @version 0.4.0
 */
public class SQLiteUtils {

    private static String driver;
    private static String url;

    static {
        //initialize static attribute
        try {
            Properties prop = new Properties();
            InputStream in = SQLiteUtils.class.getClassLoader()
                    .getResourceAsStream("db.properties");
            prop.load(in);
            driver = prop.getProperty("jdbc.driver");
            url = prop.getProperty("jdbc.url");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * A method used to connect with sqlite database
     *
     * @return the connection connects to the sqlite database
     * @throws ClassNotFoundException may occur when reflection<br>
     *                                met unknown problem
     */
    static Connection getConnection() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println("There is an error with the driver, check it please.");
            e.printStackTrace();
        }
        Connection con = null;
        try {
            con = DriverManager.getConnection(url);
            con.setAutoCommit(false);
        } catch (Exception e) {
            System.err.println("There is an error with sql, check it please.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return con;
    }

}
