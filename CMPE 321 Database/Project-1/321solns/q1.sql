/*
We used "count" function to get number of restaurants. Since
Restaurant_ID is unique for every restaurant, counting it gives
the number of restaurants.
*/
SELECT count(Restaurant.Restaurant_ID) AS Restaurant_Count
FROM Restaurant