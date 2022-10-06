-- Firstly,we created a Databse named "SimpleBoun" and then used it. 
CREATE DATABASE IF NOT EXISTS SimpleBoun;
USE SimpleBoun;

-- We created a table for departments that contains department id and name field.
-- Department id is a primary key of the relation.Name is a unique attribute..
CREATE TABLE IF NOT EXISTS Department(
dep_id INT,
name VARCHAR(20),
PRIMARY KEY(dep_id),
UNIQUE(name)
);

-- We created a table for Students that contains username, student id, password, name,surname, email and dep_id attribute.
-- username is a primary key and student_id is unique for each student. dep_id is a field taken from Department (foreign key)
-- Each student has to be in a Department so it can not be null. 
CREATE TABLE IF NOT EXISTS Student (
username VARCHAR(20),
student_id INT,
password VARCHAR(20),
name VARCHAR(20),
surname VARCHAR(20),
email VARCHAR(20),
dep_id INT NOT NULL,
PRIMARY KEY(username),
UNIQUE(student_id),
FOREIGN KEY(dep_id) REFERENCES Department(dep_id) ON DELETE CASCADE
);

-- We created a table for Instructors that contains username,title, password,name,surname,email and dep_id.
-- username is a primary key for Instructor table.dep_id is foreign key that is taken frm Department table.
-- Each Instructor should be in a Department so dep_id can not be null. 
CREATE TABLE IF NOT EXISTS Instructor (
username VARCHAR(20),
title VARCHAR(20),
password VARCHAR(20),
name VARCHAR(20),
surname VARCHAR(20),
email VARCHAR(20),
dep_id INT NOT NULL,
PRIMARY KEY(username),
FOREIGN KEY(dep_id) REFERENCES Department(dep_id) ON DELETE CASCADE
);

-- We created a table for Classroom that contains classroom_id, campus and capacity.
-- classroom_id is a primary key of the table.
CREATE TABLE IF NOT EXISTS Classroom (
classroom_id INT,
campus VARCHAR(20),
capacity INT,
PRIMARY KEY(classroom_id)
);

-- We created a table for Course that contains course_id, name, credits, quota,classroom_id,time_slot and instructor_username.
-- Each Course should be given by a instructor. Therefore, instructor_username can not be null.
-- classroom_id and time_slot pair is a unique for each course since two courses can not be at the same time and in same classroom.
-- A instructor can not give two different courses at the same time.Therefore,time_slot and instructor_username should be unique.
-- course_id is primary key of the relation.
-- classroom_id is taken from Classroom table.
-- instructor_username is taken from Instructor table.
-- We checked time_slot to be in [1,10] interval.
CREATE TABLE IF NOT EXISTS Course (
course_id INT,
name VARCHAR(20),
credits INT,
quota INT,
classroom_id INT NOT NULL,
time_slot INT NOT NULL,
instructor_username VARCHAR(20) NOT NULL,
UNIQUE(classroom_id, time_slot),
UNIQUE(time_slot, instructor_username),
PRIMARY KEY(course_id),
FOREIGN KEY(classroom_id) REFERENCES Classroom(classroom_id) ON DELETE CASCADE,
FOREIGN KEY(instructor_username) REFERENCES Instructor(username) ON DELETE CASCADE,
CHECK (1<=time_slot<=10)
);

-- We created a table for Prerequisites of Courses that contains course_id and pre_course_id.
-- course_id and pre_course_id pair is a primary key.
-- course_id and pre_course_id is taken from Course Table.
-- We checked that course_id greater than pre_course_id in terms of string comparison.
CREATE TABLE IF NOT EXISTS Prerequisite (
course_id INT,
pre_course_id INT,
PRIMARY KEY(course_id, pre_course_id),
FOREIGN KEY(course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
FOREIGN KEY(pre_course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
CHECK (STRCMP(course_id,pre_course_id)=1)
);

-- We created Student Grade table to store students grades taken from a course.
-- Each student take only one grade for a course.Therefore,we used student_id and course_id pair as a primary key.
-- student_id is taken from Student Table.
-- course_id is taken from  Course table. 
CREATE TABLE IF NOT EXISTS Student_Grade(
student_id INT,
course_id INT,
grade REAL,
PRIMARY KEY(student_id,course_id),
FOREIGN KEY(student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
FOREIGN KEY(course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);

-- We created a table to store who added which course.
-- Each student can add a course one time.Therefore, student_id and course_id pair is a primary key.
-- student_id is taken  from Student table.
-- course_id is taken from Course.
CREATE TABLE IF NOT EXISTS Course_Added(
student_id INT,
course_id INT,
PRIMARY KEY(student_id, course_id),
FOREIGN KEY(student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
FOREIGN KEY(course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);

-- We create a table to store Database Managers.
-- Each Database Manager contains username and password attribute.
-- username is a primary key indeed no two course with the same username
CREATE TABLE IF NOT EXISTS Database_Manager(
username VARCHAR(20),
password VARCHAR(20),
PRIMARY KEY(username)
)
