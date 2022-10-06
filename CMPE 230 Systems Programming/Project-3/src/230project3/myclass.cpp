#include <QtGui>
#include "myclass.h"
#include <QRegExp>
#include <QHeaderView>

using namespace std;

/*
     This is the implementation of our main widget.
*/

MyClass::MyClass(std::vector<std::string> cryptos, QWidget *parent) : QWidget(parent)
{
     this->cryptos = cryptos; // This is the coin names which are sent from main function.

     QStringList horizantalHeaderElements = {"USD","EUR","GBP"}; // These are the names of curriencies which are
                                                                 // shown at the horizantal header of our table.
     QStringList verticalHeaderElements;// This is the names of coins which will be shown at the horizontal header
                                        // our table.

     int size = 0;

     // I created a vertical header list containing as many elements as the given coin list and initialize the vertical
     // header elements to "...". After getting the names of these coins, I will update them with the names of coins.
     for(string crypto : cryptos){
          verticalHeaderElements.append("...");
          size++;
     }

     tableWidget = new QTableWidget(size, 3, this); // This is the table which contains all the elements.

     tableWidget->setHorizontalHeaderLabels(horizantalHeaderElements); // Set the horizontal header.
     tableWidget->setVerticalHeaderLabels(verticalHeaderElements); // Set the vertical header.

     QHeaderView* hHeader = tableWidget->horizontalHeader();
     hHeader->setSectionResizeMode(QHeaderView::Stretch);
     QHeaderView* vHeader = tableWidget->verticalHeader();
     vHeader->setSectionResizeMode(QHeaderView::Stretch);

     // Here, I initialized all the entries of our table with "...". After the http request that will return exchange rates,
     // they will contain the rates.
     for(int i = 0 ; i < tableWidget->rowCount() ; i++){
          for(int j = 0 ; j < tableWidget->columnCount() ; j++){
               QTableWidgetItem *item = new QTableWidgetItem();
               item->setText("...");
               tableWidget->setItem(i, j, item);
          }
     }

     QHBoxLayout *layout = new QHBoxLayout;

     layout->addWidget(tableWidget);
     setLayout(layout);

     // We have two network manager, one for getting ids of the coins, one for the getting prices of the coins.
     manager = new QNetworkAccessManager(this) ;
     idGetter = new QNetworkAccessManager(this) ;  

     connect(manager, SIGNAL(finished(QNetworkReply *)),this, SLOT(replyFinished(QNetworkReply *)));
     connect(idGetter, SIGNAL(finished(QNetworkReply *)),this, SLOT(getIds(QNetworkReply *)));

     // This is for getting the names of coins which are given in the file.
     idGetter->get(QNetworkRequest(QUrl("https://api.coingecko.com/api/v3/coins/list")));

}

/*
     This slot gets the ids of the names and then sends a request to API to get the prices.
*/
void MyClass::getIds(QNetworkReply *reply){
     int pos = 0;
     this->ids = new QVector<QString>(); 
     this->names = new QVector<QString>();

     // I read the data fetched from the web site. 
     QString data = (QString) reply->readAll();

     for(string crypto : cryptos){
          QString symbol = "";
          QString id = "";
          QString name = "";

          // There are three cases:
          // 1. The string given in the file can be the symbol of the coin,
          // 2. Or it can be the name of the coin,
          // 3. Or it can be the id of the coin.
          // I checked these three conditions respectively. In each case, by help of RegEx, I get 
          // symbol, name and id of the coin. IDs are then constitute the http address of the website
          // that we will send a request to get prices of the coins. 
          string symbolPtrn = "\\{\\\"id\\\":\\\"([^{]+)\\\",\\\"symbol\\\":\\\"("+crypto+")\\\",\\\"name\\\":\\\"([^{]+)\\\"\\}";
          QString symbolPattern = QString::fromStdString(symbolPtrn);
          QRegExp symbolrx(symbolPattern);
          if(symbolrx.indexIn(data,pos) != -1){
               id = symbolrx.cap(1);
               symbol = QString::fromStdString(crypto);
               name = symbolrx.cap(3);
          } else{
               string namePtrn = "\\{\\\"id\\\":\\\"([^{]+)\\\",\\\"symbol\\\":\\\"([^{]+)\\\",\\\"name\\\":\\\"(" + crypto + ")\\\"\\}";
               QString namePattern = QString::fromStdString(namePtrn);
               QRegExp namerx(namePattern);
               if(namerx.indexIn(data,pos) != -1){
                    id = namerx.cap(1);
                    symbol = namerx.cap(2);
                    name = QString::fromStdString(crypto);
               } else{
                    string idPtrn = "\\{\\\"id\\\":\\\"(" + crypto + ")\\\",\\\"symbol\\\":\\\"([^{]+)\\\",\\\"name\\\":\\\"([^{]+)\\\"\\}";
                    QString idPattern = QString::fromStdString(idPtrn);
                    QRegExp idrx(idPattern);
                    if(idrx.indexIn(data,pos) != -1){
                         id = QString::fromStdString(crypto);
                         symbol = idrx.cap(2);
                         name = idrx.cap(3);
                    }
               }
          }
          ids->push_back(id);
          names->push_back(name);
          
     }

     // Here, I constructed my actual vertical header after getting the names of the coins.
     // Names are shown in the vertical header.
     QStringList verticalHeaderElements;
     for(QString name : *names){
          verticalHeaderElements.append(name);
     }

     tableWidget->setVerticalHeaderLabels(verticalHeaderElements); 

     // Here, I created the part of the string of the http request which contains coin ids.
     QString requestline = ids->at(0);
     for(int i = 1 ; i < ids->size() ; i++){
          requestline = requestline + "," + ids->at(i);
     }

     // Here, I created my full url for http request to get the prices of the coins.
     QString urlpart1 = "https://api.coingecko.com/api/v3/simple/price?ids=";
     QString urlpart2 = "&vs_currencies=usd,eur,gbp";
     QString url = "";
     url.append(urlpart1);
     url.append(requestline);
     url.append(urlpart2);
     // Then sent the request.
     manager->get(QNetworkRequest(QUrl(url)));

}

/*
     This is the slot which sets all the entries of our table according to http request.
*/
void MyClass::replyFinished(QNetworkReply *reply)  {

     int pos = 0;
     // I read the data fetched from the web site. 
     QString data = (QString) reply->readAll();
     int k = 0;
     for(QString id : *ids){
          // By help of RegEx, I get all the prices of each coin.
          QString pattern = "\"" + id + "\":\\{\"usd\":(\\d\\.\\d+e[-+]\\d+|\\d+\\.\\d+|\\d+)\\,\"eur\":(\\d\\.\\d+e[-+]\\d+|\\d+\\.\\d+|\\d+),\"gbp\":(\\d\\.\\d+e[-+]\\d+|\\d+\\.\\d+|\\d+)";
          QRegExp rx(pattern);
          QString usdStr, eurStr, gbpStr;
          if (rx.indexIn(data, pos) != -1 ) {
               usdStr = rx.cap(1); // USD price
               eurStr = rx.cap(2); // EUR price
               gbpStr = rx.cap(3); // GBP price
          }
          else {
               // If the API does return something like " "12866-lauder":{} ", it means API does not return prices.
               // So, in that case instead of price, I showed "NULL" instead of price.
               QString pattern = "\"" + id + "\":\\{\\}";
               QRegExp rx(pattern);
               if (rx.indexIn(data, pos) != -1 ){
                    usdStr = "NULL"; // USD price
                    eurStr = "NULL"; // EUR price
                    gbpStr = "NULL"; // GBP price
               } else{
                    // In case of an error, string "Error" is displayed.
                    usdStr = QString("Error"); 
                    eurStr = QString("Error");
                    gbpStr = QString("Error"); 
               }
          }
          // According to fetched prices, I set the corresponding price entries of our table.
          QTableWidgetItem *usdItem = new QTableWidgetItem();
          usdItem->setText(usdStr);
          usdItem->setTextAlignment(Qt::AlignCenter);
          tableWidget->setItem(k,0,usdItem);

          QTableWidgetItem *eurItem = new QTableWidgetItem();
          eurItem->setText(eurStr);
          eurItem->setTextAlignment(Qt::AlignCenter);
          tableWidget->setItem(k,1,eurItem);

          QTableWidgetItem *gbpItem = new QTableWidgetItem();
          gbpItem->setText(gbpStr);
          gbpItem->setTextAlignment(Qt::AlignCenter);
          tableWidget->setItem(k,2,gbpItem);

          k++;
     }
}


     // "\\{\\\"id\\\":\\\"([a-z0-9\\-]+)\\\",\\\"symbol\\\":\\\"([a-zA-Z0-9\\s\\-]+)\\\",\\\"name\\\":\\\"([a-zA-Z0-9\\s\\-\\.\\(\\)/]+)\\\"\\}"



