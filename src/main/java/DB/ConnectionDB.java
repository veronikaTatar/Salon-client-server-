package DB;
import java.sql.*;
import java.util.ArrayList;

public class ConnectionDB {
    private static ConnectionDB instance;

    protected String dbHost = "127.0.0.1";
    protected String dbPort = "3306";
    protected String dbUser = "root";
    protected String dbPass = "sQl2025_NIKA";
    protected String dbName = "salon";

    ArrayList<String[]> masResult;

    public static Connection dbConnection;
    private Statement statement;
    private ResultSet resultSet;

    public ConnectionDB()
            throws ClassNotFoundException, SQLException {
        String connectionString = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
        //+ "?verifyServerCertificate=false"+
        //"&useSSL=false"+
        //       "&requireSSL=false"+
        //"&useLegacyDatetimeCode=false"+
        //      "&amp"+
        //      "&serverTimezone=UTC";

        Class.forName("com.mysql.cj.jdbc.Driver");

        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPass);
        statement = dbConnection.createStatement();
    }

    public void execute(String query) {
        try {
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // обеспечивает создание единственного экземпляра подключения к базе данныхСИНГЛТОН
    //подобно SELECT
    public static synchronized ConnectionDB getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new ConnectionDB();
        }
        return instance;
    }

    public ArrayList<String[]> getArrayResult(String str) {
        masResult = new ArrayList<String[]>();
        try {
            resultSet = statement.executeQuery(str);
            int count = resultSet.getMetaData().getColumnCount();

            while (resultSet.next()) {
                String[] arrayString = new String[count];
                for (int i = 1;  i <= count; i++)
                    arrayString[i - 1] = resultSet.getString(i);

                masResult.add(arrayString);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return masResult;
    }

    public PreparedStatement prepareStatement(String sql) {
        try {
            return dbConnection.prepareStatement(sql);
        } catch (SQLException e) {
            System.out.println("Error preparing statement.");
            e.printStackTrace();
            return null; // Или можно выбросить исключение
        }
    }

    public CallableStatement prepareCall(String proc) {
        try {
            // Создаем и возвращаем CallableStatement для выполнения хранимой процедуры
            return dbConnection.prepareCall(proc);
        } catch (SQLException e) {
            System.out.println("Error preparing callable statement.");
            e.printStackTrace();
            return null; // Или можно выбросить исключение
        }
    }
}
