import mysql.connector
import os
from mysql.connector import Error

try:
    connection = mysql.connector.connect(
    host="localhost",
    user="root",
    password=os.environ["MYSQL_PASSWORD"],
    database="SimpleBoun",
    auth_plugin='mysql_native_password'
    )

    cursor= connection.cursor()

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Department(
        dep_id VARCHAR(20),
        name VARCHAR(200),
        PRIMARY KEY(dep_id),
        UNIQUE(name)
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Student (
        username VARCHAR(200),
        student_id INT,
        password VARCHAR(200),
        name VARCHAR(200),
        surname VARCHAR(200),
        email VARCHAR(200),
        dep_id VARCHAR(20) NOT NULL,
        completed_credits INT DEFAULT 0, -- project 3
        gpa REAL DEFAULT 0, -- project 3
        PRIMARY KEY(username),
        UNIQUE(student_id),
        FOREIGN KEY(dep_id) REFERENCES Department(dep_id) ON DELETE CASCADE
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Instructor (
        username VARCHAR(200),
        title VARCHAR(200),
        password VARCHAR(200),
        name VARCHAR(200),
        surname VARCHAR(200),
        email VARCHAR(200),
        dep_id VARCHAR(20) NOT NULL,
        PRIMARY KEY(username),
        FOREIGN KEY(dep_id) REFERENCES Department(dep_id) ON DELETE CASCADE
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Classroom (
        classroom_id VARCHAR(20),
        campus VARCHAR(200),
        capacity INT,
        PRIMARY KEY(classroom_id)
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Course (
        course_id VARCHAR(200), 
        name VARCHAR(200),
        credits INT,
        quota INT,
        classroom_id VARCHAR(20) NOT NULL,
        time_slot INT NOT NULL,
        instructor_username VARCHAR(200) NOT NULL,
        UNIQUE(classroom_id, time_slot),
        UNIQUE(time_slot, instructor_username),
        PRIMARY KEY(course_id),
        FOREIGN KEY(classroom_id) REFERENCES Classroom(classroom_id) ON DELETE CASCADE,
        FOREIGN KEY(instructor_username) REFERENCES Instructor(username) ON DELETE CASCADE,
        CHECK (1<=time_slot<=10)
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Prerequisite (
        course_id VARCHAR(200),
        pre_course_id VARCHAR(200),
        PRIMARY KEY(course_id, pre_course_id),
        FOREIGN KEY(course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
        FOREIGN KEY(pre_course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
        CHECK (STRCMP(course_id,pre_course_id)=1)
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Student_Grade(
        student_id INT,
        course_id VARCHAR(200),
        grade REAL,
        PRIMARY KEY(student_id,course_id),
        FOREIGN KEY(student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
        FOREIGN KEY(course_id) REFERENCES Course(course_id) ON DELETE CASCADE
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Course_Added(
        student_id INT,
        course_id VARCHAR(200),
        PRIMARY KEY(student_id, course_id),
        FOREIGN KEY(student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
        FOREIGN KEY(course_id) REFERENCES Course(course_id) ON DELETE CASCADE
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS Database_Manager(
        username VARCHAR(200),
        password VARCHAR(200),
        PRIMARY KEY(username)
        )
    """)

    cursor.execute("""
    CREATE PROCEDURE get_courses (department_id VARCHAR(20),campus VARCHAR(200),min_credits INT,max_credits INT)   
        select c.course_id, c.name, i.surname, d.name, c.credits, c.classroom_id, c.time_slot, c.quota 
        from course as c,instructor as i, department as d, classroom as cl 
        where c.instructor_username = i.username and i.dep_id = department_id and d.dep_id = i.dep_id and 
        c.credits >=min_credits and c.credits <=max_credits and cl.campus=campus and cl.classroom_id=c.classroom_id
    """)

    cursor.execute("""
    CREATE TRIGGER update_credits AFTER INSERT ON student_grade
    FOR EACH ROW
    UPDATE student SET completed_credits = 
        completed_credits + (select course.credits from course where course.course_id = NEW.course_id) 
    WHERE student.student_id = NEW.student_id
    """)  
    
    cursor.execute("""
    CREATE TRIGGER update_gpa AFTER INSERT ON student_grade
    FOR EACH ROW
    UPDATE Student SET gpa = 
        ((completed_credits-(select course.credits from course where course.course_id = NEW.course_id))*gpa 
        + (select course.credits from course where course.course_id = NEW.course_id) *NEW.grade )
        / completed_credits
    WHERE Student.student_id = NEW.student_id
    """)

    cursor.execute("""
    CREATE TRIGGER course_quota AFTER INSERT on course
    FOR EACH ROW
    IF NEW.quota > (select classroom.capacity from classroom where classroom.classroom_id = NEW.classroom_id)  THEN 
    SIGNAL SQLSTATE '50001' SET MESSAGE_TEXT = "The quota of the course should be less than or equal to classroom capacity. " ;
    END IF;
    """)

    connection.commit()


except Error as e:
    print("Error while connecting to MySQL", e)
