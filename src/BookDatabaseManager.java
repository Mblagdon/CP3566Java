import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDatabaseManager {
    private Connection conn;
    private Statement stmt;
    private PreparedStatement pstmtBookInsert;
    private PreparedStatement pstmtAuthorInsert;
    private PreparedStatement pstmtBookSelectAll;
    private PreparedStatement pstmtAuthorSelectAll;
    private PreparedStatement pstmtAuthorISBNInsert;
    private PreparedStatement pstmtAuthorISBNSelect;
    private PreparedStatement pstmtUpdateBook;
    private PreparedStatement pstmtUpdateAuthor;
    private PreparedStatement pstmtDeleteBook;
    private PreparedStatement pstmtDeleteAuthor;
    private PreparedStatement pstmtDeleteAuthorISBNRelationship;
    private PreparedStatement pstmtSelectBooksByAuthorID;

    private List<Book> bookList;
    private List<Author> authorList;

    // SQL queries
    private static final String INSERT_BOOK_QUERY = "INSERT INTO titles(isbn, title, editionNumber, copyright) VALUES(?,?,?,?)";
    private static final String INSERT_AUTHOR_QUERY = "INSERT INTO authors(authorID, firstName, lastName) VALUES(?,?,?)";
    private static final String SELECT_ALL_BOOKS_QUERY = "SELECT * FROM titles";
    private static final String SELECT_ALL_AUTHORS_QUERY = "SELECT * FROM authors";
    private static final String INSERT_AUTHOR_ISBN_QUERY = "INSERT INTO authorisbn(authorID, isbn) VALUES(?,?)";
    private static final String SELECT_AUTHOR_ISBN_BY_ISBN = "SELECT * FROM authorisbn WHERE isbn = ?";
    private static final String UPDATE_BOOK_QUERY = "UPDATE titles SET title=?, editionNumber=?, copyright=? WHERE isbn=?";
    private static final String UPDATE_AUTHOR_QUERY = "UPDATE authors SET firstName=?, lastName=? WHERE authorID=?";
    private static final String DELETE_BOOK_QUERY = "DELETE FROM titles WHERE isbn=?";
    private static final String DELETE_AUTHOR_QUERY = "DELETE FROM authors WHERE authorID=?";
    private static final String DELETE_AUTHOR_ISBN_RELATIONSHIP = "DELETE FROM authorisbn WHERE authorID=? AND isbn=?";
    private static final String SELECT_BOOKS_BY_AUTHOR_ID = "SELECT t.* FROM titles t INNER JOIN authorisbn ai ON t.isbn = ai.isbn WHERE ai.authorID = ?";

    // Database connection constants
    static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    static final String DB_URL = "jdbc:mariadb://localhost:3300/books";
    static final String USER = "root";
    static final String PASS = "root";

    public BookDatabaseManager() throws SQLException, ClassNotFoundException {
        connectToDatabase();
        this.stmt = conn.createStatement();
        this.bookList = new ArrayList<>();
        this.authorList = new ArrayList<>();

        this.pstmtBookInsert = conn.prepareStatement(INSERT_BOOK_QUERY);
        this.pstmtAuthorInsert = conn.prepareStatement(INSERT_AUTHOR_QUERY);
        this.pstmtBookSelectAll = conn.prepareStatement(SELECT_ALL_BOOKS_QUERY);
        this.pstmtAuthorSelectAll = conn.prepareStatement(SELECT_ALL_AUTHORS_QUERY);
        this.pstmtAuthorISBNInsert = conn.prepareStatement(INSERT_AUTHOR_ISBN_QUERY);
        this.pstmtAuthorISBNSelect = conn.prepareStatement(SELECT_AUTHOR_ISBN_BY_ISBN);
        this.pstmtUpdateBook = conn.prepareStatement(UPDATE_BOOK_QUERY);
        this.pstmtUpdateAuthor = conn.prepareStatement(UPDATE_AUTHOR_QUERY);
        this.pstmtDeleteBook = conn.prepareStatement(DELETE_BOOK_QUERY);
        this.pstmtDeleteAuthor = conn.prepareStatement(DELETE_AUTHOR_QUERY);
        this.pstmtDeleteAuthorISBNRelationship = conn.prepareStatement(DELETE_AUTHOR_ISBN_RELATIONSHIP);
        this.pstmtSelectBooksByAuthorID = conn.prepareStatement(SELECT_BOOKS_BY_AUTHOR_ID);
    }

    private void connectToDatabase() throws SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER);
        this.conn = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // CREATE operations
    public int addNewBook(Book book) throws SQLException {
        pstmtBookInsert.setString(1, book.getISBN());
        pstmtBookInsert.setString(2, book.getTitle());
        pstmtBookInsert.setInt(3, book.getEdition());
        pstmtBookInsert.setString(4, book.getCopyright());
        return pstmtBookInsert.executeUpdate();
    }

    public int addNewAuthor(Author author) throws SQLException {
        pstmtAuthorInsert.setInt(1, author.getId());
        pstmtAuthorInsert.setString(2, author.getFirstName());
        pstmtAuthorInsert.setString(3, author.getLastName());
        return pstmtAuthorInsert.executeUpdate();
    }

    // READ operations
    public List<Book> loadBooks() throws SQLException {
        bookList.clear();
        ResultSet rs = pstmtBookSelectAll.executeQuery();
        while (rs.next()) {
            Book book = new Book(rs.getString("isbn"), rs.getString("title"), rs.getInt("editionNumber"), rs.getString("copyright"));
            book.setAuthorList(fetchAuthorsForBook(book.getISBN()));
            bookList.add(book);
        }
        return bookList;
    }

    public List<Author> loadAuthors() throws SQLException {
        authorList.clear();
        ResultSet rs = pstmtAuthorSelectAll.executeQuery();
        while (rs.next()) {
            Author author = new Author(rs.getInt("authorID"), rs.getString("firstName"), rs.getString("lastName"));
            author.setBookList(fetchBooksForAuthor(author.getId()));
            authorList.add(author);
        }
        return authorList;
    }

    // UPDATE operations
    public int updateBook(Book book) throws SQLException {
        pstmtUpdateBook.setString(1, book.getTitle());
        pstmtUpdateBook.setInt(2, book.getEdition());
        pstmtUpdateBook.setString(3, book.getCopyright());
        pstmtUpdateBook.setString(4, book.getISBN());
        return pstmtUpdateBook.executeUpdate();
    }

    public int updateAuthor(Author author) throws SQLException {
        pstmtUpdateAuthor.setString(1, author.getFirstName());
        pstmtUpdateAuthor.setString(2, author.getLastName());
        pstmtUpdateAuthor.setInt(3, author.getId());
        return pstmtUpdateAuthor.executeUpdate();
    }

    public int associateAuthorWithBook(int authorID, String isbn) throws SQLException {
        pstmtAuthorISBNInsert.setInt(1, authorID);
        pstmtAuthorISBNInsert.setString(2, isbn);
        return pstmtAuthorISBNInsert.executeUpdate();
    }

    // DELETE operations
    public int deleteBook(String isbn) throws SQLException {
        // Remove any relationships in the authorisbn table
        pstmtDeleteAuthorISBNRelationship.setString(1, isbn);
        pstmtDeleteAuthorISBNRelationship.executeUpdate();
        // Delete the book
        pstmtDeleteBook.setString(1, isbn);
        return pstmtDeleteBook.executeUpdate();
    }

    public int deleteAuthor(int authorID) throws SQLException {
        // Remove any relationships in the authorisbn table
        pstmtDeleteAuthorISBNRelationship.setInt(1, authorID);
        pstmtDeleteAuthorISBNRelationship.executeUpdate();
        // Delete the author
        pstmtDeleteAuthor.setInt(1, authorID);
        return pstmtDeleteAuthor.executeUpdate();
    }

    // Relationship management methods
    public List<Author> fetchAuthorsForBook(String isbn) throws SQLException {
        List<Author> authorsForBook = new ArrayList<>();
        pstmtAuthorISBNSelect.setString(1, isbn);
        ResultSet rs = pstmtAuthorISBNSelect.executeQuery();
        while (rs.next()) {
            int authorID = rs.getInt("authorID");
            for (Author author : loadAuthors()) {
                if (author.getId() == authorID) {
                    authorsForBook.add(author);
                    break;
                }
            }
        }
        return authorsForBook;
    }

    public List<Book> fetchBooksForAuthor(int authorID) throws SQLException {
        List<Book> booksForAuthor = new ArrayList<>();
        pstmtSelectBooksByAuthorID.setInt(1, authorID);
        ResultSet rs = pstmtSelectBooksByAuthorID.executeQuery();
        while (rs.next()) {
            Book book = new Book(rs.getString("isbn"), rs.getString("title"), rs.getInt("editionNumber"), rs.getString("copyright"));
            booksForAuthor.add(book);
        }
        return booksForAuthor;
    }
}
