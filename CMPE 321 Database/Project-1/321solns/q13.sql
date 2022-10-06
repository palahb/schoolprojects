-- We selected necessary fields from necessary tables.
SELECT Cuisine.Cuisine_ID, Cuisine.Type, 

-- Restaurant count can be fonud by count function:
count(Restaurant.Restaurant_ID) AS Restaurant_Count, 

-- Distinct Country_Region count can be fonud b DISTINCT operator and count function:
count(DISTINCT Country_Region) AS Distinct_Country_Region_Count

/* What we need is Cuisine and Restaurant tables togather. We used LEFT
JOIN to get them:*/
FROM Cuisine
LEFT JOIN Restaurant ON Restaurant.Cuisine_Type=Cuisine.Cuisine_ID

-- To get cuisines by their restaurant counts and distinct country region counts,
-- we used GROUP BY operator
GROUP BY Cuisine.Cuisine_ID

-- To sort by type, we used ORDER BY
ORDER BY Cuisine.Type