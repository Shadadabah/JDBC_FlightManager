import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Part 4 — Independent prompting challenge. Secure, read-only arrivals viewer filtered by terminal.
 * <p>
 * Connects to {@code FlightDB}, prompts for a target terminal (valid values in the dataset: 2, 3, 4,
 * 5), and prints an {@code INNER JOIN} of {@code Arrivals} and {@code FlightRoutes} for that terminal,
 * ordered chronologically by {@code scheduled}. The terminal filter is bound through a parameterized
 * {@link PreparedStatement} ({@code setString}, since {@code terminal} is {@code VARCHAR(10)}), so the
 * query is injection-safe.
 * </p>
 *
 * @author Student A &amp; Student B (Joint Work)
 * @version 1.0 (2026-2027)
 */
public class FlightViewer {

    /** JDBC connection URL for FlightDB (public key retrieval + Jerusalem timezone enabled). */
    private static final String URL =
            "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";

    /** Default MySQL administrative user. */
    private static final String USER = "root";

    /**
     * Prompts for the root password and a terminal number, then prints the sorted, joined arrivals
     * for that terminal.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Please enter your MySQL root password: ");
            String password = scanner.nextLine();

            System.out.print("Enter a terminal number to view (2, 3, 4, or 5): ");
            String terminal = scanner.nextLine().trim();

            String sql = "SELECT a.id, a.flight_number, r.from_city, a.scheduled, a.delay_status, a.terminal " +
                         "FROM Arrivals a " +
                         "INNER JOIN FlightRoutes r ON a.flight_number = r.flight_number " +
                         "WHERE a.terminal = ? " +
                         "ORDER BY a.scheduled ASC";

            try (Connection conn = DriverManager.getConnection(URL, USER, password);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, terminal);   // terminal is VARCHAR(10) -> bind as String

                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("\n --- Arrivals for Terminal " + terminal + " (sorted by schedule) ---");
                    System.out.printf("%-5s | %-10s | %-15s | %-10s | %-20s | %-10s%n",
                            "ID", "Flight", "From", "Scheduled", "Status", "Terminal");
                    System.out.println("--------------------------------------------------------------------------------------");

                    int rowCount = 0;
                    while (rs.next()) {
                        System.out.printf("%-5d | %-10s | %-15s | %-10s | %-20s | %-10s%n",
                                rs.getInt("id"), rs.getString("flight_number"), rs.getString("from_city"),
                                rs.getString("scheduled"), rs.getString("delay_status"), rs.getString("terminal"));
                        rowCount++;
                    }
                    System.out.println("--------------------------------------------------------------------------------------");
                    System.out.println("Total flights for terminal " + terminal + ": " + rowCount);
                }

            } catch (SQLException e) {
                System.err.println("Failed to fetch flight data.");
                System.err.println("Message: " + e.getMessage());
            }
        }
    }
}
