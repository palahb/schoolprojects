#include <QtGui>
#include <QNetworkAccessManager>
#include <QNetworkRequest>
#include <QNetworkReply>
#include <QLabel>
#include <QHBoxLayout>
#include <QTableWidget>

using namespace std;
/*
  This is the header file of our widget which shows the
  prices of coins.
*/
class MyClass : public QWidget
{
    Q_OBJECT

    public:
      vector<string> cryptos; // This vector will contains given names, symbols or ids of coins.
      QVector<QString> *ids;  // This vector will contain ids of the coins.
      QVector<QString> *names;// This vector will contain names of the coins.
      MyClass(vector<string> cryptos, QWidget *parent = 0); // Contructor
	 
    public slots:
      void getIds(QNetworkReply * reply); // This slot is for getting id's of the coins.
      void replyFinished(QNetworkReply * reply);  // This slot is for getting exchange rates of the coins.

    private:
      QTableWidget *tableWidget;
      QNetworkAccessManager *manager;// This manager is for getting the exchange rates.
      QNetworkAccessManager *idGetter;// This manager is for getting the ids of the coins for
                                      // getting the exchange rates.

} ;
