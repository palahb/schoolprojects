/*
To display Awarded_Since in YYYY fromat, we discovered "strftime" function.
strftime('%Y', <date>) gives us the year of the <date>. By adding "AS Awarded_Since"
at the end of it, we ensured displaying years under the name of "Awarded_Since".
Other parts are straightforward.
Source: https://www.sqltutorial.org/sql-date-functions/how-to-extract-year-from-date-in-sql/
*/

SELECT Restaurant.Name, Restaurant.Chef, strftime('%Y', Restaurant.Awarded_Since) AS Awarded_Since 
FROM Restaurant
WHERE Awarded_Since < "2000"
ORDER BY Awarded_Since ASC