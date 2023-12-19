-- create the users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    email VARCHAR(25) UNIQUE NOT NULL,
    password VARCHAR(20) NOT NULL,
    isDoctor BOOLEAN NOT NULL
);
