
-- Used PS3 codes in this question:
SELECT 	Gourmet.Gourmet_ID,
-- We used CASE operator to check Feasibility
CASE
	-- If there exists at least one restaurant which meets requirements
	-- of a gourmet, it returns TRUE
	WHEN EXISTS
	(SELECT *
	FROM Restaurant
	-- First requirement is Travel destination
	WHERE Gourmet.Travel_Destination = Restaurant.Country_Region 
	-- Second one is fovorite cuisine
	AND Gourmet.Favorite_Cuisine = Restaurant.Cuisine_Type
	-- Last one is average budget
	AND Gourmet.Average_Budget >= Restaurant.Average_Price)
	THEN "TRUE"
	ELSE "FALSE"
END AS Feasibility
FROM Gourmet