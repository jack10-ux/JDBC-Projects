
package library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberDAO {

    // Register a new member
    public void registerMember(String memberId,
                               String memberName,
                               String email,
                               String phone) {

        String sql = "INSERT INTO members "
                + "(member_id, member_name, email, phone) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, memberId);
            ps.setString(2, memberName);
            ps.setString(3, email);
            ps.setString(4, phone);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println(
                        "Member registered successfully!");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Unable to register member: "
                    + e.getMessage());
        }
    }

    // View all registered members
    public void viewAllMembers() {

        String sql = "SELECT * FROM members ORDER BY member_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== ALL MEMBERS ==========");

            System.out.printf("%-12s %-25s %-30s %-15s%n",
                    "Member ID", "Member Name", "Email", "Phone");

            System.out.println(
                    "--------------------------------------------------------------------------------");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.printf("%-12s %-25s %-30s %-15s%n",
                        rs.getString("member_id"),
                        rs.getString("member_name"),
                        rs.getString("email"),
                        rs.getString("phone"));
            }

            if (!found) {
                System.out.println("No members registered yet.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Unable to retrieve members: "
                    + e.getMessage());
        }
    }
}
