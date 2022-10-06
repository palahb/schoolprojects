SELECT Destination.Destination_Name, 

-- By count function, we get number of gourmets
count(Gourmet.Gourmet_ID) AS Gourmet_Count

/* What we need is Destination and Gourmet tables togather. We used INNER
JOIN to get them:*/
FROM Destination
INNER JOIN Gourmet ON Gourmet.Travel_Destination = Destination.Destination_ID

-- And we need to Group them by destination names
GROUP BY Destination.Destination_Name

-- In every destination, there should be more than 1 gourmet.
HAVING Gourmet_Count > 1
