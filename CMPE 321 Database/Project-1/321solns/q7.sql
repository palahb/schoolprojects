/* We selected necessary fields from necessary tables.*/
SELECT Restaurant.Country_Region, Destination.Destination_Name, 

/* To get count of distinct cuisine types, we used DISTINCT operator and count function:*/ 
count(DISTINCT Restaurant.Cuisine_Type) AS Cuisine_Count

/* What we need is Restaurant and Destination tables togather. We used LEFT
JOIN to get them:*/
FROM Restaurant
LEFT JOIN Destination ON (Restaurant.Country_Region = Destination.Destination_ID)

/* To group Resaturants by their country_regions, we used GROUP BY operator.*/
GROUP BY Restaurant.Country_Region

/* To sort the results by Cuisine_Count in ascending order, we used ORDER BY and
ASC operators:*/
ORDER BY Cuisine_Count ASC