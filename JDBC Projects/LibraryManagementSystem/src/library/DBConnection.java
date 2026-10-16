
package library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/library_db";

    private static final String USER = "root";

    private static final String PASSWORD = "Mahith@999";

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL, USER, PASSWORD
        );
    }

    public static void main(String[] args) {

        try (Connection con = getConnection()) {

            System.out.println(
                    "Connected to MySQL successfully!"
            );

            System.out.println(
                    "Database: " +
                    con.getCatalog()
            );

        } catch (SQLException e) {

            System.out.println(
                    "Database connection failed!"
            );

            e.printStackTrace();
        }
    }
}
