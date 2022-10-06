# Create database if you did not create before: python3 create_db.py
# Activate virtual environment: source simpleboun-env/bin/activate
# Run: python3 manage.py runserver
# Go to http://127.0.0.1:8000/

from cProfile import run
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from .forms import *
from .db_utils import run_statement
import hashlib

def encrypt_password(hash_string):
    sha_signature = hashlib.sha256(hash_string.encode()).hexdigest()
    return sha_signature

# Create your views here.

def index(request):
    if request.session:
        request.session.flush()

    isFailed=request.GET.get("fail",False) 

    dbmLoginForm = DBMLoginForm()
    instructorLoginForm = InstructorLoginForm()
    studentLoginForm = StudentLoginForm()

    return render(request, 'loginIndex.html', {
        "dbm_login_form":dbmLoginForm, 
        "instructor_login_form":instructorLoginForm, 
        "student_login_form":studentLoginForm, 
        "action_fail":isFailed})

def dbm_login(req):
    dbm_username=req.POST["dbm_username"]
    dbm_password=encrypt_password(req.POST["dbm_password"]) ## SHA256

    result = run_statement(f"""
    SELECT * 
    FROM Database_Manager 
    WHERE username='{dbm_username}' and password='{dbm_password}'
    """)
    if result: #If a result is retrieved
        req.session["dbm_username"]=dbm_username 
        return HttpResponseRedirect('../registration/dbm_home')
    else:
        return HttpResponseRedirect('../registration?fail=true')

def dbm_home(req):
    try:
        req.session["dbm_username"]
    except:
        return HttpResponseRedirect('../registration')

    return render(req,'dbmHome.html')

def dbm_add_user(req):
    dbmAddInstructorForm = DBMAddInstructorForm()
    dbmAddStudentForm = DBMAddStudentForm()

    added = req.GET.get("added",False) 
    error = req.GET.get("error",False) 
    return render(req,'dbmAddUser.html',{
        "dbm_add_instructor_form":dbmAddInstructorForm,
        "dbm_add_student_form":dbmAddStudentForm,
        "added":added,
        "error":error
        })

def dbm_add_instructor(req):
    instructor_username=req.POST["instructor_username"]
    instructor_password=encrypt_password(req.POST["instructor_password"]) ## SHA256
    instructor_email=req.POST["instructor_email"]
    instructor_name=req.POST["instructor_name"]
    instructor_surname=req.POST["instructor_surname"]
    instructor_department_id=req.POST["instructor_department_id"]
    instructor_title=req.POST["instructor_title"]
    try:
        run_statement(
            f"""
            INSERT INTO Instructor 
            VALUES('{instructor_username}','{instructor_title}',
                   '{instructor_password}','{instructor_name}',
                   '{instructor_surname}','{instructor_email}',
                   '{instructor_department_id}')
            """)
        return HttpResponseRedirect('../registration/dbm_add_user?added=true')
    except:
        return HttpResponseRedirect('../registration/dbm_add_user?error=true')

def dbm_add_student(req):
    student_username=req.POST["student_username"]
    student_student_id=req.POST["student_student_id"]
    student_password=encrypt_password(req.POST["student_password"]) ## SHA256
    student_email=req.POST["student_email"]
    student_name=req.POST["student_name"]
    student_surname=req.POST["student_surname"]
    student_department_id=req.POST["student_department_id"]
    try:
        run_statement(f"""
        INSERT INTO Student 
        VALUES('{student_username}','{student_student_id}','{student_password}',
               '{student_name}','{student_surname}','{student_email}',
               '{student_department_id}',0,0)
        """)
        
        return HttpResponseRedirect('../registration/dbm_add_user?added=true')
    except:
        return HttpResponseRedirect('../registration/dbm_add_user?error=true')


def dbm_delete_student_render(req):
    dbmDeleteStudentForm = DBMDeleteStudentForm()

    deleted = req.GET.get("deleted",False) 
    error = req.GET.get("error",False) 
    return render(req,'dbmDeleteStudent.html',{
        "dbm_delete_student_form":dbmDeleteStudentForm,
        "deleted":deleted,
        "error":error
        })

def dbm_delete_student(req):
    student_student_id=req.POST["student_student_id"]
    try:
        run_statement(
            f"DELETE FROM Student WHERE student_id='{student_student_id}'"
            )
        return HttpResponseRedirect('../registration/dbm_delete_student?deleted=true')

    except:
        return HttpResponseRedirect('../registration/dbm_delete_student?error=true')

def dbm_update_title_render(req):
    dbmUpdateTitleForm = DBMUpdateTitleForm()

    updated = req.GET.get("updated",False) 
    error = req.GET.get("error",False) 
    return render(req,'dbmUpdateTitle.html',{
        "dbm_update_title_form":dbmUpdateTitleForm,
        "updated":updated,
        "error":error
        })

def dbm_update_title(req):
    instructor_username=req.POST["instructor_username"]
    instructor_title=req.POST["instructor_title"]
    try:
        run_statement(f"""
        UPDATE Instructor 
        SET title='{instructor_title}' 
        WHERE username='{instructor_username}'
        """)
        return HttpResponseRedirect('../registration/dbm_update_title?updated=true')
    except:
        return HttpResponseRedirect('../registration/dbm_update_title?error=true')

def dbm_list_students(req):
    result=run_statement(f"""
    select s.username, s.name, s.surname, s.email, d.dep_id, s.completed_credits, s.gpa 
    from student as s, department as d 
    where d.dep_id = s.dep_id 
    order by s.completed_credits asc""") 
    return render(req,'dbmListStudents.html',{"results":result})

def dbm_list_instructors(req):
    result=run_statement(f"""
    SELECT i.username, i.name, i.surname, i.email, d.dep_id, i.title 
    FROM Instructor as i, Department as d 
    WHERE d.dep_id=i.dep_id""")
    return render(req,'dbmListInstructors.html',{"results":result})

def dbm_view_grades_render(req):
    dbmViewGradesForm = DBMViewGradesForm()
    return render(req, 'dbmViewGrades.html', {
        "dbm_view_grades_form":dbmViewGradesForm
        })

def dbm_view_grades(req):
    dbmViewGradesForm = DBMViewGradesForm()
    student_student_id = req.POST["student_student_id"]
    result=run_statement(f"""
    select student_grade.course_id ,course.name,student_grade.grade 
    from student_grade, course 
    where course.course_id = student_grade.course_id 
        and student_id = '{student_student_id}' """)
    return render(req, 'dbmViewGrades.html', {
        "stid":student_student_id,
        "dbm_view_grades_form":dbmViewGradesForm,
        "results":result
        })

def dbm_view_courses_render(req):
    dbmViewCoursesForm = DBMViewCoursesForm()
    return render(req, 'dbmViewCourses.html', {
        "dbm_view_courses_form":dbmViewCoursesForm
        })

def dbm_view_courses(req):
    dbmViewCoursesForm = DBMViewCoursesForm()
    instructor_username = req.POST["instructor_username"]
    result=run_statement(f"""
    SELECT c.course_id, c.name, c.classroom_id, x.campus, c.time_slot 
    FROM Course as c, Classroom as x 
    WHERE c.classroom_id = x.classroom_id 
        AND c.instructor_username='{instructor_username}'""")
    return render(req, 'dbmViewCourses.html', {
        "username":instructor_username,
        "dbm_view_courses_form":dbmViewCoursesForm,
        "results":result})

def dbm_grade_average_render(req):
    dbmGradeAverageForm = DBMGradeAverageForm()
    return render(req, 'dbmGradeAverage.html', {
        "dbm_grade_average_form":dbmGradeAverageForm
        })

def dbm_grade_average(req):
    dbmGradeAverageForm = DBMGradeAverageForm()
    course_id = req.POST["course_id"]
    result=run_statement(f"""
    select student_grade.course_id, course.name, avg(student_grade.grade) 
    from student_grade, course where course.course_id = student_grade.course_id 
        and course.course_id = '{course_id}' group by course_id""")
    return render(req, 'dbmGradeAverage.html', {
        "course_id":course_id,
        "dbm_grade_average_form":dbmGradeAverageForm,
        "results":result
        })


def instructor_login(req):
    instructor_username=req.POST["instructor_username"]
    instructor_password=encrypt_password(req.POST["instructor_password"])

    result = run_statement(f"""
    SELECT * FROM Instructor 
    WHERE username='{instructor_username}' 
    and password='{instructor_password}'
    """)
    if result: #If a result is retrieved
        req.session["instructor_username"]=instructor_username 
        return HttpResponseRedirect('../registration/instructor_home')
    else:
        return HttpResponseRedirect('../registration?fail=true')

def instructor_home(req):
    try:
        req.session["instructor_username"]
    except:
        return HttpResponseRedirect('../registration')

    return render(req,'instructorHome.html')

def instructor_view_classroom_render(req):
    instructorViewClassroomForm= InstructorViewClassroomForm()
    notvalid = req.GET.get("notvalid",False) 
    return render(req, 'instructorViewClassroom.html', {
        "instructor_view_classroom_form":instructorViewClassroomForm,
        "notvalid":notvalid
        })

def instructor_view_classroom(req):
    instructorViewClassroomForm= InstructorViewClassroomForm()
    time_slot = req.POST["time_slot"]
    if int(time_slot) < 1 or int(time_slot) > 10:
        return HttpResponseRedirect('../registration/instructor_view_classroom?notvalid=true')
    result=run_statement(f"""
    SELECT classroom.classroom_id, classroom.campus,classroom.capacity 
    FROM classroom  
    WHERE classroom.classroom_id not in 
        (SELECT classroom.classroom_id from course, classroom 
        WHERE course.time_slot = '{time_slot}' 
            AND course.classroom_id = classroom.classroom_id)
    """)
    return render(req, 'instructorViewClassroom.html', {
        "time_slot":time_slot,
        "instructor_view_classroom_form":instructorViewClassroomForm,
        "results":result
        })

def instructor_add_course_render(req):
    instructorAddCourseForm = InstructorAddCourseForm()
    added = req.GET.get("added",False) 
    error = req.GET.get("error",False) 
    return render(req,'instructorAddCourse.html',{
        "instructor_add_course_form":instructorAddCourseForm,
        "added":added,
        "error":error
        })

def instructor_add_course(req):
    course_id=req.POST["course_id"]
    course_name=req.POST["course_name"]
    credits=req.POST["credits"]
    classroom_id=req.POST["classroom_id"]
    time_slot=req.POST["time_slot"]
    quota=req.POST["quota"]
    instructor_username=req.session["instructor_username"]
    
    run_statement(f"""
    INSERT INTO Course 
    VALUES('{course_id}','{course_name}',{credits},{quota},
            '{classroom_id}',{time_slot},'{instructor_username}')
        """)
    return HttpResponseRedirect('../registration/instructor_add_course?added=true')
    
def instructor_add_pre_render(req):
    instructorAddPreForm = InstructorAddPreForm()
    added = req.GET.get("added",False) 
    error = req.GET.get("error",False) 
    wrong_course = req.GET.get("wrong_course",False) 
    return render(req,'instructorAddPre.html',{
        "instructor_add_pre_form":instructorAddPreForm,
        "added":added,
        "error":error,
        "wrong_course":wrong_course
        })

def instructor_add_pre(req):
    instructor_username=req.session["instructor_username"]
    course_id=req.POST["course_id"]
    pre_course_id=req.POST["pre_course_id"]

    courses_of_instr=run_statement(f"""
    SELECT * FROM Course 
    WHERE instructor_username='{instructor_username}'
    """)
    list = []
    for course in courses_of_instr:
        list.append(course[0])

    if course_id not in list:
        return HttpResponseRedirect('../registration/instructor_add_pre?wrong_course=true')

    try:
        run_statement(f"""
        INSERT INTO Prerequisite 
        VALUES('{course_id}','{pre_course_id}')
        """)
        return HttpResponseRedirect('../registration/instructor_add_pre?added=true')
    except:
        return HttpResponseRedirect('../registration/instructor_add_pre?error=true')

def instructor_given_courses(req):
    instructor_username=req.session["instructor_username"]
    result=run_statement(f"""
    SELECT course_id, name, credits, quota, classroom_id, time_slot 
    FROM Course 
    WHERE instructor_username='{instructor_username}'
    """)
    result = list(result)
    for i in range(len(result)):
        result[i] = list(result[i])
        course_id = result[i][0]
        pre_tuple = run_statement(f"""
        select pre_course_id 
        from prerequisite 
        where course_id='{course_id}'
        """)
        pre_string = ""
        for pre in list(pre_tuple):
            pre_string += list(pre)[0] + ","
        result[i].append(pre_string)
    return render(req,'instructorGivenCourses.html',{
        "results":result,
        "instructor_username":instructor_username
        })

def instructor_view_students_render(req):
    wrong_course = req.GET.get("wrong_course",False) 
    instructorViewStudentsForm = InstructorViewStudentsForm()
    return render(req,'instructorViewStudents.html',{
        "instructor_view_students_form":instructorViewStudentsForm,
        "wrong_course":wrong_course
        })

def instructor_view_students(req):
    instructor_username=req.session["instructor_username"]
    course_id=req.POST["course_id"]

    instructorViewStudentsForm = InstructorViewStudentsForm()

    courses_of_instr=run_statement(f"""
    SELECT * FROM Course 
    WHERE instructor_username='{instructor_username}'
    """)
    list = []
    for course in courses_of_instr:
        list.append(course[0])

    if course_id not in list:
        return HttpResponseRedirect('../registration/instructor_view_students?wrong_course=true')
    
    result = run_statement(f"""
    SELECT student.username,student.student_id,student.email,student.name,student.surname 
    FROM student,course_added 
    WHERE course_added.course_id = '{course_id}' 
        AND course_added.student_id = student.student_id
    """)
    return render(req, 'instructorViewStudents.html', {
        "course_id":course_id,
        "instructor_view_students_form":instructorViewStudentsForm,
        "results":result
        })

def instructor_update_course_render(req):
    instructorUpdateCourseForm = InstructorUpdateCourseForm()

    updated = req.GET.get("updated",False) 
    wrong_course = req.GET.get("wrong_course",False) 
    error = req.GET.get("error",False) 
    return render(req,'instructorUpdateCourse.html',{
        "instructor_update_course_form":instructorUpdateCourseForm,
        "updated":updated,
        "wrong_course":wrong_course,
        "error":error
        })

def instructor_update_course(req):
    instructor_username=req.session["instructor_username"]
    course_id=req.POST["course_id"]
    new_course_name=req.POST["new_course_name"]

    courses_of_instr=run_statement(f"""
    SELECT * 
    FROM Course 
    WHERE instructor_username='{instructor_username}'
    """)
    list = []
    for course in courses_of_instr:
        list.append(course[0]) 

    if course_id not in list:
        return HttpResponseRedirect('../registration/instructor_update_course?wrong_course=true')
    
    try:
        run_statement(
            f"UPDATE course SET name='{new_course_name}' where course_id='{course_id}'"
            )
        return HttpResponseRedirect('../registration/instructor_update_course?updated=true')
    except:
        return HttpResponseRedirect('../registration/instructor_update_course?error=true')

def instructor_give_grade_render(req):
    instructorGiveGradeForm = InstructorGiveGradeForm()

    updated = req.GET.get("updated",False) 
    wrong_course = req.GET.get("wrong_course",False) 
    error = req.GET.get("error",False) 
    return render(req,'instructorGiveGrade.html',{
        "instructor_give_grade_form":instructorGiveGradeForm,
        "updated":updated,
        "wrong_course":wrong_course,
        "error":error
        })

def instructor_give_grade(req):
    instructor_username=req.session["instructor_username"]
    course_id=req.POST["course_id"]
    student_id=req.POST["student_id"]
    grade=req.POST["grade"]

    # Get courses that this student take and this instructor gives
    available_courses=run_statement(f"""
    SELECT * FROM course AS c, course_added AS ca 
    WHERE ca.student_id = {student_id} AND
	c.course_id=ca.course_id AND
	c.instructor_username = '{instructor_username}'
    """)
    list = []
    for course in available_courses:
        list.append(course[0]) 

    # If this course is not a course that this student takes and this instructor gives
    # return a warning message in UI.
    if course_id not in list:
        return HttpResponseRedirect('../registration/instructor_give_grade?wrong_course=true')
    
    # Else, give grade...
    run_statement(f"""
    INSERT INTO student_grade 
    VALUES({student_id},'{course_id}',{grade})
    """)
    # ...and delete the course from student's added courses list.
    run_statement(f"""
    DELETE FROM course_added 
    WHERE student_id={student_id} 
        AND course_id='{course_id}'
    """)
    return HttpResponseRedirect('../registration/instructor_give_grade?updated=true')
   


def student_login(req):
    student_username=req.POST["student_username"]
    student_password=encrypt_password(req.POST["student_password"])

    result = run_statement(f"""
    SELECT * FROM Student 
    WHERE username='{student_username}' 
        AND password='{student_password}'
    """)
    if result: 
        req.session["student_username"]=student_username 
        return HttpResponseRedirect('../registration/student_home') 
    else:
        return HttpResponseRedirect('../registration?fail=true')

def student_home(req):
    try:
        req.session["student_username"]
    except:
        return HttpResponseRedirect('../registration') 
    return render(req,'studentHome.html')

def student_list_courses(req):
    
    result=run_statement(f"""
    select c.course_id, c.name, i.surname, d.dep_id, c.credits, 
           c.classroom_id, c.time_slot, c.quota 
    from course as c, instructor as i, department as d 
    where c.instructor_username = i.username and i.dep_id=d.dep_id
    """) 
    result = list(result)
    for i in range(len(result)):
        result[i] = list(result[i])
        course_id = result[i][0]
        pre_tuple = run_statement(f"""
        select pre_course_id 
        from prerequisite 
        where course_id='{course_id}'
        """)
        pre_string = ""
        for pre in list(pre_tuple):
            pre_string += list(pre)[0] + ","
        result[i].append(pre_string)

    return render(req,'studentListCourses.html',{"results":result})

def student_add_course_render(req):
    studentAddCourseForm = StudentAddCourseForm()

    added = req.GET.get("added",False) 
    error = req.GET.get("error",False) 
    graded = req.GET.get("graded",False) 
    prenotsatisfied = req.GET.get("prenotsatisfied",False) 
    quotafull = req.GET.get("quotafull",False) 

    return render(req,'studentAddCourse.html',{
        "student_add_course_form":studentAddCourseForm,
        "added":added,
        "error":error,
        "graded":graded,
        "prenotsatisfied":prenotsatisfied,
        "quotafull":quotafull
        })

def student_add_course(req):
    course_id=req.POST["course_id"]
    student_username=req.session["student_username"]

    # Get student id:
    student_id_tuple = run_statement(f"""
    SELECT student_id 
    FROM Student 
    WHERE username ='{student_username}'
    """)
    student_id = student_id_tuple[0][0]

    # A student cannot take a course twice:
    completed_courses_tuple = run_statement(f"""
    SELECT course_id 
    FROM Student_Grade 
    WHERE student_id={student_id}
    """)
    completed_courses = []
    for course_info in completed_courses_tuple:
        completed_courses.append(course_info[0])
    
    if course_id in completed_courses:
        return HttpResponseRedirect('../registration/student_add_course?graded=true')

    # Should have grade from all of the prerequisites:
    pre_tuple = run_statement(f"""
    SELECT pre_course_id 
    FROM Prerequisite 
    WHERE course_id='{course_id}'
    """)
    for pre in pre_tuple:
        pre = pre[0]
        if pre not in completed_courses:
            return HttpResponseRedirect('../registration/student_add_course?prenotsatisfied=true')
    
    # Quota of the course should not be full:
    quota = run_statement(f"""
    SELECT quota 
    FROM Course 
    WHERE course_id='{course_id}'
    """)
    current_number = run_statement(f"""
    SELECT count(student_id) 
    FROM Course_Added 
    WHERE course_id='{course_id}'
    """)
    if current_number >= quota:
        return HttpResponseRedirect('../registration/student_add_course?quotafull=true')

    try:
        run_statement(
           f"INSERT INTO Course_Added VALUES('{student_id}','{course_id}')"
           )
        return HttpResponseRedirect('../registration/student_add_course?added=true')
    except:
        return HttpResponseRedirect('../registration/student_add_course?error=true')


def student_view_your_courses(req):
    student_username=req.session["student_username"]
    # Get student id:
    student_id_tuple = run_statement(f"""
        SELECT student_id FROM Student WHERE username ='{student_username}'
        """)
    student_id = student_id_tuple[0][0]

    result=run_statement(f"""
        SELECT course.course_id, course.name, student_grade.grade 
        FROM Course 
        LEFT JOIN Student_Grade ON course.course_id = student_grade.course_id 
        LEFT JOIN Course_Added ON course.course_id = course_added.course_id 
        WHERE student_grade.student_id = {student_id} 
            OR course_added.student_id = {student_id}
        """)
    return render(req,'studentViewYourCourses.html',{"results":result})

def student_search_course_render(req):
    studentSearchCourseForm = StudentSearchCourseForm()

    return render(req,'studentSearchCourse.html',{
        "student_search_course_form":studentSearchCourseForm,
        })

def student_search_course(req):
    studentSearchCourseForm = StudentSearchCourseForm()
    keyword=req.POST["keyword"]
    result=run_statement(f"""
        SELECT course.course_id, course.name, instructor.surname, 
            department.dep_id, course.credits, course.classroom_id, 
            course.time_slot, course.quota 
        FROM course, instructor, department 
        WHERE locate('{keyword}',course.name) > 0 AND 
        instructor.username = course.instructor_username AND 
        instructor.dep_id=department.dep_id
        """)
    result = list(result)
    for i in range(len(result)):
        result[i] = list(result[i])
        course_id = result[i][0]
        pre_tuple = run_statement(f"""
        select pre_course_id from prerequisite where course_id='{course_id}'
        """)
        pre_string = ""
        for pre in list(pre_tuple):
            pre_string += list(pre)[0] + ","
        result[i].append(pre_string)

    return render(req,'studentSearchCourse.html',{
        "student_search_course_form":studentSearchCourseForm,
        "results":result,
        "keyword":keyword
        })

def student_filter_course_render(req):
    studentFilterCourseForm = StudentFilterCourseForm()

    return render(req,'studentFilterCourse.html',{
        "student_filter_course_form":studentFilterCourseForm,
        })

def student_filter_course(req):
    studentFilterCourseForm = StudentFilterCourseForm()

    department=req.POST["department"]
    campus=req.POST["campus"]
    minimum_credits=req.POST["minimum_credits"]
    maximum_credits=req.POST["maximum_credits"]

    department_id=run_statement(f"""
        SELECT dep_id FROM department where name='{department}'
    """)

    result=run_statement(
           f"""
            CALL get_courses('{department_id[0][0]}', '{campus}', 
                {minimum_credits}, {maximum_credits})
           """
           )

    result = list(result)
    for i in range(len(result)):
        result[i] = list(result[i])
        course_id = result[i][0]
        pre_tuple = run_statement(f"""
        select pre_course_id from prerequisite where course_id='{course_id}'
        """)
        pre_string = ""
        for pre in list(pre_tuple):
            pre_string += list(pre)[0] + ","
        result[i].append(pre_string)

    return render(req,'studentFilterCourse.html',{
        "student_filter_course_form":studentFilterCourseForm,
        "results":result,
        })