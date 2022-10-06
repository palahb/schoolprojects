/* We selected necessary fields from necessary tables.*/
SELECT Restaurant.Name, Restaurant.Chef, Destination.Destination_Name

/* What we need is the cross product of Restaurant, Row of Gourmet 1 of Gourmet table
and Destination table. So we LEFT JOINed them: */
FROM Restaurant
LEFT JOIN Gourmet ON (Gourmet.Gourmet_ID=1)
LEFT JOIN Destination ON (Restaurant.Country_Region = Destination.Destination_ID)

/* Since there is only one row in the Gourmet table which is of Gourmet with ID 1 in 
this query, we can use just the table fields. 
The conditions given in the description are as follows:*/
WHERE Restaurant.Country_Region = Gourmet.Travel_Destination AND
Restaurant.Average_Price <= Gourmet.Average_Budget AND
Restaurant.Cuisine_Type = Gourmet.Favorite_Cuisine 