# Implementation of Project-2 of Fall 2021 CMPE 300 @Bogazici University
# Authors:
#           Halil Burak Pala - 2019400282
#           Berat Damar      - 2018400039
# This code is working. Can be run with the command:
# mpiexec -n [# of Processors] python3 project_2.py [input file] [output file]

from mpi4py import MPI
import sys

def splitCoordinates(coordsInWaves, rank, ratio):
	oCoordsInWavesAll = coordsInWaves[0]
    
	pCoordsInWavesAll = coordsInWaves[1]

	# Send only the coordinates that this process is responsible from
    # in waves so that it can update the map part that it is responsible 
    # from at every wave.
	boundary = range((rank-1)*ratio, rank*ratio)
	oCoordsInWavesForProcess = []
	for oCoordsInWaveAll in oCoordsInWavesAll:
		oCoordsInWaveForProcess = []
		for o_coord in oCoordsInWaveAll:
			row_coord = o_coord[0]
			if row_coord in boundary:
				oCoordsInWaveForProcess.append(o_coord)
		oCoordsInWavesForProcess.append(oCoordsInWaveForProcess)
    
	pCoordsInWavesForProcess = []
	for pCoordsInWaveAll in pCoordsInWavesAll:
		pCoordsInWaveForProcess = []
		for p_coord in pCoordsInWaveAll:
			row_coord = p_coord[0]
			if row_coord in boundary:
				pCoordsInWaveForProcess.append(p_coord)
		pCoordsInWavesForProcess.append(pCoordsInWaveForProcess)
	return [oCoordsInWavesForProcess,pCoordsInWavesForProcess]

# This function initializes the process according to packet sent
# from the root process.
def processInitialization(packet, rank):

    mapPart = packet[0]
    coordsInWaves = packet[1]

    oCoordsInWavesForProcess = coordsInWaves[0]
    pCoordsInWavesForProcess = coordsInWaves[1]
    N = len(mapPart[0])
    W = len(coordsInWaves[0])+1

    upperRow = mapPart[0]
    lowerRow = mapPart[-1]
    
    return (mapPart,oCoordsInWavesForProcess,pCoordsInWavesForProcess,N,W,upperRow,lowerRow)

# This function simulates the game.
def simulateGame(mapPart, upperNeighborRow, lowerNeighborRow):
    N = len(mapPart[0])

    # Every row of the map part is inspected.
    for i in range(len(mapPart)):
        row = mapPart[i]

        # If inspected row is the 1st row of the map part, its above row is
        # set as upper neighbor's lower row. If inspected row is the last row
        # of the map part, its below row is set as lower neighbor's upper row.
        aboveRow = mapPart[i-1] if i != 0 else upperNeighborRow
        belowRow = mapPart[i+1] if i != len(mapPart)-1 else lowerNeighborRow

        for j in range(N):
            # castle is a tuple. castle[0] is its type, castle[1] is its health

            # Relative locations of neighboring castles are as follows:
            #
            #   northWestCastle     northCastle     northEastCastle
            #     westCastle          castle           eastCastle
            #   southWestCastle     southCastle     southEastCastle
            #
            castle = row[j]
            # If there is no castle in that location, castle is nothing but a dot.
            # In that case, just skip executing following lines.
            if castle == ".":
                continue
            # Here, the missing locations for edges and corners are inspected. In
            # such cases, that location is assumed to be consisted of a dot.
            northCastle = aboveRow[j]
            southCastle = belowRow[j]
            eastCastle = row[j+1] if j != N-1 else '.'
            westCastle = row[j-1] if j != 0 else '.'
            northEastCastle = aboveRow[j+1] if j != N-1 else '.'
            southEastCastle = belowRow[j+1] if j != N-1 else '.'
            southWestCastle = belowRow[j-1] if j != 0 else '.'
            northWestCastle = aboveRow[j-1] if j != 0 else '.'

            if castle[0] == 'o':
                damage = 0
                if northCastle[0] == '+':
                    damage += 1
                if westCastle[0] == '+':
                    damage += 1
                if southCastle[0] == '+':
                    damage += 1
                if eastCastle[0] == '+':
                    damage += 1
                castle[1] -= damage * 2

            elif castle[0] == '+':
                damage = 0
                if northCastle[0] == 'o':
                    damage += 1
                if northEastCastle[0] == 'o':
                    damage += 1
                if eastCastle[0] == 'o':
                    damage += 1
                if southEastCastle[0] == 'o':
                    damage += 1
                if southCastle[0] == 'o':
                    damage += 1
                if southWestCastle[0] == 'o':
                    damage += 1
                if westCastle[0] == 'o':
                    damage += 1
                if northWestCastle[0] == 'o':
                    damage += 1
                castle[1] -= damage
            
            row[j] = castle
        mapPart[i] = row

    # After one simulation, remove all of the destroyed castles.
    for i in range(len(mapPart)):
        for j in range(N):
            if mapPart[i][j] != '.':
                if mapPart[i][j][1] <= 0:
                    mapPart[i][j] = '.'

# This function updates the map according to given coordinate
# lists for o and p castles.
def updateMapPart(mapPart, oCoords, pCoords, rank):
    ratio = len(mapPart)
    for o_coord in oCoords:
        col_coord = o_coord[0] - (rank-1)*ratio
        row_coord = o_coord[1]
        if(mapPart[col_coord][row_coord] == "."):
            mapPart[col_coord][row_coord] = ['o',6]
    for p_coord in pCoords:
        col_coord = p_coord[0] - (rank-1)*ratio
        row_coord = p_coord[1]
        if(mapPart[col_coord][row_coord] == "."):
            mapPart[col_coord][row_coord] = ['+',8]

comm = MPI.COMM_WORLD
size = comm.Get_size()
P = size-1
rank = comm.Get_rank()

if rank == 0: # MANAGER Process
    inputFile = sys.argv[1]
    outputFile = sys.argv[2]
    with open(inputFile, "r") as fd:
        lines = fd.read().splitlines()
    fd.close()

    numbers = lines[0].split()
    N = int(numbers[0]) # Size of the map
    W = int(numbers[1]) # Number of waves
    map = [['.' for i in range(N)] for j in range(N)]

    # Initialize the map according to first rows for o and p castles.
    o_line = lines[1]
    p_line = lines[2]
    o_coord_raw = o_line.split(', ')
    p_coord_raw = p_line.split(', ')
    o_coord = []
    p_coord = []
    for raw_coord in o_coord_raw:
        col_coord = int(raw_coord.split()[0])
        row_coord = int(raw_coord.split()[1])
        map[col_coord][row_coord] = ['o',6]
    for raw_coord in p_coord_raw:
        col_coord = int(raw_coord.split()[0])
        row_coord = int(raw_coord.split()[1])
        map[col_coord][row_coord] = ['+',8]

    oCoordsInWavesAll = []
    pCoordsInWavesAll = []
    # get the coordinates at each round (starting from 2nd round since
    # coordinates in the 1st round is used for initializing the map). 
    for i in range(2,W+1):
        o_line = lines[2*i-1]
        p_line = lines[2*i]
        o_coord_raw = o_line.split(', ')
        p_coord_raw = p_line.split(', ')
        o_coord = []
        p_coord = []
        for raw_coord in o_coord_raw:
            col_coord = int(raw_coord.split()[0])
            row_coord = int(raw_coord.split()[1])
            coord = (col_coord, row_coord)
            o_coord.append(coord)
        for raw_coord in p_coord_raw:
            col_coord = int(raw_coord.split()[0])
            row_coord = int(raw_coord.split()[1])
            coord = (col_coord, row_coord)
            p_coord.append(coord)
        oCoordsInWavesAll.append(o_coord)
        pCoordsInWavesAll.append(p_coord)

    # Following are sent to the processes:
    # 1. corresponding map part
    # 2. coordinates in each wave
    # They are sent as a packet.
    coordsInWaves = (oCoordsInWavesAll, pCoordsInWavesAll)
    ratio = int(N/P)
    for i in range(P):
        mapPart = map[i*ratio:(i+1)*ratio]
        coordsInWavesForProcess = splitCoordinates(coordsInWaves,i+1,ratio)
        packet = (mapPart, coordsInWavesForProcess)
        comm.send(packet, dest = i+1, tag=1)
    
    # After all processes are finished, they sent their map parts
    # to this root process one by one.
    map = []
    for i in range(1,P+1):
        mapPart = comm.recv(source = i, tag = 5 + i)
        for mapLine in mapPart:
            map.append(mapLine)
    
    # Writing to the output file
    out = open(outputFile, 'w')
    for i in range(N):
        castles = [castle[0] for castle in map[i]]
        line = ""
        for j in range(len(castles)-1):
            line += castles[j] + " "
        line += castles[N-1]
        out.write(line)
        if i != N-1:
            out.write('\n')
    out.close()

        
# You can refer to COMMUNICATION ILLUSTRATION section in the end of this file
# to see an illustration of the communication between processes.
elif rank == 1: # This is the first worker process
    packet = comm.recv(source=0, tag=1)
    (mapPart,oCoordsInWaves,pCoordsInWaves,N,W,upperRow,lowerRow) \
        = processInitialization(packet, rank)

    for wave in range(0,W):
        for round in range(8):
            # tag 2: O--> odd numbered processes send their lower row to the below 
            # processes (i.e. even processes)
            comm.send(lowerRow, dest = 2, tag=2 )

            # tag 3 communication does not occur in the 1st process since it has no
            # process to send its upper row above it

            # tag 4 communication does not occur in the 1st process since there is no
            # process which sends its lower row to the 1st process. So we will assume
            # upper neighbor row of 1st process is full of dots:
            upperNeighborRow = ['.'] * N

            # tag 5: O<--- odd numbered processes receive lower neighbor row from 
            # the below processes (i.e. even processes)
            lowerNeighborRow = comm.recv(source = rank+1, tag=5)

            # Processing the rows:
            simulateGame(mapPart, upperNeighborRow, lowerNeighborRow)

            upperRow = mapPart[0]
            lowerRow = mapPart[-1]

        if wave != W-1: # In the last wave, no need to look for next wave
            oCoords = oCoordsInWaves[wave]
            pCoords = pCoordsInWaves[wave]
            updateMapPart(mapPart, oCoords, pCoords, rank)

    comm.send(mapPart, dest = 0, tag = 5 + rank)


elif rank == P:
    packet = comm.recv(source=0, tag=1)
    (mapPart,oCoordsInWaves,pCoordsInWaves,N,W,upperRow,lowerRow) \
        = processInitialization(packet, rank)

    for wave in range(W):
        for round in range(8):
            if P % 2 == 1: # if the last process is odd ranked,

                # tag 2 communication does not occur in odd ranked last processes
                # since such processes do not have any process to send their lower
                # row below them

                # tag 3: O--> odd numbered processes send their upper row to the above 
                # processes (i.e. even processes)
                comm.send(upperRow, dest = rank-1, tag=3)

                # tag 4: O<--- odd numbered processes receive upper neighbor row from 
                # the above processes (i.e. even processes)
                upperNeighborRow = comm.recv(source = rank-1, tag=4)

                # tag 5 communication does not occur in odd ranked last processes
                # since there is no process which sends its upper row to such processes.
                # So, we will assume lower neighbor row of such processes is full of dots.
                lowerNeighborRow = ['.'] * N 

            else: # if the last process is even ranked,

                # tag 2: O<--- even numbered processes receive upper neighbor row from 
                # the above processes (i.e. odd processes)
                upperNeighborRow = comm.recv(source = rank-1, tag=2)

                # tag 3 communication does not occur in even ranked last processes since
                # there is no process below it which sends its upper row. So, we will
                # assume lower neighbor row of such processes is full of dots.
                lowerNeighborRow = ['.'] * N

                # tag 4 communication does not occur in even ranked last processes since
                # there is no process below it to send its lower row.

                # tag 5: O--> even numbered processes send their upper row to the above 
                # processes (i.e. odd processes)
                comm.send(upperRow, dest = rank-1, tag=5)
            
            # Processing the rows:
            simulateGame(mapPart, upperNeighborRow, lowerNeighborRow)
            # print(rank, wave, round, mapPart)

            upperRow = mapPart[0]
            lowerRow = mapPart[-1]

        if wave != W-1: # in the last wave, no need to look for next wave
            oCoords = oCoordsInWaves[wave]
            pCoords = pCoordsInWaves[wave]
            updateMapPart(mapPart, oCoords, pCoords, rank)

    comm.send(mapPart, dest = 0, tag = 5 + rank)
        

elif rank % 2 == 1: # ODD ranked worker processes
    packet = comm.recv(source=0, tag=1)
    (mapPart,oCoordsInWaves,pCoordsInWaves,N,W,upperRow,lowerRow) \
        = processInitialization(packet, rank)

    for wave in range(W):
        for round in range(8):
            # tag 2: O--> odd numbered processes send their lower row to the below 
            # processes (i.e. even processes)
            comm.send(lowerRow, dest = rank+1, tag=2)

            # tag 3: O--> odd numbered processes send their upper row to the above 
            # processes (i.e. even processes)
            comm.send(upperRow, dest = rank-1, tag=3)

            # tag 4: O<--- odd numbered processes receive upper neighbor row from 
            # the above processes (i.e. even processes)
            upperNeighborRow = comm.recv(source = rank-1, tag=4)

            # tag 5: O<--- odd numbered processes receive lower neighbor row from 
            # the below processes (i.e. even processes)
            lowerNeighborRow = comm.recv(source = rank+1, tag=5)
            # Processing the rows:
            simulateGame(mapPart, upperNeighborRow, lowerNeighborRow)

            upperRow = mapPart[0]
            lowerRow = mapPart[-1]
        if wave != W-1: # in the last wave, no need to look for next wave
            
            oCoords = oCoordsInWaves[wave]
            pCoords = pCoordsInWaves[wave]
            updateMapPart(mapPart, oCoords, pCoords, rank)

    comm.send(mapPart, dest = 0, tag = 5 + rank)

else: # EVEN ranked worker processes
    packet = comm.recv(source=0, tag=1)
    (mapPart,oCoordsInWaves,pCoordsInWaves,N,W,upperRow,lowerRow) \
        = processInitialization(packet, rank)

    for wave in range(W):
        for round in range(8):
            # tag 2: O<--- even numbered processes receive upper neighbor row from 
            # the above processes (i.e. odd processes)
            upperNeighborRow = comm.recv(source = rank-1, tag=2)

            # tag 3: O<--- even numbered processes receive lower neighbor row from 
            # the below processes (i.e. odd processes)
            lowerNeighborRow = comm.recv(source = rank+1, tag=3)

            # tag 4: O--> even numbered processes send their lower row to the below 
            # processes (i.e. odd processes)
            comm.send(lowerRow, dest = rank+1, tag=4)

            # tag 5: O--> even numbered processes send their upper row to the above 
            # processes (i.e. odd processes)
            comm.send(upperRow, dest = rank-1, tag=5)

            # Processing the rows:
            simulateGame(mapPart, upperNeighborRow, lowerNeighborRow)

            upperRow = mapPart[0]
            lowerRow = mapPart[-1]

        if wave != W-1: # in the last wave, no need to look for next wave
            
            oCoords = oCoordsInWaves[wave]
            pCoords = pCoordsInWaves[wave]
            updateMapPart(mapPart, oCoords, pCoords, rank)

    comm.send(mapPart, dest = 0, tag = 5 + rank)


'''
COMMUNICATION ILLUSTRATION:

    ^(3)            |
----|---------------|--------   send(2)
    |               v(4)        send(3)
        ODD RANKED              recv(4)
    |               ^(5)        recv(5)
----|---------------|--------
    v(2)            |           recv(2)
        EVEN RANKED             recv(3)
    ^(3)            |           send(4)
----|---------------|--------   send(5)
    |               v(4)

First 2 and 3 occurs, then 4 and 5 occurs.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

For the first process:

-----------------------------
                   
        RANK:1
    |               ^(5)
----|---------------|--------
    v(2)            |

1st ranked process does not send to or receive from
the upper process since there is no such process.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

For the last process, two possibilities:

*** If it is odd ranked: ***

    ^(3)            |
----|---------------|--------
    |               v(4)
   ODD RANKED LAST PROCESS

-----------------------------

*** If it is even ranked: ****

    |               ^(5)
----|---------------|--------
    v(2)            |
   EVEN RANKED LAST PROCESS
                
-----------------------------

In both cases, no data transfer from or to lower
process since there is no such process.
'''