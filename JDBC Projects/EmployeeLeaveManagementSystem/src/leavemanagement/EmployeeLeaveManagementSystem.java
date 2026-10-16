package leavemanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class EmployeeLeaveManagementSystem {

    private static final Scanner sc = new Scanner(System.in);

    // ANSI colors for console output
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";
    private static final String WHITE = "\u001B[37m";

    private static void printColor(String color, String message) {
        System.out.println(color + message + RESET);
    }

    private static void printHeader(String title) {
        printColor(CYAN, "\n==============================================");
        printColor(PURPLE, "       " + title);
        printColor(CYAN, "==============================================");
    }


    public static void main(String[] args) {

        while (true) {
            showMenu();

            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    addEmployee();
                    break;
                case 2:
                    viewAllEmployees();
                    break;
                case 3:
                    searchEmployee();
                    break;
                case 4:
                    applyLeave();
                    break;
                case 5:
                    viewAllLeaveRequests();
                    break;
                case 6:
                    processLeave("APPROVED");
                    break;
                case 7:
                    processLeave("REJECTED");
                    break;
                case 8:
                    viewEmployeeLeaveHistory();
                    break;
                case 9:
                    printColor(GREEN, "Thank you for using the system!");
                    sc.close();
                    return;
                default:
                    printColor(RED, "Invalid choice. Enter 1 to 9.");
            }
        }
    }

    private static void showMenu() {
        printHeader("EMPLOYEE LEAVE MANAGEMENT SYSTEM");
        printColor(GREEN,  "1. Add Employee");
        printColor(CYAN,   "2. View All Employees");
        printColor(BLUE,   "3. Search Employee");
        printColor(PURPLE, "4. Apply for Leave");
        printColor(YELLOW, "5. View All Leave Requests");
        printColor(GREEN,  "6. Approve Leave");
        printColor(RED,    "7. Reject Leave");
        printColor(CYAN,   "8. View Employee Leave History");
        printColor(RED,    "9. Exit");
    }

    private static int readInt(String message) {
        while (true) {
            System.out.print(YELLOW + message + RESET);
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printColor(RED, "Please enter a valid number.");
            }
        }
    }

    private static String readRequired(String message) {
        while (true) {
            System.out.print(YELLOW + message + RESET);
            String value = sc.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            printColor(RED, "This field cannot be empty.");
        }
    }

    private static LocalDate readDate(String message) {
        while (true) {
            String value = readRequired(message);

            try {
                return LocalDate.parse(value);
            } catch (Exception e) {
                printColor(RED, "Invalid date. Use YYYY-MM-DD.");
            }
        }
    }

    private static boolean employeeExists(int employeeId)
            throws SQLException {

        String sql =
                "SELECT employee_id FROM employees WHERE employee_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // OPTION 1: ADD EMPLOYEE

    private static void addEmployee() {

        printHeader("ADD EMPLOYEE");

        String name = readRequired("Employee name: ");
        String email = readRequired("Email: ");
        String department = readRequired("Department: ");
        String designation = readRequired("Designation: ");
        LocalDate joiningDate = readDate("Joining date (YYYY-MM-DD): ");

        String sql = "INSERT INTO employees "
                + "(employee_name, email, department, designation, joining_date) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, department);
            ps.setString(4, designation);
            ps.setDate(5, java.sql.Date.valueOf(joiningDate));

            int rows = ps.executeUpdate();

            if (rows > 0) {
                printColor(GREEN, "Employee added successfully.");
            }

        } catch (SQLException e) {
            printColor(RED, "Could not add employee: " + e.getMessage());
        }
    }

    // OPTION 2: VIEW ALL EMPLOYEES

    private static void viewAllEmployees() {

        printHeader("ALL EMPLOYEES");

        String sql = "SELECT * FROM employees ORDER BY employee_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;
                printEmployee(rs);
            }

            if (!found) {
                printColor(YELLOW, "No employees found.");
            }

        } catch (SQLException e) {
            printColor(RED, "Error: " + e.getMessage());
        }
    }

    // OPTION 3: SEARCH EMPLOYEE

    private static void searchEmployee() {

        printHeader("SEARCH EMPLOYEE");

        int id = readInt("Enter employee ID: ");

        String sql = "SELECT * FROM employees WHERE employee_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    printEmployee(rs);
                } else {
                    printColor(YELLOW, "Employee not found.");
                }
            }

        } catch (SQLException e) {
            printColor(RED, "Error: " + e.getMessage());
        }
    }

    private static void printEmployee(ResultSet rs)
            throws SQLException {

        printColor(BLUE, "------------------------------------");
        System.out.println("Employee ID : " + rs.getInt("employee_id"));
        System.out.println("Name        : " + rs.getString("employee_name"));
        System.out.println("Email       : " + rs.getString("email"));
        System.out.println("Department  : " + rs.getString("department"));
        System.out.println("Designation : " + rs.getString("designation"));
        System.out.println("Joining Date: " + rs.getDate("joining_date"));
        printColor(BLUE, "------------------------------------");
    }

    // OPTION 4: APPLY FOR LEAVE

    private static void applyLeave() {

        printHeader("APPLY FOR LEAVE");

        int employeeId = readInt("Enter employee ID: ");

        try {
            if (!employeeExists(employeeId)) {
                printColor(RED, "Employee ID does not exist.");
                return;
            }

            System.out.println("Leave types: CASUAL, SICK, EARNED");

            String leaveType = readRequired("Enter leave type: ")
                    .toUpperCase();

            if (!leaveType.equals("CASUAL")
                    && !leaveType.equals("SICK")
                    && !leaveType.equals("EARNED")) {

                printColor(RED, "Invalid leave type.");
                return;
            }

            LocalDate startDate =
                    readDate("Start date (YYYY-MM-DD): ");

            LocalDate endDate =
                    readDate("End date (YYYY-MM-DD): ");

            if (startDate.isBefore(LocalDate.now())) {
                printColor(RED, "Start date cannot be in the past.");
                return;
            }

            if (endDate.isBefore(startDate)) {
                printColor(RED, "End date cannot be before start date.");
                return;
            }

            int totalDays = (int) ChronoUnit.DAYS.between(
                    startDate, endDate) + 1;

            String reason = readRequired("Reason for leave: ");

            String sql = "INSERT INTO leave_requests "
                    + "(employee_id, leave_type, start_date, end_date, "
                    + "total_days, reason) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, employeeId);
                ps.setString(2, leaveType);
                ps.setDate(3, java.sql.Date.valueOf(startDate));
                ps.setDate(4, java.sql.Date.valueOf(endDate));
                ps.setInt(5, totalDays);
                ps.setString(6, reason);

                ps.executeUpdate();

                printColor(GREEN, "Leave request submitted successfully.");
                System.out.println("Total leave days: " + totalDays);
                printColor(YELLOW, "Status: PENDING");
            }

        } catch (SQLException e) {
            printColor(RED, "Error applying for leave: " + e.getMessage());
        }
    }

    // OPTION 5: VIEW ALL LEAVE REQUESTS

    private static void viewAllLeaveRequests() {

        printHeader("ALL LEAVE REQUESTS");

        String sql = "SELECT l.*, e.employee_name "
                + "FROM leave_requests l "
                + "JOIN employees e ON l.employee_id = e.employee_id "
                + "ORDER BY l.leave_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;
                printLeaveRequest(rs);
            }

            if (!found) {
                printColor(YELLOW, "No leave requests found.");
            }

        } catch (SQLException e) {
            printColor(RED, "Error: " + e.getMessage());
        }
    }

    // OPTION 6 AND OPTION 7: APPROVE OR REJECT LEAVE

    private static void processLeave(String newStatus) {

        printHeader(newStatus + " LEAVE");

        int leaveId = readInt("Enter leave request ID: ");

        String sql = "UPDATE leave_requests SET status = ? "
                + "WHERE leave_id = ? AND status = 'PENDING'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, leaveId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                printColor(GREEN, "Leave request " + newStatus.toLowerCase() + " successfully.");
            } else {
                printColor(YELLOW, "Request not found or it has already been processed.");
            }

        } catch (SQLException e) {
            printColor(RED, "Error: " + e.getMessage());
        }
    }

    private static void printLeaveRequest(ResultSet rs)
            throws SQLException {

        printColor(BLUE, "------------------------------------");
        System.out.println("Leave ID    : " + rs.getInt("leave_id"));
        System.out.println("Employee ID : " + rs.getInt("employee_id"));
        System.out.println("Employee    : " + rs.getString("employee_name"));
        System.out.println("Leave Type  : " + rs.getString("leave_type"));
        System.out.println("Start Date  : " + rs.getDate("start_date"));
        System.out.println("End Date    : " + rs.getDate("end_date"));
        System.out.println("Total Days  : " + rs.getInt("total_days"));
        System.out.println("Reason      : " + rs.getString("reason"));
        System.out.println("Status      : " + rs.getString("status"));
        System.out.println("Applied On  : " + rs.getTimestamp("applied_on"));
        printColor(BLUE, "------------------------------------");
    }

    // OPTION 8: VIEW EMPLOYEE LEAVE HISTORY

    private static void viewEmployeeLeaveHistory() {

        printHeader("EMPLOYEE LEAVE HISTORY");

        int employeeId = readInt("Enter employee ID: ");

        String sql = "SELECT l.*, e.employee_name "
                + "FROM leave_requests l "
                + "JOIN employees e ON l.employee_id = e.employee_id "
                + "WHERE l.employee_id = ? "
                + "ORDER BY l.leave_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;
                    printLeaveRequest(rs);
                }

                if (!found) {
                    printColor(YELLOW, "No leave history found for this employee.");
                }
            }

        } catch (SQLException e) {
            printColor(RED, "Error: " + e.getMessage());
        }
    }
}


