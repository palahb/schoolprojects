#!/usr/bin/python


from os import X_OK
import sys

# 64 KB byte-addressable memory. Every entry is 1 byte.
memory = ["00"]*65536  

# 1-bit flags, initially all of them set to zero (False).
ZF = False
CF = False
SF = False

# 2-byte registers PC, A, B, C, D, E, SP.
registers = ["0000", #PC
			 "0000", #A
			 "0000", #B
			 "0000", #C
			 "0000", #D
			 "0000", #E
			 "FFFF"] #SP

# The function which takes two strings as parameters. 
# First one is the hexadecimal value of operand in the instruction,
# where the second is the 2-bit addressing mode in the instruction.
# The function returns the actual hexadecimal value that
# the operand points to. 
def getoperand(operand, addressingmode):
	if addressingmode == "00":
		result = operand
	elif addressingmode == "01":
		result = registers[int(operand,16)]
	elif addressingmode == "10":
		firstbyte = memory[int(registers[int(operand,16)],16)]
		secondbyte = memory[int(registers[int(operand,16)],16)+1]
		result = firstbyte + secondbyte
	elif addressingmode == "11":
		firstbyte = memory[int(operand,16)]
		secondbyte = memory[int(operand,16)+1]
		result = firstbyte + secondbyte
	return result

# The function that takes the operand, addressing mode and flags. 
# We first calculate the actual value that the operand  implies.
# Then the the value on the accumulator is prepared and both values are converted to int.
# After the addition of these integers, the result is set as binary by format function.
# Finally, the flags are set based on the binary result. 
# The function returns hexadecimal result and the flags. 
def ADD(operand, addressingmode, CF, ZF, SF):
	hexaddend1 = getoperand(operand, addressingmode)
	hexaddend2 = registers[1]
	binaryresult = format(int(hexaddend1,16) + int(hexaddend2,16),"016b")
	if len(binaryresult) == 16:
		CF = False
	elif len(binaryresult) == 17:
		CF = True
		binaryresult = binaryresult[1:]
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	hexresult = format(int(binaryresult,2),"04x").upper()
	return (hexresult,CF, ZF, SF)

# This function uses other functions to subtract operand from A.
# Since we know that the operand is unsigned, we don't need sign bit for it.
# First we use NOT and INC functions to convert the value of the operand to 
# its negative form. But these functions do not change flags.
def SUB(operand,addressingmode,CF,ZF,SF):
	(temp1,_,_) = NOT(operand, addressingmode, ZF, SF)
	(temp2,_, _, _) = INC(temp1, "00", CF, ZF, SF)
	return ADD(temp2, "00", CF, ZF, SF)

# The function that takes the operand, addressing mode and flags. 
# We first calculate the actual value that the operand  implies.
# We add 1 to this value, the result is set as binary by format function.
# Finally, the flags are set based on the binary result. 
# The function returns hexadecimal result and the flags. 
def INC(operand, addressingmode, CF, ZF, SF):
	hexaddend = getoperand(operand, addressingmode)
	binaryresult = format(int(hexaddend,16) + 1,"016b")
	if len(binaryresult) == 16:
		CF = False
	elif len(binaryresult) == 17:
		CF = True
		binaryresult = binaryresult[1:]
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	hexresult = format(int(binaryresult,2),"04x").upper() 
	return (hexresult,CF,ZF,SF)

# Very similar to the INC function, the only difference is that
# 65535 is added instead of 1.
def DEC(operand, addressingmode, CF, ZF, SF):
	hexaddend = getoperand(operand, addressingmode)
	#addition operation:
	binaryresult = format(int(hexaddend,16) + 65535,"016b")
	#set flags:
	if len(binaryresult) == 16:
		CF = False
	elif len(binaryresult) == 17:
		CF = True
		binaryresult = binaryresult[1:]
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	hexresult = format(int(binaryresult,2),"04x").upper() 
	return (hexresult,CF,ZF,SF)

# This function finds the bitwise NOT of the value of operand.
# Then it sets the flags accordingly.
def NOT(operand,addressingmode,ZF,SF):
	# get operand
	hexoperand = getoperand(operand, addressingmode)
	binaryoperand = list(format(int(hexoperand,16),"016b"))
	for i in range(16):
		if binaryoperand[i] == "0":
			binaryoperand[i] = "1"
		else:
			binaryoperand[i] = "0"
	binaryresult = ''.join(binaryoperand)
	hexresult = format(int(binaryresult,2),"04x")
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	return (hexresult,ZF,SF)

# This function finds the bitwise XOR of the operand and A.
# Then it sets the flags accordingly.
def XOR(operand,addressingmode,ZF,SF):
	# get operand
	hexoperand = getoperand(operand, addressingmode)
	binaryoperand = list(format(int(hexoperand,16),"016b"))
	binarycontentofA = list(format(int(registers[1],16),"016b"))
	for i in range(16):
		if binaryoperand[i] == binarycontentofA[i]:
			binaryoperand[i] = "0"
		else:
			binaryoperand[i] = "1"
	binaryresult = ''.join(binaryoperand)
	hexresult = format(int(binaryresult,2),"04x")
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	return (hexresult,ZF,SF)

# This function finds the bitwise AND of the operand and A.
# Then it sets the flags accordingly.
def AND(operand,addressingmode,ZF,SF):
	# get operand
	hexoperand = getoperand(operand, addressingmode)
	binaryoperand = list(format(int(hexoperand,16),"016b"))
	binarycontentofA = list(format(int(registers[1],16),"016b"))
	for i in range(16):
		if binaryoperand[i] == "1" and binarycontentofA[i] == "1":
			binaryoperand[i] = "1"
		else:
			binaryoperand[i] = "0"
	binaryresult = ''.join(binaryoperand)
	hexresult = format(int(binaryresult,2),"04x")
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	return (hexresult,ZF,SF)

# This function finds the bitwise OR of the operand and A.
# Then it sets the flags accordingly.
def OR(operand,addressingmode,ZF,SF):
	# get operand
	hexoperand = getoperand(operand, addressingmode)
	binaryoperand = list(format(int(hexoperand,16),"016b"))
	binarycontentofA = list(format(int(registers[1],16),"016b"))
	for i in range(16):
		if binaryoperand[i] == "0" and binarycontentofA[i] == "0":
			binaryoperand[i] = "0"
		else:
			binaryoperand[i] = "1"
	binaryresult = ''.join(binaryoperand)
	hexresult = format(int(binaryresult,2),"04x")
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	return (hexresult,ZF,SF)

# This function gets the binary value of the operand at first.
# Then it shifts all bits one to the left.
# The flags are set accordingly.
def SHL(operand, addressingmode, CF, ZF, SF):
	hexoperand = getoperand(operand, addressingmode)
	binaryoperand = format(int(hexoperand,16),"016b")
	binaryresult = (binaryoperand + "0")[1:]
	hexresult = format(int(binaryresult,2),"04x")
	if binaryoperand[0] == "1":
		CF = True
	else:
		CF = False
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	return (hexresult,CF,ZF,SF)

# This function gets the binary value of the operand at first.
# Then it shifts all bits one to the right.
# The flags are set accordingly.
def SHR(operand, addressingmode, ZF, SF):
	hexoperand = getoperand(operand, addressingmode)
	binaryoperand = format(int(hexoperand,16),"016b")
	binaryresult = ("0" + binaryoperand)[:-1]
	hexresult = format(int(binaryresult,2),"04x")
	if binaryresult[0] == "1":
		SF = True
	else:
		SF = False
	if int(binaryresult,2) == 0:
		ZF = True
	else:
		ZF = False
	return (hexresult,ZF,SF)

# This is the main function. All process is done here.
if __name__ == "__main__":
	
	# input file is read
	inputfile = open(sys.argv[1],"r")
	filename = sys.argv[1][:-4]
	outputfile = open(filename+".txt", "w")
	lines = []
	
	# for each line in input file, lines variable is updated
	for line in inputfile:
		line = line.strip()
		tokens = line.split()
		lines.append(tokens)
	
	# load instructions into the memory:
	temp_pc = 0
	for line in lines:
		instruction = line[0]
		# notice that each instruction is 3B whereas each memory location can store 1B
		firstbyte = instruction[0:2]
		secondbyte = instruction[2:4]
		thirdbyte = instruction[4:6]
		memory[temp_pc] = firstbyte
		memory[temp_pc+1] = secondbyte
		memory[temp_pc+2] = thirdbyte
		temp_pc += 3
	
	# instruction fetch and decode:
	while True:
		current_pc = int(registers[0],16)
		firstbyte = memory[current_pc]
		firstbinary = format(int(firstbyte,16),"08b") 
		
		# '00', '01', '02', ... , '0B', '0C', ... , or '1C'
		opcode = format(int(firstbinary[0:6],2),"02x") 
		
		# '00','01', '10' or '11'
		addressingmode = firstbinary[6:8]
		secondbyte = memory[current_pc+1]
		thirdbyte = memory[current_pc+2]
		operand = secondbyte + thirdbyte
		
		# At the execution stage, we frequently used functions that are defined above,
		# and then write them to the appropriate addres according to the addressing mode.
		
		# At jump instructions, we utilized 2 sources:
		# First:
		# https://piazza.com/class/km84om1nme367e?cid=149
		# "The *Hypothetical CPU in the project*    is  NOT   8086 -   so do not  confuse  the two."
		# 
		# Second:
		# https://www.philadelphia.edu.jo/academics/qhamarsheh/uploads/Lecture%2018%20Conditional%20Jumps%20Instructions.pdf
		# 
		# So we interpreted these as JA in cmpe230 is logically equivalent to the JG in 8086.
		# Then, the conditions at jump instructions are implemented accordingly.
		
		# execution:
		
		# if "HALT" instruction is ommitted, gives error
		if opcode == "01": # HALT
			current_pc += 3
			registers[0] = format(current_pc,"04x")
			
			# breaks the main "while true" loop
			break
		
		elif opcode == "02": # LOAD
			if addressingmode == "00":
				registers[1] = operand # operand = 2 byte
			elif addressingmode == "01":
				registers[1] = registers[int(operand,16)]
			elif addressingmode == "10":
				memfirstbyte = memory[int(registers[int(operand,16)],16)]
				memsecondbyte = memory[int(registers[int(operand,16)],16)+1]
				registers[1] = memfirstbyte + memsecondbyte
			elif addressingmode == "11":
				registers[1] = memory[int(operand,16)] + memory[int(operand,16)+1]
		
		elif opcode == "03": #STORE
			if addressingmode == "01":
				registers[int(operand,16)] = registers[1]
			elif addressingmode == "10":
				memory[int(registers[int(operand,16)],16)] = registers[1][0:2]
				memory[int(registers[int(operand,16)],16)+1] = registers[1][2:4]
			elif addressingmode == "11":
				memory[int(operand,16)] = registers[1][0:2]
				memory[int(operand,16)+1] = registers[1][2:4]
		
		elif opcode == "04": #ADD
			(hexresult,CF, ZF, SF) = ADD(operand, addressingmode, CF, ZF, SF)
			registers[1] = hexresult
		
		elif opcode == "05": #SUB
			(hexresult,CF,ZF,SF) = SUB(operand,addressingmode,CF,ZF,SF)
			registers[1] = hexresult
		
		elif opcode == "06": #INC
			(hexresult,CF, ZF, SF) = INC(operand, addressingmode, CF, ZF, SF)
			if addressingmode == "01":
				registers[int(operand,16)] = hexresult
			elif addressingmode == "10":
				memory[int(registers[int(operand,16)],16)] = hexresult[0:2]
				memory[int(registers[int(operand,16)],16)+1] = hexresult[2:4]
			elif addressingmode == "11":
				memory[int(operand,16)] = hexresult[0:2]
				memory[int(operand,16)+1] = hexresult[2:4]
		
		elif opcode == "07": #DEC
			(hexresult,CF, ZF, SF) = DEC(operand, addressingmode, CF, ZF, SF)
			if addressingmode == "01":
				registers[int(operand,16)] = hexresult
			elif addressingmode == "10":
				memory[int(registers[int(operand,16)],16)] = hexresult[0:2]
				memory[int(registers[int(operand,16)],16)+1] = hexresult[2:4]
			elif addressingmode == "11":
				memory[int(operand,16)] = hexresult[0:2]
				memory[int(operand,16)+1] = hexresult[2:4]
		
		elif opcode == "08":  #XOR
			(hexresult,ZF,SF) = XOR(operand, addressingmode, ZF, SF)
			registers[1] = hexresult
		
		elif opcode == "09":  #AND
			(hexresult,ZF,SF) = AND(operand, addressingmode, ZF, SF)
			registers[1] = hexresult
		
		elif opcode == "0a":  #OR
			(hexresult,ZF,SF) = OR(operand, addressingmode, ZF, SF)
			registers[1] = hexresult
		
		elif opcode == "0b":  #NOT
			(hexresult,ZF,SF) = NOT(operand, addressingmode, ZF, SF)
			if addressingmode == "01":
				registers[int(operand,16)] = hexresult
			elif addressingmode == "10":
				memory[int(registers[int(operand,16)],16)] = hexresult[0:2]
				memory[int(registers[int(operand,16)],16)+1] = hexresult[2:4]
			elif addressingmode == "11":
				memory[int(operand,16)] = hexresult[0:2]
				memory[int(operand,16)+1] = hexresult[2:4]
		
		elif opcode == "0c": #SHL
			(hexresult,CF,ZF,SF) = SHL(operand, addressingmode, CF, ZF, SF)
			if addressingmode == "01":
				registers[int(operand,16)] = hexresult
			elif addressingmode == "10":
				memory[int(registers[int(operand,16)],16)] = hexresult[0:2]
				memory[int(registers[int(operand,16)],16)+1] = hexresult[2:4]
			elif addressingmode == "11":
				memory[int(operand,16)] = hexresult[0:2]
				memory[int(operand,16)+1] = hexresult[2:4]
		
		elif opcode == "0d": #SHR
			(hexresult,ZF,SF) = SHR(operand, addressingmode, ZF, SF)
			if addressingmode == "01":
				registers[int(operand,16)] = hexresult
			elif addressingmode == "10":
				memory[int(registers[int(operand,16)],16)] = hexresult[0:2]
				memory[int(registers[int(operand,16)],16)+1] = hexresult[2:4]
			elif addressingmode == "11":
				memory[int(operand,16)] = hexresult[0:2]
				memory[int(operand,16)+1] = hexresult[2:4]
		
		elif opcode == "0e": #NOP
			pass
		
		elif opcode == "0f": #PUSH
			hexresult = getoperand(operand,addressingmode)
			memory[int(registers[6],16)-1] = hexresult[0:2]
			memory[int(registers[6],16)] = hexresult[2:4]
			registers[6] = format(int(registers[6],16)-2, "04x")
		
		elif opcode == "10": #POP
			first_byte = memory[int(registers[6],16)+1]
			secnd_byte = memory[int(registers[6],16)+2]
			hexresult = first_byte + secnd_byte
			if addressingmode == "01":
				registers[int(operand,16)] = hexresult
			elif addressingmode == "10":
				memory[int(registers[int(operand,16)],16)] = hexresult[0:2]
				memory[int(registers[int(operand,16)],16)+1] = hexresult[2:4]
			elif addressingmode == "11":
				memory[int(operand,16)] = hexresult[0:2]
				memory[int(operand,16)+1] = hexresult[2:4]
			registers[6] = format(int(registers[6],16)+2,"04x")
		
		elif opcode == "11": #CMP
			(hexresult,CF,ZF,SF) = SUB(operand,addressingmode,CF,ZF,SF)
			
		
		elif opcode == "12": #JMP
			registers[0] = getoperand(operand,addressingmode)
			continue
		
		elif opcode == "13": #JZ - JE
			if ZF:
				registers[0] = getoperand(operand,addressingmode)
				continue
			else:
				pass
		
		elif opcode == "14": #JNZ - JNE
			if not ZF:
				registers[0] = getoperand(operand,addressingmode)
				continue
			else:
				pass
		
		elif opcode == "15": #JC
			if CF:
				registers[0] = getoperand(operand,addressingmode)
				continue
			else:
				pass
		
		elif opcode == "16": #JNC
			if not CF:
				registers[0] = getoperand(operand,addressingmode)
				continue
			else:
				pass
		
		elif opcode == "17": #JA
			if (not SF) and (not ZF):
				registers[0] = getoperand(operand,addressingmode)
				continue
			else:
				pass
		
		elif opcode == "18": #JAE
			if not SF:
				registers[0] = getoperand(operand,addressingmode)
				continue
			else:
				pass
		
		elif opcode == "19": #JB
			if SF:
				registers[0] = getoperand(operand,addressingmode)
				continue
			else:
				pass
		
		elif opcode == "1a": #JBE
			if SF or ZF:
				registers[0] = getoperand(operand,addressingmode)
				continue
			else:
				pass
		
		elif opcode == "1b": #READ 
			try :
				rawstr = input("enter:")
			except TypeError:
				print("exception")
			hexresult = format(ord(rawstr[0]),"04x")
			if addressingmode == "00":
				# error
				print("error2")
			elif addressingmode == "01":
				registers[int(operand,16)] = hexresult
			elif addressingmode == "10":
				memory[int(registers[int(operand,16)],16)] = hexresult[0:2]
				memory[int(registers[int(operand,16)],16)+1] = hexresult[2:4]
			elif addressingmode == "11":
				memory[int(operand,16)] = hexresult[0:2]
				memory[int(operand,16)+1] = hexresult[2:4]
			
		elif opcode == "1c": #PRINT
			outputfile.write(chr(int(getoperand(operand,addressingmode),16)))
			outputfile.write("\n")
		
		current_pc += 3
		registers[0] = format(current_pc,"04x")



