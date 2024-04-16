Lab 2 - databases
-- Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 0;

-- Delete tables if they exist
DROP TABLE IF EXISTS Reservation;
DROP TABLE IF EXISTS Shows;
DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS Theatre;
DROP TABLE IF EXISTS Movie;

-- Create User table
CREATE TABLE User (
    Username CHAR(50) NOT NULL,
    Name VARCHAR(50) NOT NULL,
    Address VARCHAR(50) NOT NULL,
    TelephoneNbr VARCHAR(10) NOT NULL,
    PRIMARY KEY(Username)
);

-- Create Reservation table
CREATE TABLE Reservation (
    ReservationID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Username CHAR(50) NOT NULL,
    ShowID INTEGER NOT NULL,
	-- ShowDate DATE NOT NULL,
    FOREIGN KEY (Username) REFERENCES User(Username),
    FOREIGN KEY (ShowID) REFERENCES Shows(ShowID)
    
);

-- Create Theatre table
CREATE TABLE Theatre (
    TheatreName VARCHAR(50) NOT NULL,
    NumberOfSeats Integer NOT NULL,
    PRIMARY KEY(TheatreName)
);

-- Create Movie table
CREATE TABLE Movie (
    MovieName VARCHAR(50) NOT NULL,
    PRIMARY KEY (MovieName)
);

CREATE TABLE Shows (
    ShowID INTEGER AUTO_INCREMENT PRIMARY KEY,
    MovieName VARCHAR(50) NOT NULL,
    NumSeatsAvailable INTEGER NOT NULL,
    ShowDate DATE NOT NULL,
    TheatreName VARCHAR(50) NOT NULL,
    ReservationID INTEGER,
    UNIQUE(MovieName, ShowDate),
    FOREIGN KEY (MovieName) REFERENCES Movie(MovieName) ON UPDATE CASCADE,
    FOREIGN KEY (TheatreName) REFERENCES Theatre(TheatreName),
    FOREIGN KEY (ReservationID) REFERENCES Reservation(ReservationID)
);


-- Start transaction
START TRANSACTION;

-- Insert data into User table
INSERT INTO User VALUES
('TH','Tonny','Furutorpsgatan 49B', '0762622195'),
('AZ','Ashraf Alzain','Södergatan 97', '0723764811');

-- Insert data into Reservation table
INSERT INTO Reservation(Username, ShowID) VALUES
('AZ', 1),
('TH', 1);


UPDATE Shows
SET NumSeatsAvailable = NumSeatsAvailable - 1
WHERE ShowID = 1 OR ShowID = 2;


-- Insert data into Theatre table
INSERT INTO Theatre VALUES
('Filmstaden', 40),
('NordiskFilm', 40),
('Bio Metropol', 40);

-- Insert data into Movie table
INSERT INTO Movie VALUES
('StarWars'),
('Aquaman'),
('Batman'),
('Superman');

-- Insert data into Show table
INSERT INTO Shows(MovieName, NumSeatsAvailable, ShowDate, TheatreName, ShowID) VALUES
('StarWars', 5, '2009-01-23', 'Filmstaden', 1),
('StarWars', 12, '2021-01-25', 'Filmstaden', 2),
('Aquaman', 0, '2027-07-24', 'NordiskFilm', 4),
('Superman', 3, '2022-09-02', 'Bio Metropol', 3),
('Batman', 1, '2024-01-25', 'Filmstaden', 5);

-- Commit transaction
COMMIT;

-- Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;








SELECT *
FROM Reservation
JOIN User ON Reservation.Username = User.Username


SELECT Movie.MovieName, Reservation.ReservationID, Username
FROM Movie
JOIN Shows ON Movie.MovieName = Shows.MovieName
JOIN Reservation ON Shows.ShowID = Reservation.ShowID
WHERE Movie.MovieName = 'StarWars';


SELECT Movie.MovieName, Shows.ShowDate, Theatre.TheatreName
FROM Movie
JOIN Shows ON Movie.MovieName = Shows.MovieName
JOIN Theatre ON Shows.TheatreName = Theatre.TheatreName;

D8
SELECT Movie.MovieName, Shows.ShowDate, Theatre.TheatreName
FROM Movie
JOIN Shows ON Movie.MovieName = Shows.MovieName
JOIN Theatre ON Shows.TheatreName = Theatre.TheatreName
where Theatre.theatreName = 'Lol';

---

SELECT Movie.MovieName, Shows.ShowDate, Theatre.TheatreName
FROM Movie
JOIN Shows ON Movie.MovieName = Shows.MovieName
JOIN Theatre ON Shows.TheatreName = Theatre.TheatreName
where Theatre.theatreName like 'f%';


-- where Theatre.theatreName = 'filmstaden';
-- where Theatre.theatreName = '?';



D9. The problems that can arise are conditions such as when multiple users check for seat availability, it might display
    a nbr of seats available. However, when they all book simutaneously, the server might not be able to handle that and the result
    of that is that they all booked seats even though there aren't that many seats available. Another problem that might arise is the
    server might suffer from not updating correct due to the high amount of active people that are booking, for instance the database
    could or might not be able to update all the bookings simutaneously, resulting in false numbers of available seats.

D10
   Some sort of transaction management which is the ability to execute a series of database operations as a single unit to ensure consistency
   and isolation when for instance the server is in the presence of multiple transactions.

   Different level of isolations that ensure that the system can candle the amount of acitivity going on, on the booking site.

   Some sort of durability, for instance the ACID properties that ensure the server in the presence of errors and crashes that the site
   should be able to handle exceptions.

