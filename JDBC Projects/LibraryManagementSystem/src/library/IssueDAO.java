
package library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;

public class IssueDAO {

    // Issue a book
    public void issueBook(String memberId, String bookId) {

        String checkMember =
                "SELECT member_id FROM members WHERE member_id = ?";

        String checkBook =
                "SELECT available_copies FROM books "
                + "WHERE book_id = ? FOR UPDATE";

        String insertIssue =
                "INSERT INTO book_issues "
                + "(member_id, book_id, issue_date, due_date) "
                + "VALUES (?, ?, ?, ?)";

        String updateBook =
                "UPDATE books SET available_copies = "
                + "available_copies - 1 "
                + "WHERE book_id = ? AND available_copies > 0";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                // Check member
                try (PreparedStatement ps =
                             con.prepareStatement(checkMember)) {

                    ps.setString(1, memberId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Member ID not found.");
                        }
                    }
                }

                // Check book availability
                try (PreparedStatement ps =
                             con.prepareStatement(checkBook)) {

                    ps.setString(1, bookId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Book ID not found.");
                        }

                        if (rs.getInt("available_copies") <= 0) {
                            throw new SQLException(
                                    "No copies available.");
                        }
                    }
                }

                LocalDate today = LocalDate.now();
                LocalDate due = today.plusDays(14);

                // Decrease available copies
                try (PreparedStatement ps =
                             con.prepareStatement(updateBook)) {

                    ps.setString(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to update book stock.");
                    }
                }

                // Insert issue record
                try (PreparedStatement ps =
                             con.prepareStatement(insertIssue)) {

                    ps.setString(1, memberId);
                    ps.setString(2, bookId);
                    ps.setDate(3, Date.valueOf(today));
                    ps.setDate(4, Date.valueOf(due));

                    ps.executeUpdate();
                }

                con.commit();

                System.out.println("Book issued successfully!");
                System.out.println("Issue Date: " + today);
                System.out.println("Due Date: " + due);

            } catch (SQLException e) {
                con.rollback();
                System.out.println(
                        "Book issue failed: " + e.getMessage());

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Database error: " + e.getMessage());
        }
    }

    // View all issued and returned books
    public void viewIssuedBooks() {

        String sql =
                "SELECT bi.issue_id, bi.member_id, "
                + "m.member_name, bi.book_id, b.title, "
                + "bi.issue_date, bi.due_date, bi.return_date, "
                + "bi.fine_amount "
                + "FROM book_issues bi "
                + "JOIN members m ON bi.member_id = m.member_id "
                + "JOIN books b ON bi.book_id = b.book_id "
                + "ORDER BY bi.issue_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== ISSUED BOOKS ==========");

            System.out.printf(
                    "%-8s %-10s %-18s %-10s %-22s "
                    + "%-12s %-12s %-12s %-10s%n",
                    "Issue ID", "Member ID", "Member Name",
                    "Book ID", "Book Title", "Issue Date",
                    "Due Date", "Return Date", "Fine");

            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------");

            boolean found = false;

            while (rs.next()) {
                found = true;

                Date returnDate = rs.getDate("return_date");
                String returnDateText = returnDate == null
                        ? "Not returned"
                        : returnDate.toString();

                double fine = rs.getDouble("fine_amount");

                System.out.printf(
                        "%-8d %-10s %-18s %-10s %-22s "
                        + "%-12s %-12s %-12s Rs.%-7.2f%n",
                        rs.getInt("issue_id"),
                        rs.getString("member_id"),
                        rs.getString("member_name"),
                        rs.getString("book_id"),
                        rs.getString("title"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        returnDateText,
                        fine);
            }

            if (!found) {
                System.out.println("No issue records found.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Unable to view issued books: " + e.getMessage());
        }
    }
}
