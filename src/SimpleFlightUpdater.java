import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Part 3.2 / Part 5.1 — INTENTIONALLY VULNERABLE flight status updater.
 * <p>
 * This class demonstrates an <strong>insecure</strong> Data Access Layer pattern: it builds the
 * SQL {@code UPDATE} by raw string concatenation using a plain {@link java.sql.Statement}.
 * Because the user input is pasted directly into the SQL text, the class is exploitable via
 * SQL Injection. It is used in Part 5.1 to perform a live attack with the payload
 * {@code Landed' #}, which closes the string literal early and comments out the WHERE clause,
 * overwriting all 299 rows.
 * </p>
 * <p>
 * <strong>Do not use this pattern in production.</strong> The secure, parameterized version is
 * provided in {@code SimpleFlightUpdater_SECURE.java} (Part 5.2 refactor).
 * </p>
 *
 * @author Student A (Driver) &amp; Student B (Navigator)
 * @version 1.0 (2026-2027)
 */
public class SimpleFlightUpdater {

    /** JDBC connection URL for FlightDB (public key retrieval + Jerusalem timezone enabled). */
    private static final String URL =
            "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";

    /** Default MySQL administrative user. */
    private static final String USER = "root";

    /**
     * Prompts for the root password and a new delay status for flight {@code LX354}, then runs the
     * update using string concatenation (vulnerable by design).
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Please enter your MySQL root password: ");
            String password = scanner.nextLine();

            System.out.print("Enter a new delay status for flight 'LX354': ");
            String userInput = scanner.nextLine();

            try (Connection conn = DriverManager.getConnection(URL, USER, password);
                 Statement stmt = conn.createStatement()) {

                // VULNERABLE: user input concatenated straight into the SQL string.
                String sql = "UPDATE Arrivals SET delay_status = '" + userInput +
                             "' WHERE flight_number = 'LX354'";

                System.out.println("\nExecuting SQL:\n" + sql + "\n");

                int affectedRows = stmt.executeUpdate(sql);
                System.out.println("Rows affected: " + affectedRows);

                if (affectedRows > 1) {
                    System.out.println("WARNING: more than one row changed — the table may have been corrupted!");
                }

            } catch (SQLException e) {
                System.err.println("Database update failed!");
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("Message: " + e.getMessage());
            }
        }
    }
}
