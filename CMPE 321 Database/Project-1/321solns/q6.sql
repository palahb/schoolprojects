/* We selected necessary fields from necessary tables.*/
SELECT Restaurant.Country_Region, Destination.Destination_Name,

/* To get count of restaurants, we used the same approach with q1.sql:*/ 
count(Restaurant.Restaurant_ID) AS Restaurant_Count

/* What we need is Restaurant and Destination tables togather. We used LEFT
JOIN to get them:*/
FROM Restaurant
LEFT JOIN Destination ON Restaurant.Country_Region = Destination.Destination_ID

/* To group Resaturants by their country_regions, we used GROUP BY operator.*/
GROUP BY Restaurant.Country_Region

/* To sort the results by Restaurant_Count in descending order, we used ORDER BY and
DESC operators:*/
ORDER BY Restaurant_Count DESC