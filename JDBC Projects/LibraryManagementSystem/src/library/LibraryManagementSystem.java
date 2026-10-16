package library;

import java.util.Scanner;

public class LibraryManagementSystem {

    // ANSI colors for console output
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    private static void printColor(String color, String message) {
        System.out.println(color + message + RESET);
    }

    private static void printHeader(String title) {
        printColor(CYAN, "\n==============================================");
        printColor(PURPLE, "          " + title);
        printColor(CYAN, "==============================================");
    }


    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        BookDAO bookDAO = new BookDAO();
        MemberDAO memberDAO = new MemberDAO();
        IssueDAO issueDAO = new IssueDAO();
        ReturnDAO returnDAO = new ReturnDAO();
        ReportDAO reportDAO = new ReportDAO();

        while (true) {

            printHeader("LIBRARY MANAGEMENT SYSTEM");
            printColor(GREEN,  " 1. Add Book");
            printColor(CYAN,   " 2. View All Books");
            printColor(BLUE,   " 3. Register Member");
            printColor(PURPLE, " 4. View All Members");
            printColor(YELLOW, " 5. Issue Book");
            printColor(GREEN,  " 6. Return Book");
            printColor(CYAN,   " 7. View Issued Books");
            printColor(BLUE,   " 8. Search Book");
            printColor(PURPLE, " 9. Update Book Details");
            printColor(RED,    "10. Delete Book");
            printColor(YELLOW, "11. Book Summary Report");
            printColor(GREEN,  "12. Member Issue Report");
            printColor(CYAN,   "13. Overdue Books Report");
            printColor(BLUE,   "14. Fine Report");
            printColor(RED,    "15. Exit");
            System.out.print(YELLOW + "Enter your choice: " + RESET);

            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                printColor(RED, "Please enter a valid number.");
                continue;
            }

            switch (choice) {

                case 1: {
                    System.out.print(YELLOW + "Enter Book ID: " + RESET);
                    String bookId = sc.nextLine();

                    System.out.print(YELLOW + "Enter Book Title: " + RESET);
                    String title = sc.nextLine();

                    System.out.print(YELLOW + "Enter Author Name: " + RESET);
                    String author = sc.nextLine();

                    System.out.print(YELLOW + "Enter Genre: " + RESET);
                    String genre = sc.nextLine();

                    int totalCopies = readInteger(
                            sc, (YELLOW + "Enter Total Copies: " + RESET));

                    if (totalCopies <= 0) {
                        printColor(RED, "Total copies must be greater than zero.");
                        break;
                    }

                    bookDAO.addBook(
                            bookId, title, author, genre, totalCopies);
                    break;
                }

                case 2:
                    bookDAO.viewAllBooks();
                    break;

                case 3: {
                    System.out.print(YELLOW + "Enter Member ID: " + RESET);
                    String memberId = sc.nextLine();

                    System.out.print(YELLOW + "Enter Member Name: " + RESET);
                    String memberName = sc.nextLine();

                    System.out.print(YELLOW + "Enter Email: " + RESET);
                    String email = sc.nextLine();

                    System.out.print(YELLOW + "Enter Phone Number: " + RESET);
                    String phone = sc.nextLine();

                    memberDAO.registerMember(
                            memberId, memberName, email, phone);
                    break;
                }

                case 4:
                    memberDAO.viewAllMembers();
                    break;

                case 5: {
                    System.out.print(YELLOW + "Enter Member ID: " + RESET);
                    String issueMemberId = sc.nextLine();

                    System.out.print(YELLOW + "Enter Book ID: " + RESET);
                    String issueBookId = sc.nextLine();

                    issueDAO.issueBook(issueMemberId, issueBookId);
                    break;
                }

                case 6: {
                    int issueId = readInteger(
                            sc, (YELLOW + "Enter Issue ID: " + RESET));

                    returnDAO.returnBook(issueId);
                    break;
                }

                case 7:
                    issueDAO.viewIssuedBooks();
                    break;

                case 8: {
                    System.out.print(YELLOW + "Enter Book ID, Title, or Author to search: " + RESET);

                    String keyword = sc.nextLine();
                    bookDAO.searchBook(keyword);
                    break;
                }

                case 9: {
                    System.out.print(YELLOW + "Enter Book ID to update: " + RESET);
                    String updateId = sc.nextLine();

                    System.out.print(YELLOW + "Enter New Title: " + RESET);
                    String newTitle = sc.nextLine();

                    System.out.print(YELLOW + "Enter New Author: " + RESET);
                    String newAuthor = sc.nextLine();

                    System.out.print(YELLOW + "Enter New Genre: " + RESET);
                    String newGenre = sc.nextLine();

                    int newTotalCopies = readInteger(
                            sc, (YELLOW + "Enter New Total Copies: " + RESET));

                    if (newTotalCopies < 0) {
                        printColor(RED, "Total copies cannot be negative.");
                        break;
                    }

                    bookDAO.updateBook(
                            updateId,
                            newTitle,
                            newAuthor,
                            newGenre,
                            newTotalCopies);
                    break;
                }

                case 10: {
                    System.out.print(YELLOW + "Enter Book ID to delete: " + RESET);
                    String deleteId = sc.nextLine();

                    System.out.print(YELLOW + "Are you sure you want to delete this book? (yes/no): " + RESET);

                    String confirmation = sc.nextLine();

                    if (confirmation.equalsIgnoreCase("yes")) {
                        bookDAO.deleteBook(deleteId);
                    } else {
                        printColor(YELLOW, "Delete operation cancelled.");
                    }
                    break;
                }

                case 11:
                    reportDAO.displayBookSummary();
                    break;

                case 12:
                    reportDAO.displayMemberIssueReport();
                    break;

                case 13:
                    reportDAO.displayOverdueBooks();
                    break;

                case 14:
                    reportDAO.displayFineReport();
                    break;

                case 15:
                    printColor(GREEN, "Thank you for using Library Management System!");
                    sc.close();
                    return;

                default:
                    printColor(RED, "Invalid choice. Please select 1 to 15.");
            }
        }
    }

    private static int readInteger(Scanner sc, String message) {

        while (true) {
            System.out.print(YELLOW + message + RESET);

            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                printColor(RED, "Please enter a valid whole number.");
            }
        }
    }
}

