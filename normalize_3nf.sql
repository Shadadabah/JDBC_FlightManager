-- =====================================================================
-- normalize_3nf.sql
-- Part 2.3 : Decompose rawflights(scheduled, flight, fromC, delay, terminal)
-- into Third Normal Form (3NF). Run in the FlightDB Query Console (Ctrl+Enter)
-- AFTER importing arrived_flights.csv into table `rawflights`.
-- Functional dependency removed:  flight -> fromC
-- =====================================================================

-- 1. Normalized tables ------------------------------------------------
CREATE TABLE FlightRoutes (
    flight_number VARCHAR(50)  PRIMARY KEY,
    from_city     VARCHAR(100) NOT NULL
);

CREATE TABLE Arrivals (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(50) NOT NULL,
    scheduled     VARCHAR(50),
    delay_status  VARCHAR(50),
    terminal      VARCHAR(10),
    CONSTRAINT fk_arrivals_route
        FOREIGN KEY (flight_number) REFERENCES FlightRoutes(flight_number)
        ON DELETE CASCADE
);

-- 2. Populate from the raw table --------------------------------------
--    Routes: one row per distinct flight number (removes transitive redundancy).
INSERT INTO FlightRoutes (flight_number, from_city)
SELECT DISTINCT flight, fromC
FROM rawflights;

--    Arrivals: every landing event. 'delay' maps to 'delay_status' AS IS (no CASE).
INSERT INTO Arrivals (flight_number, scheduled, delay_status, terminal)
SELECT flight, scheduled, delay, terminal
FROM rawflights;

-- 3. Drop the temporary flat table ------------------------------------
DROP TABLE rawflights;
