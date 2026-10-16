
package library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookDAO {

    // 1. Add Book
    public void addBook(String bookId, String title, String author,
                        String genre, int totalCopies) {

        String sql = "INSERT INTO books "
                + "(book_id, title, author, genre, total_copies, available_copies) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookId);
            ps.setString(2, title);
            ps.setString(3, author);
            ps.setString(4, genre);
            ps.setInt(5, totalCopies);
            ps.setInt(6, totalCopies);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Book added successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Unable to add book: " + e.getMessage());
        }
    }

    // 2. View All Books
    public void viewAllBooks() {

        String sql = "SELECT * FROM books ORDER BY book_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            printHeader();

            boolean found = false;

            while (rs.next()) {
                printBook(rs);
                found = true;
            }

            if (!found) {
                System.out.println("No books found.");
            }

        } catch (SQLException e) {
            System.out.println("Unable to view books: " + e.getMessage());
        }
    }

    // 3. Search Book by ID, title, or author
    public void searchBook(String keyword) {

        String sql = "SELECT * FROM books "
                + "WHERE book_id LIKE ? "
                + "OR title LIKE ? "
                + "OR author LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            try (ResultSet rs = ps.executeQuery()) {

                printHeader();

                boolean found = false;

                while (rs.next()) {
                    printBook(rs);
                    found = true;
                }

                if (!found) {
                    System.out.println("No matching books found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Unable to search books: " + e.getMessage());
        }
    }

    // 4. Update Book Details
    public void updateBook(String bookId, String title, String author,
                           String genre, int newTotalCopies) {

        if (newTotalCopies < 0) {
            System.out.println("Total copies cannot be negative.");
            return;
        }

        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, "
                + "available_copies = available_copies + (? - total_copies), "
                + "total_copies = ? "
                + "WHERE book_id = ? "
                + "AND available_copies + (? - total_copies) >= 0";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, genre);
            ps.setInt(4, newTotalCopies);
            ps.setInt(5, newTotalCopies);
            ps.setString(6, bookId);
            ps.setInt(7, newTotalCopies);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Book details updated successfully!");
            } else {
                System.out.println(
                    "Book not found, or total copies cannot be less "
                    + "than the number of currently issued copies."
                );
            }

        } catch (SQLException e) {
            System.out.println("Unable to update book: " + e.getMessage());
        }
    }

    // 5. Delete Book
    public void deleteBook(String bookId) {

        String sql = "DELETE FROM books "
                + "WHERE book_id = ? "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM book_issues "
                + "WHERE book_issues.book_id = books.book_id "
                + "AND return_date IS NULL)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bookId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Book deleted successfully!");
            } else {
                System.out.println(
                    "Book not found or it has an active issue."
                );
            }

        } catch (SQLException e) {
            System.out.println(
                "Unable to delete book. It may have issue history: "
                + e.getMessage()
            );
        }
    }

    // Display table headings
    private void printHeader() {
        System.out.println();
        System.out.printf(
            "%-12s %-25s %-20s %-15s %-12s %-15s%n",
            "Book ID", "Title", "Author", "Genre",
            "Total Copies", "Available"
        );

        System.out.println(
            "-------------------------------------------------------------------------------------------------------------"
        );
    }

    // Display one book
    private void printBook(ResultSet rs) throws SQLException {
        System.out.printf(
            "%-12s %-25s %-20s %-15s %-12d %-15d%n",
            rs.getString("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("genre"),
            rs.getInt("total_copies"),
            rs.getInt("available_copies")
        );
    }
}
