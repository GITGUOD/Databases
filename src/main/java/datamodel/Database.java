package datamodel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Database is a class that specifies the interface to the 
 * movie database. Uses JDBC and the MySQL Connector/J driver.
 */
public class Database {
    /** 
     * The database connection.
     */
    private Connection conn;
        
    /**
     * Create the database interface object. Connection to the database
     * is performed later.
     */
    public Database() {
        conn = null;
    }
       
    /* --- TODO: Change this method to fit your choice of DBMS --- */
    /** 
     * Open a connection to the database, using the specified user name
     * and password.
     *
     * @param userName The user name.
     * @param password The user's password.
     * @return true if the connection succeeded, false if the supplied
     * user name and password were not recognized. Returns false also
     * if the JDBC driver isn't found.
     */
    public boolean openConnection(String userName, String password) {
        try {
        	// Connection strings for included DBMS clients:
        	// [MySQL]       jdbc:mysql://[host]/[database]
        	// [PostgreSQL]  jdbc:postgresql://[host]/[database]
        	// [SQLite]      jdbc:sqlite://[filepath]
        	
        	// Use "jdbc:mysql://puccini.cs.lth.se/" + userName if you using our shared server
        	// If outside, this statement will hang until timeout.
            conn = DriverManager.getConnection 
                ( "jdbc:mysql://puccini.cs.lth.se/" + userName, userName, password);


                
        }
        catch (SQLException e) {
            System.err.println(e);
            e.printStackTrace();
            return false;
        }
        return true;
    }
        
    /**
     * Close the connection to the database.
     */
    public void closeConnection() {
        try {
            if (conn != null)
                conn.close();
        }
        catch (SQLException e) {
        	e.printStackTrace();
        }
        conn = null;
        
        System.err.println("Database connection closed.");
    }
        
    /**
     * Check if the connection to the database has been established
     *
     * @return true if the connection has been established
     */
    public boolean isConnected() {
        return conn != null;
    }
	
  	public Show getShowData(String mTitle, String mDate) {
		//Integer mFreeSeats = 42;
		//String mVenue = "Kino 2";
		
		/* --- TODO: add code for database query --- */
        //My code
        try {
            String query = "SELECT ShowID, NumSeatsAvailable, TheatreName FROM Shows WHERE MovieName = ? AND ShowDate = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, mTitle);
            statement.setString(2, mDate);
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                int ShowID = resultSet.getInt("ShowID");
                int mFreeSeatss = resultSet.getInt("NumSeatsAvailable");
                String mVenues = resultSet.getString("TheatreName");
                return new Show(mTitle, mDate, mVenues, mFreeSeatss, ShowID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return null; // Return null if no data found or an error occurred
    }

    /* --- TODO: insert more own code here --- */

    //Metod som returnerar sant eller falskt beroende på om inloggningen lyckas, min egna kod
    public boolean login(String userName) {

        //Kontrollera om anslutningen är etablerad
        if(!isConnected()) {
            System.out.println("The connection between you and the database has not been established");
            return false;
        } 
            try {
                //förbereder en SQL-fråga för att söka efter användaren i databasen. Anledningen till varför
                // Vi har frågetecken och sedan använder stmt.setString är för att binda de faktiska värderna 
                // för att göra det säkrare att infoga användare input
                PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM User WHERE Username = ?");
                
                //
                statement.setString(1, userName);
    

                //Utför SQL-frågan och få resultat
                ResultSet resultSet = statement.executeQuery();
                
                //Om det finns en matchning för användaren, lyckades inloggningen
                if(resultSet.next()) {
                    int rowCount = resultSet.getInt(1); //Hämta antalet rader
                    return rowCount > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();

            }
        
            //Om något går fel, misslyckades inloggningen
        return false;
    }

    public List<String> getAllMovies() {
        List<String> movies = new ArrayList<>();

        try {
            //SQL statement för att hämta alla filmer från tabellen shows
        String sql = "SELECT DISTINCT MovieName FROM Shows";

        //förberedd en SQL-statement med stringen som inparameter
        PreparedStatement statement = conn.prepareStatement(sql);

        //Utför SQL frågan och få resultatet
        ResultSet resultSet = statement.executeQuery();

        //Loopa igenom resultatet och lägg till varje film i listan
        while(resultSet.next()) {
            String movieTitle = resultSet.getString("MovieName");
            movies.add(movieTitle);
        }

        //Stänger av resurser
        resultSet.close();
        statement.close();
    } catch (SQLException e) {
        e.printStackTrace();
        //Hantera eventuella SQL-fel
    }

        return movies;
    }

    public List<String> getMovieDates(String movieName) {
        List<String> allDates = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT ShowDate FROM Shows WHERE MovieName = ?";

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, movieName);

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                String ShowDate = resultSet.getString("ShowDate");
                allDates.add(ShowDate);
            }

            resultSet.close();
        statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allDates;
    }

    public int bookTicket(String username, int showID) {
        //Declaring bookingnumber and initializing it
        int bookingNumber = -1;
    
        //Defining sql code that will later be executed
        String insertReservationQuery = "INSERT INTO Reservation (Username, ShowID) VALUES (?, ?)";
        String updateSeatsQuery = "UPDATE Shows SET NumSeatsAvailable = NumSeatsAvailable - 1 WHERE ShowID = ?";
        String checkAvailabilityQuery = "SELECT NumSeatsAvailable FROM Shows WHERE ShowID = ?";
    
        try {
            // Set transaction isolation level, this means that you secure that no other transaction can
            // change your database or transaction until the current transaction is finished
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    
            // Starting the transaction, however, before you do that you have to set autocommit to false, or
            //in other words, you stop commiting transactions automatically and instead manually and so if something goes wrong, you can reroll it.
            conn.setAutoCommit(false);
    
            // Check availability of the seats with the sql query above.
            try (PreparedStatement checkAvailabilityStatement = conn.prepareStatement(checkAvailabilityQuery)) {
                //Sets the value of the first placeholder in the string checkAvailabilityQuery (there's only one in that string (?))
                checkAvailabilityStatement.setInt(1, showID);
                //Execute it
                ResultSet availabilityResult = checkAvailabilityStatement.executeQuery();
    
                //if we get any results
                if (availabilityResult.next()) {
                    //We check if there are ny seats available and get it and save it in the integer numSeatsAvailable.
                    int numSeatsAvailable = availabilityResult.getInt("NumSeatsAvailable");
    
                    //After we have saved the results of the number of seats available, we check if the number of seats is positive or if there are any free.
                    if (numSeatsAvailable > 0) {
                        // Inserting the reservation, we start by preparing a statement to the database and while we do that, we also want to receive the keys
                        // that are associated with the statement, this is especially useful when it comes to auto incrementing something, in this case an ID or reservation nbr
                        try (PreparedStatement insertReservationStatement = conn.prepareStatement(insertReservationQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                             PreparedStatement updateSeatsStatement = conn.prepareStatement(updateSeatsQuery)) {
    
                            // Set parameters/values for insertReservationStatement
                            insertReservationStatement.setString(1, username);
                            insertReservationStatement.setInt(2, showID);
    
                            // Execute insertReservationStatement into the database which means that we save it in the databse
                            int rowsInserted = insertReservationStatement.executeUpdate();
    
                            //If we insert something or in this case a movieTicket
                            if (rowsInserted > 0) {
                                // We retrieve generated keys or a (booking number) which it is called
                                ResultSet generatedKeys = insertReservationStatement.getGeneratedKeys();
                                //Bfore we retrieve, we have to check If there are any generated keys in the first case
                                if (generatedKeys.next()) {
                                    bookingNumber = generatedKeys.getInt(1);
                                }
    
                                // Update available seats by subtracting one seats to the seats available. Setting the value and then execute it of course.
                                updateSeatsStatement.setInt(1, showID);
                                updateSeatsStatement.executeUpdate();
                            }
                        }
                    }
                }
            }
    
            // Commit transaction after we finish with the manual changes
            conn.commit();
            //Then reverting the change to the autocommit settning so everything works as it used to.
            conn.setAutoCommit(true);
            //if we catch any errors we try to....
        } catch (SQLException e) {
            try {
                // Do a rollback transaction which means we go to the past to the previous state where we undo any changes
                conn.rollback();
                //Then we return the auto commit to true after having it set to false
                conn.setAutoCommit(true);
                //Then we print the errors
                e.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    
        return bookingNumber;
    }
    
    
    public List<Reservation> getUserReservations(String username) {
        List<Reservation> userReservations = new ArrayList<>();
        try {
            String query = "SELECT Reservation.ReservationID, Shows.MovieName, Shows.ShowDate, Shows.TheatreName " +
            "FROM Reservation " +
            "INNER JOIN Shows ON Reservation.ShowID = Shows.ShowID " +
            "WHERE Username = ?";
            
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
    
            while(resultSet.next()) {
                int reservationID = resultSet.getInt("ReservationID");
                String movieName = resultSet.getString("MovieName");
                String showDate = resultSet.getString("ShowDate");
                String theatreName = resultSet.getString("TheatreName");
    
                Reservation reservation = new Reservation(reservationID, movieName, showDate, theatreName);
                userReservations.add(reservation);
            }
    
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userReservations;
    }
}    
    
/*    public List<Reservation> getAllReservations() {
        //A method for getting all the reservations
        List<Reservation> reservations = new ArrayList<>();
        try {
            //Get all of  these stuffs from the database
            String sql = "SELECT Reservation.ReservationID, Shows.MovieName, Shows.ShowDate, Shows.TheatreName FROM Reservation INNER JOIN Shows ON Reservation.ShowID = Shows.ShowID";

            PreparedStatement statement = conn.prepareStatement(sql);
            
            ResultSet resultSet = statement.executeQuery();

            //As long as we have a result or iterating the list
            while(resultSet.next()) {
                int ReservationID = resultSet.getInt("ReservationID");
                String MovieName = resultSet.getString("MovieName");
                String showDate = resultSet.getString("ShowDate");
                String theatreName = resultSet.getString("TheatreName");
                
                //Creating a new 
                Reservation reservation = new Reservation(ReservationID, MovieName, showDate, theatreName);
                reservations.add(reservation);
            }

            resultSet.close();
        statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
} */



//Gammal kod
/*public int bookTicket(String username, int ShowID) {
        int bookingNumber = -1;
    
        try {
            //Preparing Query to later execute it
            String checkAvailability = "SELECT NumSeatsAvailable FROM Shows WHERE ShowID = ?";
            PreparedStatement statement = conn.prepareStatement(checkAvailability);
            statement.setInt(1, ShowID);
            ResultSet resultSet = statement.executeQuery();
    
            //After we have executed, check if there is data or lines
            if (resultSet.next()) {
                int numSeatsAvailable = resultSet.getInt("NumSeatsAvailable");
    
                //if there are seats available then you are ready to book and then u book as well as u update the table
                if (numSeatsAvailable > 0) {
                    String updateSeatsQuery = "UPDATE Shows SET NumSeatsAvailable = ? WHERE ShowID = ?";
                    PreparedStatement updateSeatsStatement = conn.prepareStatement(updateSeatsQuery);
                    updateSeatsStatement.setInt(1, numSeatsAvailable - 1);
                    updateSeatsStatement.setInt(2, ShowID);
                    int rowsUpdated = updateSeatsStatement.executeUpdate();
    
                    //Update by inserting it into the database and in return the one who booked gets booking number if the if statement is true where if the Query gets execute
                    //And
                    if (rowsUpdated > 0) {
                        String insertReservationQuery = "INSERT INTO Reservation (Username, ShowID) VALUES (?, ?)";
                        PreparedStatement insertReservationStatement = conn.prepareStatement(insertReservationQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                        insertReservationStatement.setString(1, username);
                        insertReservationStatement.setInt(2, ShowID);
    
                        int rowsInserted = insertReservationStatement.executeUpdate();
    
                        if (rowsInserted > 0) {
                            ResultSet generatedKeys = insertReservationStatement.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                bookingNumber = generatedKeys.getInt(1);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return bookingNumber;
    } */