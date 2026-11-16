import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Part 5.2 — SECURE refactor of {@link SimpleFlightUpdater} using a parameterized
 * {@link java.sql.PreparedStatement}.
 * <p>
 * <strong>Why the original was vulnerable:</strong> the previous version built the SQL command by
 * concatenating the raw user input into the query text
 * ({@code "... SET delay_status = '" + userInput + "' WHERE ..."}). An attacker could submit
 * {@code Landed' #}: the {@code '} terminated the string literal early and {@code #} began a MySQL
 * comment, so the engine discarded the {@code WHERE flight_number = 'LX354'} filter and overwrote
 * every row in {@code Arrivals} (all 299 flights).
 * </p>
 * <p>
 * <strong>How PreparedStatement prevents it:</strong> the SQL template is sent to the database and
 * compiled <em>with the {@code ?} placeholder already in place</em>, before any user data is bound.
 * The value supplied through {@link PreparedStatement#setString(int, String)} is transmitted
 * separately as a typed parameter — never parsed as SQL — so the string {@code Landed' #} is treated
 * literally as data. The quote and comment characters lose their special meaning and only the single
 * targeted row can change.
 * </p>
 *
 * @author Student A (Driver) &amp; Student B (Navigator)
 * @version 2.0 (2026-2027)
 */
public class SimpleFlightUpdater_SECURE {

    /** JDBC connection URL for FlightDB (public key retrieval + Jerusalem timezone enabled). */
    private static final String URL =
            "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";

    /** Default MySQL administrative user. */
    private static final String USER = "root";

    /**
     * Prompts for the root password and a new delay status for flight {@code LX354}, then performs
     * the update safely using a parameterized {@link PreparedStatement}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Please enter your MySQL root password: ");
            String password = scanner.nextLine();

            System.out.print("Enter a new delay status for flight 'LX354': ");
            String userInput = scanner.nextLine();

            // Parameterized template: the user value can never alter the SQL structure.
            String sql = "UPDATE Arrivals SET delay_status = ? WHERE flight_number = ?";

            try (Connection conn = DriverManager.getConnection(URL, USER, password);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, userInput);   // bound as data only
                pstmt.setString(2, "LX354");

                int affectedRows = pstmt.executeUpdate();
                System.out.println("Rows affected: " + affectedRows);
                System.out.println("Only flight LX354 was updated — the injection payload was stored literally.");

            } catch (SQLException e) {
                System.err.println("Database update failed!");
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("Message: " + e.getMessage());
            }
        }
    }
}
