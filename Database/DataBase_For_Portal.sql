-- User Table
CREATE TABLE "User" (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_first_login BOOLEAN DEFAULT TRUE,
    ROLE VARCHAR(20) NOT NULL
);

-- Role Table
CREATE TABLE Role (
    role_name VARCHAR(20) PRIMARY KEY
);

-- Student Table
CREATE TABLE Student (
    student_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    Rollno VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    GENDER VARCHAR(10) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "User"(user_id)
);

-- GENDER Table
CREATE TABLE GENDER (
    gender_value VARCHAR(10) PRIMARY KEY
);

-- Assignment Table
CREATE TABLE Assignment (
    assignment_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    Subject_Code VARCHAR(20) NOT NULL,
    Subject_Title VARCHAR(255) NOT NULL,
    Description TEXT,
    Start_time TIMESTAMP NOT NULL,
    End_time TIMESTAMP NOT NULL,
    ASSIGNMENT_TYPE VARCHAR(20) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "User"(user_id)
);

-- ASSIGNMENT_TYPE Table
CREATE TABLE ASSIGNMENT_TYPE (
    type_name VARCHAR(20) PRIMARY KEY
);

-- Submission Table
CREATE TABLE Submission (
    Submission_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL,
    assignment_id INT NOT NULL,
    file_path VARCHAR(500),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_name VARCHAR(255),
    file_data BYTEA,
    file_size BIGINT,
    FOREIGN KEY (student_id) REFERENCES Student(student_id),
    FOREIGN KEY (assignment_id) REFERENCES Assignment(assignment_id)
);

-- PASSWORD_RESET_token Table
CREATE TABLE PASSWORD_RESET_token (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "User"(user_id)
);

-- email_logs Table
CREATE TABLE email_logs (
    email_id SERIAL PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'SENT'
);