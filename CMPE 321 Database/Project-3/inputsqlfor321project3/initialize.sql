use simpleboun;
insert into Department values('CMPE','Computer Engineering');
insert into Department values('EE', 'Electrical and Electronics Engineering');
insert into Department values('ME', 'Mechanical Engineering');
insert into Department values('IE', 'Industrial Engineering');
insert into Department values('ChE', 'Chemical Engineering');
insert into Department values('MATH', 'Mathematics');
insert into Department values('PHYS', 'Physics');
insert into Department values('CHEM', 'Chemistry');
insert into Department values('MAN', 'Management');
insert into Department values('POLS', 'Politics');

insert into classroom values("M101","South Campus",70);
insert into classroom values("M102","South Campus",60);
insert into classroom values("M103","South Campus",40);
insert into classroom values("M104","South Campus",20);
insert into classroom values("M105","South Campus",100);
insert into classroom values("NH201","North Campus",40);
insert into classroom values("NH202","North Campus",120);
insert into classroom values("NH203","North Campus",90);
insert into classroom values("NH204","North Campus",40);
insert into classroom values("NH205","North Campus",45);

/*
-- Query: SELECT * FROM SimpleBoun.Instructor
LIMIT 0, 1000

-- Date: 2022-04-30 12:45
*/
INSERT INTO `Instructor` (`username`,`title`,`password`,`name`,`surname`,`email`,`dep_id`) VALUES ('arzucan.ozgur','Professor','5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Arzucan','Özgür','arzucan.ozgur@boun.edu.tr','CMPE');
INSERT INTO `Instructor` (`username`,`title`,`password`,`name`,`surname`,`email`,`dep_id`) VALUES ('birkan.yilmaz','Assistant Professor','5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Birkan','Yılmaz','birkaz.yilmaz@boun.edu.tr','CMPE');
INSERT INTO `Instructor` (`username`,`title`,`password`,`name`,`surname`,`email`,`dep_id`) VALUES ('cem.ersoy','Pofessor','5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Cem','Ersoy','cem.ersoy@boun.edu.tr','CMPE');
INSERT INTO `Instructor` (`username`,`title`,`password`,`name`,`surname`,`email`,`dep_id`) VALUES ('cem.say','Associate Professor','5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Cem','Say','cem.say@boun.edu.tr','CMPE');
INSERT INTO `Instructor` (`username`,`title`,`password`,`name`,`surname`,`email`,`dep_id`) VALUES ('inci.baytas','Associate Professor','5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','İnci','Baytaş','inci.baytas@boun.edu.tr','CMPE');
INSERT INTO `Instructor` (`username`,`title`,`password`,`name`,`surname`,`email`,`dep_id`) VALUES ('tuna.tugcu','Professor','5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Tuna','Tuğcu','tuna.tugcu@boun.edu.tr','CMPE');

/*
-- Query: SELECT * FROM SimpleBoun.Course
LIMIT 0, 1000

-- Date: 2022-04-30 12:46
*/
INSERT INTO `Course` (`course_id`,`name`,`credits`,`quota`,`classroom_id`,`time_slot`,`instructor_username`) VALUES ('CMPE 150','Introduction to Computing',4,95,"M105",2,'arzucan.ozgur');
INSERT INTO `Course` (`course_id`,`name`,`credits`,`quota`,`classroom_id`,`time_slot`,`instructor_username`) VALUES ('CMPE 160','Object Oriented Programming',4,80,"NH203",1,'tuna.tugcu');
INSERT INTO `Course` (`course_id`,`name`,`credits`,`quota`,`classroom_id`,`time_slot`,`instructor_username`) VALUES ('CMPE 250','Data Structures',4,100,"NH202",3,'birkan.yilmaz');
INSERT INTO `Course` (`course_id`,`name`,`credits`,`quota`,`classroom_id`,`time_slot`,`instructor_username`) VALUES ('CMPE 300','Operating Systems',4,100,"NH202",2,'tuna.tugcu');
INSERT INTO `Course` (`course_id`,`name`,`credits`,`quota`,`classroom_id`,`time_slot`,`instructor_username`) VALUES ('CMPE 321','Database',3,65,"M101",3,'arzucan.ozgur');
INSERT INTO `Course` (`course_id`,`name`,`credits`,`quota`,`classroom_id`,`time_slot`,`instructor_username`) VALUES ('CMPE 350','Automata Theory',3,35,"NH201",1,'cem.say');

/*
-- Query: SELECT * FROM SimpleBoun.Student
LIMIT 0, 1000

-- Date: 2022-04-30 12:46
*/
INSERT INTO `Student` (`username`,`student_id`,`password`,`name`,`surname`,`email`,`dep_id`,`completed_credits`,`gpa`) VALUES ('ata.solak',2019765342,'5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Ata','Solak','ata.solak@boun.edu.tr','IE',0,0);
INSERT INTO `Student` (`username`,`student_id`,`password`,`name`,`surname`,`email`,`dep_id`,`completed_credits`,`gpa`) VALUES ('berat.damar',2018400039,'5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Berat','Damar','berat.damar@boun.edu.tr','CMPE',0,0);
INSERT INTO `Student` (`username`,`student_id`,`password`,`name`,`surname`,`email`,`dep_id`,`completed_credits`,`gpa`) VALUES ('ferhat.kaya',2018401324,'5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Ferhat','Kaya','ferhat.kaya@boun.edu.tr','ME',0,0);
INSERT INTO `Student` (`username`,`student_id`,`password`,`name`,`surname`,`email`,`dep_id`,`completed_credits`,`gpa`) VALUES ('halil.pala',2019400282,'5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Halil Burak','Pala','halil.pala@boun.edu.tr','CMPE',0,0);
INSERT INTO `Student` (`username`,`student_id`,`password`,`name`,`surname`,`email`,`dep_id`,`completed_credits`,`gpa`) VALUES ('lokman.ilkin',2017405453,'5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5','Lokman','İlkin','lokman.ilkin@boun.edu.tr','MATH',0,0);

/*
-- Query: SELECT * FROM SimpleBoun.Prerequisite
LIMIT 0, 1000

-- Date: 2022-04-30 12:47
*/
INSERT INTO `Prerequisite` (`course_id`,`pre_course_id`) VALUES ('CMPE 250','CMPE 150');
INSERT INTO `Prerequisite` (`course_id`,`pre_course_id`) VALUES ('CMPE 250','CMPE 160');

/*
-- Query: SELECT * FROM SimpleBoun.Database_Manager
LIMIT 0, 1000

-- Date: 2022-04-30 12:47
*/
INSERT INTO `Database_Manager` (`username`,`password`) VALUES ('admin','5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5');
