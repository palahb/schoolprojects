1) Navigate to 321project3 folder

2) Firstly, create a virtual environment:
python3 -m venv simpleboun-env

3) Activate this virtual environment:
source simpleboun-env/bin/activate

4) Then install packages specified in requirements.txt file:
pip3 install -r requirements.txt

5) Create the database:
python3 create_db.py

6) Run the application:
python3 manage.py runserver

7) Go to http://127.0.0.1:8000/