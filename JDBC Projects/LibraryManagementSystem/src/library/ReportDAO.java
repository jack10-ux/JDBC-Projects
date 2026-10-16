
package library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportDAO {

    // 1. Book Summary Report
    public void displayBookSummary() {

        String sql = "SELECT COUNT(*) AS total_titles, "
                + "COALESCE(SUM(total_copies), 0) AS total_copies, "
                + "COALESCE(SUM(available_copies), 0) AS available_copies "
                + "FROM books";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int totalCopies = rs.getInt("total_copies");
                int availableCopies = rs.getInt("available_copies");

                System.out.println("\n========== BOOK SUMMARY ==========");
                System.out.println("Total Book Titles: "
                        + rs.getInt("total_titles"));
                System.out.println("Total Copies: " + totalCopies);
                System.out.println("Available Copies: " + availableCopies);
                System.out.println("Issued Copies: "
                        + (totalCopies - availableCopies));
            }

        } catch (SQLException e) {
            System.out.println("Unable to generate report: "
                    + e.getMessage());
        }
    }

    // 2. Member Issue Report
    public void displayMemberIssueReport() {

        String sql = "SELECT m.member_id, m.member_name, "
                + "COUNT(i.issue_id) AS total_issues "
                + "FROM members m "
                + "LEFT JOIN book_issues i ON m.member_id = i.member_id "
                + "GROUP BY m.member_id, m.member_name "
                + "ORDER BY total_issues DESC, m.member_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== MEMBER ISSUE REPORT ==========");
            System.out.printf("%-15s %-30s %-15s%n",
                    "Member ID", "Member Name", "Total Issues");

            boolean found = false;

            while (rs.next()) {
                System.out.printf("%-15s %-30s %-15d%n",
                        rs.getString("member_id"),
                        rs.getString("member_name"),
                        rs.getInt("total_issues"));
                found = true;
            }

            if (!found) {
                System.out.println("No members found.");
            }

        } catch (SQLException e) {
            System.out.println("Unable to generate report: "
                    + e.getMessage());
        }
    }

    // 3. Overdue Books Report
    public void displayOverdueBooks() {

        String sql = "SELECT i.issue_id, m.member_name, "
                + "b.title, i.due_date "
                + "FROM book_issues i "
                + "JOIN members m ON i.member_id = m.member_id "
                + "JOIN books b ON i.book_id = b.book_id "
                + "WHERE i.return_date IS NULL "
                + "AND i.due_date < CURDATE() "
                + "ORDER BY i.due_date";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== OVERDUE BOOKS ==========");
            System.out.printf("%-12s %-25s %-30s %-15s%n",
                    "Issue ID", "Member Name", "Book Title", "Due Date");

            boolean found = false;

            while (rs.next()) {
                System.out.printf("%-12d %-25s %-30s %-15s%n",
                        rs.getInt("issue_id"),
                        rs.getString("member_name"),
                        rs.getString("title"),
                        rs.getDate("due_date"));
                found = true;
            }

            if (!found) {
                System.out.println("No overdue books found.");
            }

        } catch (SQLException e) {
            System.out.println("Unable to generate report: "
                    + e.getMessage());
        }
    }

    // 4. Fine Report
    public void displayFineReport() {

        String sql = "SELECT i.issue_id, m.member_name, b.title, "
                + "i.due_date, i.return_date, i.fine_amount "
                + "FROM book_issues i "
                + "JOIN members m ON i.member_id = m.member_id "
                + "JOIN books b ON i.book_id = b.book_id "
                + "WHERE i.fine_amount > 0 "
                + "ORDER BY i.fine_amount DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== FINE REPORT ==========");
            System.out.printf(
                    "%-10s %-22s %-25s %-13s %-13s %-10s%n",
                    "Issue ID", "Member", "Book Title",
                    "Due Date", "Return Date", "Fine");

            double totalFine = 0.0;
            boolean found = false;

            while (rs.next()) {
                double fine = rs.getDouble("fine_amount");

                System.out.printf(
                        "%-10d %-22s %-25s %-13s %-13s Rs. %-8.2f%n",
                        rs.getInt("issue_id"),
                        rs.getString("member_name"),
                        rs.getString("title"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        fine);

                totalFine += fine;
                found = true;
            }

            if (!found) {
                System.out.println("No fines recorded yet.");
            }

            System.out.printf(
                    "Total Recorded Fines: Rs. %.2f%n", totalFine);

        } catch (SQLException e) {
            System.out.println("Unable to generate fine report: "
                    + e.getMessage());
        }
    }
}
