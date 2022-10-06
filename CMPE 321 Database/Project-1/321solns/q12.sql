-- The keyword satisfies the conditions specified in the question description is
-- LEFT JOIN. So we used it.
SELECT *
FROM Restaurant
LEFT JOIN Gourmet ON Restaurant.Restaurant_ID = Gourmet.Favorite_Restaurant