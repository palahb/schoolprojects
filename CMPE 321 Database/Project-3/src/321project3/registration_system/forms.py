from django import forms
from .db_utils import run_statement

titles = (('Assistant Professor','Assistant Professor'),('Associate Professor','Associate Professor'),('Professor','Professor'))
#grades = ((4.0,'AA'),(3.5,'BA'),(3.0,'BB'),(2.5,'CB'),(2.0,'CC'),(1.5,'DC'),(1.0,'DD'))

class DBMLoginForm(forms.Form):
    dbm_username=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Username'}))
    dbm_password=forms.CharField(widget=forms.PasswordInput)

class InstructorLoginForm(forms.Form):
    instructor_username=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Username'}))
    instructor_password=forms.CharField(widget=forms.PasswordInput)

class StudentLoginForm(forms.Form):
    student_username=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Username'}))
    student_password=forms.CharField(widget=forms.PasswordInput)

class DBMAddInstructorForm(forms.Form):
    instructor_username=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Username'}))
    instructor_password=forms.CharField(widget=forms.PasswordInput)
    instructor_email=forms.CharField(widget=forms.TextInput(attrs={'placeholder': 'Email'}))
    instructor_name=forms.CharField(widget=forms.TextInput(attrs={'placeholder': 'Name'}))
    instructor_surname=forms.CharField(widget=forms.TextInput(attrs={'placeholder': 'Surname'}))
    instructor_department_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder': 'Department Id'}))
    instructor_title=forms.ChoiceField(choices=titles)

class DBMAddStudentForm(forms.Form):
    student_username=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Username'}))
    student_password=forms.CharField(widget=forms.PasswordInput)
    student_email=forms.CharField(widget=forms.TextInput(attrs={'placeholder': 'Email'}))
    student_name=forms.CharField(widget=forms.TextInput(attrs={'placeholder': 'Name'}))
    student_surname=forms.CharField(widget=forms.TextInput(attrs={'placeholder': 'Surname'}))
    student_department_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder': 'Department Id'}))
    student_student_id=forms.IntegerField(widget=forms.TextInput(attrs={'placeholder': 'Student Id'}))

class DBMDeleteStudentForm(forms.Form):
    student_student_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Student ID'}))

class DBMUpdateTitleForm(forms.Form):
    instructor_username=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Username'}))
    instructor_title=forms.ChoiceField(choices=titles)

class DBMViewGradesForm(forms.Form):
    student_student_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Student ID'}))

class DBMViewCoursesForm(forms.Form):
    instructor_username=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Username'}))

class DBMGradeAverageForm(forms.Form):
    course_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Course ID'}))


class InstructorLoginForm(forms.Form):
    instructor_username=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Username'}))
    instructor_password=forms.CharField(widget=forms.PasswordInput)

class InstructorViewClassroomForm(forms.Form):
    time_slot=forms.IntegerField(widget=forms.TextInput(attrs={'placeholder':'Time Slot'}))

class InstructorAddCourseForm(forms.Form):
    course_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Course ID'}))
    course_name=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Course Name'}))
    credits=forms.IntegerField(widget=forms.TextInput(attrs={'placeholder':'Credits'}))
    classroom_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Classroom ID'}))
    time_slot=forms.IntegerField(widget=forms.TextInput(attrs={'placeholder':'Time Slot'}))
    quota=forms.IntegerField(widget=forms.TextInput(attrs={'placeholder':'Quota'}))

class InstructorAddPreForm(forms.Form):
    course_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Course ID'}))
    pre_course_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Prerequisite Course ID'}))

class InstructorViewStudentsForm(forms.Form):
    course_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Course ID'}))

class InstructorUpdateCourseForm(forms.Form):
    course_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Course ID'}))
    new_course_name=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'New Course Name'}))

class InstructorGiveGradeForm(forms.Form):
    course_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Course ID'}))
    student_id=forms.IntegerField(widget=forms.TextInput(attrs={'placeholder':'Student ID'}))
    grade=forms.FloatField(widget=forms.TextInput(attrs={'placeholder':'Grade'}))

class StudentAddCourseForm(forms.Form):
    course_id=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Course ID'}))

class StudentSearchCourseForm(forms.Form):
    keyword=forms.CharField(widget=forms.TextInput(attrs={'placeholder':'Search a keyword'}))

class StudentFilterCourseForm(forms.Form):
    departments = run_statement(f"SELECT name FROM Department")
    list_of_departments = []
    for dep in departments:
        list_of_departments.append((dep[0],dep[0]))
    department=forms.ChoiceField(choices=tuple(list_of_departments))

    campuses = run_statement(f"SELECT DISTINCT campus FROM Classroom")
    list_of_campuses = []
    for campus_info in campuses:
        list_of_campuses.append((campus_info[0],campus_info[0]))
    campus=forms.ChoiceField(choices=tuple(list_of_campuses))

    minimum_credits=forms.IntegerField(widget=forms.NumberInput())
    maximum_credits=forms.IntegerField(widget=forms.NumberInput())

