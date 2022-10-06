/*
To get the entities with minimum "Average_Price"s,
we used IN operator to get the minimum Average_Price.
*/
SELECT *
FROM Restaurant 
WHERE Restaurant.Average_Price
IN(SELECT min(Restaurant.Average_Price) FROM Restaurant)