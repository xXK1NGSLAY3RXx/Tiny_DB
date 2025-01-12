SQL Query Processor

Overview

This program is a simple SQL query processor written in Java. It supports basic SQL commands such as CREATE, USE, INSERT, SELECT, UPDATE, DELETE, and DROP. The program is designed to manage databases, tables, and perform various operations on the tables. The primary goal is to provide a lightweight and easy-to-use tool for managing relational data.

Classes and Responsibilities

Attribute: Represents an attribute (column) in a table. It includes properties such as name, type, size, primary key, not null, and unique constraints.

ColumnType: Enum representing the data types supported by the system (e.g., INT, FLOAT, STRING, DATE, BOOLEAN, DOUBLE).

Database: Represents a database containing multiple tables.

Manager: Manages multiple databases and performs operations such as creating databases, using databases, and managing tables within the current database.

QueryProcessor: Processes SQL queries, delegates actions to the Manager, and provides feedback on query execution.
Table: Represents a table in a database and provides methods to insert, select, update, and delete rows.

Usage

Initial Setup
Initialize the Manager: The Manager class is responsible for managing databases and performing operations on the current database.
Create a QueryProcessor: The QueryProcessor class processes SQL queries and uses the Manager to execute them.

Sample Queries
Below are examples of how to use the available features:

Create a Database
CREATE DATABASE School;

Use a Database
USE DATABASE School;

Create a Table
CREATE TABLE Student (
    id INT PRIMARY KEY,
    name STRING NOT NULL,
    email STRING UNIQUE,
    phone STRING UNIQUE,
    age INT
);

Insert Data into a Table
INSERT INTO Student (id, name, email, phone, age) VALUES (1, 'John Doe', 'john.doe@example.com', '123-456-7890', 20);
INSERT INTO Student (id, name, email, phone, age) VALUES (2, 'Jane Smith', 'jane.smith@example.com', '234-567-8901', 22);
INSERT INTO Student (id, name, email, phone, age) VALUES (3, 'Alice Johnson', 'alice.johnson@example.com', '345-678-9012', 21);

Select Data from a Table
SELECT id, name, email FROM Student;

Update Data in a Table
UPDATE Student SET age = 23 WHERE id = 2;

Delete Data from a Table
DELETE FROM Student WHERE id = 1;

Drop a Table
DROP TABLE Student;



Overall Flow
Initialize Manager and QueryProcessor:

The Manager is instantiated to handle multiple databases.
The QueryProcessor is instantiated with the Manager to process SQL queries.
Process Queries:

The QueryProcessor processes a list of queries.
For each query, it determines the command (CREATE, USE, INSERT, etc.).
It delegates the action to the appropriate method in the Manager.
Manager Operations:

The Manager creates and uses databases, and delegates table operations to the current Database.
It manages the current database context for subsequent operations.
Database and Table Operations:

The Database class handles table creation and lookup.
The Table class handles row operations (insert, select, update, delete) and enforces constraints (e.g., primary key, unique).
Example Workflow

// Initialize Manager
Manager manager = new Manager();

// Initialize QueryProcessor
QueryProcessor queryProcessor = new QueryProcessor(manager);

// List of SQL queries
List<String> queries = Arrays.asList(
    "CREATE DATABASE School;",
    "USE DATABASE School;",
    "CREATE TABLE Student (id INT PRIMARY KEY, name STRING NOT NULL, email STRING UNIQUE, phone STRING UNIQUE, age INT);",
    "INSERT INTO Student (id, name, email, phone, age) VALUES (1, 'John Doe', 'john.doe@example.com', '123-456-7890', 20);",
    "INSERT INTO Student (id, name, email, phone, age) VALUES (2, 'Jane Smith', 'jane.smith@example.com', '234-567-8901', 22);",
    "SELECT id, name, email FROM Student;",
    "UPDATE Student SET age = 23 WHERE id = 2;",
    "DELETE FROM Student WHERE id = 1;",
    "DROP TABLE Student;"
);

queryProcessor.processQueries(queries);
