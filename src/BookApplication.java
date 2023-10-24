import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

public class BookApplication {
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            BookDatabaseManager manager = new BookDatabaseManager();

            while (true) {
                System.out.println("+---------------------------------------------------------------+");
                System.out.println("| Please select an option: \t\t\t\t\t\t\t\t\t\t|");
                System.out.println("| 1. Print all the books from the database (showing the authors)|");
                System.out.println("| 2. Print all the authors from the database (showing the books)|");
                System.out.println("| 3. Add a book to the database for an existing author\t\t\t|");
                System.out.println("| 4. Add a new author\t\t\t\t\t\t\t\t\t\t\t|");
                System.out.println("| 5. Quit\t\t\t\t\t\t\t\t\t\t\t\t\t\t|");
                System.out.println("+---------------------------------------------------------------+");

                String option = reader.readLine();

                switch (option) {
                    case "1":
                        System.out.println("+---------------------------------------------------------------+");
                        System.out.println("All the books in the database(with authors) are:");
                        System.out.println("+---------------------------------------------------------------+");
                        for (Book book : manager.loadBooks()) {
                            System.out.println(book.getTitle() + " by " + book.getAuthorList());
                        }
                        break;
                    case "2":
                        System.out.println("+---------------------------------------------------------------+");
                        System.out.println("All authors in the database(with books) are:");
                        System.out.println("+---------------------------------------------------------------+");
                        for (Author author : manager.loadAuthors()) {
                            System.out.println(author.getFirstName() + " " + author.getLastName() + " wrote " + author.getBookList());
                        }
                        break;
                    case "3":
                        System.out.println("+---------------------------------------------------------------+");
                        System.out.println("Add a book to the database for an existing author:");
                        System.out.println("+---------------------------------------------------------------+");
                        List<Author> authors = manager.loadAuthors();
                        System.out.println("List of existing authors:");
                        for (Author author : authors) {
                            System.out.println(author.getId() + ". " + author.getFirstName() + " " + author.getLastName());
                        }
                        System.out.println("Enter the author ID from the list above:");
                        int authorID = Integer.parseInt(reader.readLine());

                        System.out.println("Enter ISBN:");
                        String isbn = reader.readLine();
                        System.out.println("Enter book title:");
                        String title = reader.readLine();
                        System.out.println("Enter edition number:");
                        int edition = Integer.parseInt(reader.readLine());
                        System.out.println("Enter copyright:");
                        String copyright = reader.readLine();

                        Book newBook = new Book(isbn, title, edition, copyright);
                        Author existingAuthor = new Author(authorID, "", "");
                        newBook.getAuthorList().add(existingAuthor);
                        manager.addNewBook(newBook);
                        manager.associateAuthorWithBook(authorID, isbn);
                        break;
                    case "4":
                        System.out.println("+---------------------------------------------------------------+");
                        System.out.println("Add a new author:");
                        System.out.println("+---------------------------------------------------------------+");
                        System.out.println("Enter author ID:");
                        int id = Integer.parseInt(reader.readLine());
                        System.out.println("Enter first name:");
                        String firstName = reader.readLine();
                        System.out.println("Enter last name:");
                        String lastName = reader.readLine();
                        Author newAuthor = new Author(id, firstName, lastName);
                        manager.addNewAuthor(newAuthor);
                        break;
                    case "5":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please choose again.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
