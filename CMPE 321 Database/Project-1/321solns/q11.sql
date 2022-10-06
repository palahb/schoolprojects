-- We selected necessary fields from necessary tables
SELECT DISTINCT Restaurant.Name, Restaurant.Chef, Destination.Destination_Name

FROM Restaurant
-- We should innner join Gourmet so that only restaurants appearing in Favorite_Restaurant
-- field are in the table.
INNER JOIN Gourmet ON Restaurant.Restaurant_ID = Gourmet.Favorite_Restaurant
INNER JOIN Destination ON Restaurant.Country_Region = Destination.Destination_ID