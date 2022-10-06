#include <QtCore>
#include <QApplication>
#include "myclass.h"
#include <fstream>

using namespace std;
/*
	CMPE 230 Spring 2021 Project-3
	Halil Burak Pala

	This is our main function.
	Here, I get coin names from a file whose name
	is given in an environment variable named "MYCRYPTOCONVERT".
	Then I put the names into a vector and send it to the
	our main widget.
*/
int main(int argc,char *argv[])
{
   	QApplication a(argc, argv); 

	const char* env_p = getenv("MYCRYPTOCONVERT");
	ifstream input(env_p);
	string line;
	vector<string> cryptoslist;

	while(getline(input,line)){
		cryptoslist.push_back(line);
	}

   	MyClass my(cryptoslist) ; 
   	my.resize(640,640);
   	my.show(); 

   	return a.exec();
}
