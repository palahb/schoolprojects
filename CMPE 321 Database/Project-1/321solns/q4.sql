/* Firstly, we selected necessary fiels from Destinaton and Cuisine tables. */
SELECT Destination.Destination_Name, Cuisine.Type

/* Since we will search for minimum Average_Price, we included FROM Restaurants.*/
FROM Restaurant

/* To get Destination_Name from Destination table, we need to join this table. We
used left join for this purpose.*/
LEFT JOIN Destination ON Restaurant.Country_Region = Destination.Destination_ID

/* To get Cuisine.Type from Cuisine table, we need to join this table also. We
used left join for this purpose.*/
LEFT JOIN Cuisine ON Restaurant.Cuisine_Type = Cuisine.Cuisine_ID

/* To get necessary entries, we need to search for maximum average price. Using
the same logic with the q3.sql we can get it: */
WHERE Restaurant.Average_Price
IN(SELECT max(Restaurant.Average_Price) FROM Restaurant)