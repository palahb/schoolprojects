'''
CMPE 300 Fall 2021 Project-3 @Bogazici University
Halil Burak Pala - 2019400282
Berat Damar - 2018400039
Can be run with:
python3 main.py <"part1" or "part2"> 
(This is a python3 code. Can show unexpected results with lower versions.)
'''
import sys
import random

# This function upadates available columns for a given columns list.
# It checks for every column in the Columns list, whether each of these
# columns, or their left and right diagonal columns are on the available
# columns list and removes them in that case.
def updateAvailColumns(Column, n):
    # In each step, we reset the available columns. Then remove the 
    # necessary columns from this available columns list.
    availColumns = [i for i in range(n)]

    # For every column in the given columns list, we check the column
    # itself and its right and left diagonals.
    for i in range(len(Column)-1,-1,-1):
        currentColumn = Column[i]
        diff = len(Column) - i # This is for calculating diagonal columns. 
        if currentColumn in availColumns:
            availColumns.remove(currentColumn)
        if currentColumn+diff in availColumns: # Right diagonal
            availColumns.remove(currentColumn+diff) 
        if currentColumn-diff in availColumns: # Left diagonal
            availColumns.remove(currentColumn-diff)  
    return availColumns

# This function is for part 1. It runs given QueensLasVegas algorithm
# for given board size. It prints available columns and where each 
# queen is placed for every step in the algorithm to the given output
# stream. Returns columns to which each n or less than n queens are placed.
def QueensLasVegasPart1(n, out):
    availColumns = [i for i in range(n)]
    Column = []
    R = 0
    while len(availColumns) > 0 and R <= n-1:
        C = random.choice(availColumns)
        Column.append(C)
        R += 1
        availColumns = updateAvailColumns(Column, n)
        out.write("Step " + str(R) + ": Columns: " + str(Column) + "\n")
        out.write("Step " + str(R) + ": Available: " + str(availColumns) + "\n")
    return Column

# This function is for the part 2. It runs given QueensLasVegas algorithm
# until a specified number of columns is obtained or if this number of 
# columns is not achieved, it runs until there is no available column 
# for queens. Returns columns to which each k or less than k queens are placed.
def QueensLasVegasPart2(n, k):
    availColumns = [i for i in range(n)]
    Column = []
    R = 0
    while len(availColumns) > 0 and R <= k-1:
        C = random.choice(availColumns)
        Column.append(C)
        R += 1
        availColumns = updateAvailColumns(Column, n)
    return Column

# This function simulates the part 1 of the project. It runs 10000
# QueensLasVegas algorithm 10000 times for the given board size and
# prints the result as successful if it can succesffuly place the
# queens, it prints unsuccessful otherwise.
def part1Simulation(n, out): 
    success = 0
    for i in range(10000):
        Column = QueensLasVegasPart1(n, out)
        if len(Column) == n:
            out.write("Successful\n\n")
            success += 1
        else:
            out.write("Unsuccessful\n\n")
    out.close()
    return success
#This function checks that current placement of the queens is correct.
# In this function, we use back tracking using recursive function call.
#If current placement of the queens is correct, it returns true.
def isSafe(board, row, col):
    # Check this row on left side
    for i in range(col):
        if board[row][i] == 1:
            return False

        # Check upper diagonal on left side
    for i, j in zip(range(row, -1, -1), range(col, -1, -1)):
        if board[i][j] == 1:
            return False

        # Check lower diagonal on left side
    for i, j in zip(range(row, N, 1), range(col, -1, -1)):
        if board[i][j] == 1:
            return False

    return True
#This method is used to place queens at available rows after placing k rows.
# Try any  board[row][column] and call isSafe method to check correctness of the placement.
#I continues to try until finding available position for a queen in recursive manner.
#In this method, we benefit from internet for the logic of back tracking.
def solver(board, col):
	# If all queens are placed
	# then return true
	if col >= N:
		return True

	# Consider this column and try placing
	# this queen in other n-k rows one by one
	for i in range(N):

		if isSafe(board, i, col):
			# Place this queen
			board[i][col] = 1

			# call again to place rest of the queens
			if solver(board, col + 1) == True:
				return True

			# If placing queen in board[i][col
			# doesn't lead to a solution, then
			# queen from board[i][col]
			board[i][col] = 0

	# if the queen can not be placed in any row in
	# then return false
	return False
# This function checks correctness of placement of all queens at the end .
def checkSuccess(Column):
    n = len(Column)
    for i in range(n-1):
        for j in range(i+1,n):
            d = j-i
            # checking left part
            if Column[i] - d >= 0:
                if Column[j] == Column[i]-d:
                    return False
            # checking right part
            if Column[i] + d < n:
                if Column[j] == Column[i]+d:
                    return False
    return True
#This function take transpose of given matrix.
def matrixTranspose(anArray):
    transposed = [None]*len(anArray[0])
    for t in range(len(anArray)):
        transposed[t] = [None]*len(anArray)
        for tt in range(len(anArray[t])):
            transposed[t][tt] = anArray[tt][t]
    return transposed
#Firstly,This function call Las Vegas Algorithm until placing k queens to first k rows.
#After that, it converts to column array to 2D matrix representation of given queens placement.
#It takes transpose of that matrix to able to call  solver method and then we call solver method.
#Solver method try to find a solution deterministic way.
#If we can find a solution this function returns true.
def find_result(k):
    column_array = QueensLasVegasPart2(N, k)
    new_column = []
    table = [[0 for x in range(N)] for y in range(N)]
    #print(column_array)
    while(len(column_array) < k):
        column_array = QueensLasVegasPart2(N, k)
    for i in range(k):
        table[i][column_array[i]] = 1
    #printSolution(table)
    #print("********")
    transpose_table = matrixTranspose(table)

    if solver(transpose_table, k) == True:
        for k in range(N):
            for l in range(N):
                if(transpose_table[k][l] == 1):
                    new_column.append(l)
        if(checkSuccess(new_column)):
            #printSolution(matrixTranspose(transpose_table))
            #print("------------------------")
            return True
            
#This function call find_result function for given k value.
#It returns number of successes for 10000 calls.
def try_10000times_for_k(k):
    count = 0
    for i in range(10000):
        if (find_result(k)):
            count = count + 1
    return count

part = sys.argv[1]

if part == 'part1': # This is the implementation of the part 1.
    out6 = open("results_6.txt", "w")
    out8 = open("results_8.txt", "w")
    out10 = open("results_10.txt", "w")

    result6 = part1Simulation(6, out6)
    result8 = part1Simulation(8, out8)
    result10 = part1Simulation(10, out10)

    print("LasVegas Algorithm With n = 6")
    print("Number of successful placements is " + str(result6))
    print("Number of trials is 10000")
    print("Probability that it will come to a solution is " + str(result6/10000))
    print("")
    print("LasVegas Algorithm With n = 8")
    print("Number of successful placements is " + str(result8))
    print("Number of trials is 10000")
    print("Probability that it will come to a solution is " + str(result8/10000))
    print("")
    print("LasVegas Algorithm With n = 10")
    print("Number of successful placements is " + str(result10))
    print("Number of trials is 10000")
    print("Probability that it will come to a solution is " + str(result10/10000))


elif part == 'part2': # This is the implementation of the part 2.
    n_values = [6, 8, 10]
    for i in range(3):
        N = n_values[i]
        print("---------------", N, "---------------")
        for j in range(N):
            number_of_placements = try_10000times_for_k(j)
            print("k is ", j)
            print("Number of successful placements is", number_of_placements)
            print("Number of trials is 10000")
            print("Probability that it will come to a solution", number_of_placements / 10000)
