
package library;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnDAO {

    // Fine charged per overdue day
    private static final double FINE_PER_DAY = 5.0;

    public void returnBook(int issueId) {

        String findIssue =
                "SELECT book_id, due_date, return_date "
                + "FROM book_issues WHERE issue_id = ? FOR UPDATE";

        String updateIssue =
                "UPDATE book_issues "
                + "SET return_date = ?, fine_amount = ? "
                + "WHERE issue_id = ? AND return_date IS NULL";

        String updateBook =
                "UPDATE books "
                + "SET available_copies = available_copies + 1 "
                + "WHERE book_id = ? "
                + "AND available_copies < total_copies";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                String bookId;
                LocalDate dueDate;
                LocalDate returnDate = LocalDate.now();

                // 1. Find and lock the issue record
                try (PreparedStatement ps =
                             con.prepareStatement(findIssue)) {

                    ps.setInt(1, issueId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Issue ID not found.");
                        }

                        if (rs.getDate("return_date") != null) {
                            throw new SQLException(
                                    "This book has already been returned.");
                        }

                        bookId = rs.getString("book_id");
                        dueDate = rs.getDate("due_date")
                                .toLocalDate();
                    }
                }

                // 2. Calculate overdue days and fine
                long overdueDays = Math.max(
                        0, ChronoUnit.DAYS.between(
                                dueDate, returnDate));

                double fine = overdueDays * FINE_PER_DAY;

                // 3. Record return date and fine
                try (PreparedStatement ps =
                             con.prepareStatement(updateIssue)) {

                    ps.setDate(1, Date.valueOf(returnDate));
                    ps.setDouble(2, fine);
                    ps.setInt(3, issueId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to update return record.");
                    }
                }

                // 4. Restore one available book copy
                try (PreparedStatement ps =
                             con.prepareStatement(updateBook)) {

                    ps.setString(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to update book availability.");
                    }
                }

                // 5. Commit both changes together
                con.commit();

                System.out.println("\nBook returned successfully!");
                System.out.println("Issue ID: " + issueId);
                System.out.println("Book ID: " + bookId);
                System.out.println("Due Date: " + dueDate);
                System.out.println("Return Date: " + returnDate);
                System.out.println("Overdue Days: " + overdueDays);
                System.out.printf("Fine Amount: Rs. %.2f%n", fine);

                if (fine > 0) {
                    System.out.println(
                            "Please collect the overdue fine.");
                } else {
                    System.out.println(
                            "No overdue fine. Thank you!");
                }

            } catch (SQLException e) {

                con.rollback();

                System.out.println(
                        "Book return failed: " + e.getMessage());
                System.out.println(
                        "Database changes rolled back.");

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Database error: " + e.getMessage());
        }
    }
}
