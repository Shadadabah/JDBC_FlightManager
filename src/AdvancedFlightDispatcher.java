import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Part 3.3 — Batched arrival dispatcher using a single pre-compiled {@link PreparedStatement}.
 * <p>
 * Updates the {@code delay_status} of several {@code Arrivals} rows to a shared value in one batch
 * round-trip. The {@code newStatus} parameter is bound once, outside the loop; only the {@code id}
 * parameter changes per iteration via {@link PreparedStatement#addBatch()}, and the whole set is
 * sent with {@link PreparedStatement#executeBatch()}.
 * </p>
 *
 * @author Student B (Driver) &amp; Student A (Navigator)
 * @version 1.0 (2026-2027)
 */
public class AdvancedFlightDispatcher {

    /** JDBC connection URL for FlightDB (public key retrieval + Jerusalem timezone enabled). */
    private static final String URL =
            "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";

    /** Default MySQL administrative user. */
    private static final String USER = "root";

    /**
     * Updates {@code delay_status} to {@code newStatus} for every id in {@code arrivalIds} using a
     * single parameterized batch.
     *
     * @param conn       active JDBC {@link Connection} to {@code FlightDB}
     * @param arrivalIds the {@code Arrivals.id} values to update
     * @param newStatus  the status string applied to all given ids
     * @throws SQLException if the batch update fails
     */
    public static void updateMultipleDelays(Connection conn, List<Integer> arrivalIds, String newStatus)
            throws SQLException {
        String sql = "UPDATE Arrivals SET delay_status = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the shared status ONCE, outside the loop.
            pstmt.setString(1, newStatus);

            for (Integer id : arrivalIds) {
                pstmt.setInt(2, id);   // only the id changes per iteration
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();

            int totalAffected = 0;
            for (int r : results) {
                // executeBatch may return SUCCESS_NO_INFO (-2); count those as 1 updated row.
                totalAffected += (r >= 0) ? r : 1;
            }

            System.out.println("Batch executed for ids " + arrivalIds + ".");
            System.out.println("Statements in batch: " + results.length +
                               " | Total rows affected: " + totalAffected +
                               " | New status: '" + newStatus + "'");
        }
    }

    /**
     * Prompts for the root password and dispatches a batch update for ids {@code 1, 3, 5}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Please enter your MySQL root password: ");
            String password = scanner.nextLine();

            try (Connection conn = DriverManager.getConnection(URL, USER, password)) {
                System.out.println("Successfully connected to FlightDB!\n");
                updateMultipleDelays(conn, Arrays.asList(1, 3, 5), "Landed - Final");
            } catch (SQLException e) {
                System.err.println("Batch dispatch failed!");
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("Message: " + e.getMessage());
            }
        }
    }
}
