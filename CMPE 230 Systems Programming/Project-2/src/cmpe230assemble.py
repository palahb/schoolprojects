#!/usr/bin/python

'''
    Implementation of the assembler part of the 2nd Project of CMPE230 in Spring 2021
    authors:
    - Halil Burak Pala
    - Huseyin Turker Erdem
'''

import sys

def is_hex(s):
    '''
        This function checks whether the given string obeys
        the format of a hexadecimal number with at most four digit.
    ''' 
    if(len(s) <= 4):
        try: 
            int(s,16)
            return True
        except ValueError:
            return False
    elif is_hex(s[-4:]) and int(s[:-4],16) == 0:
        return True
    else:
        return False
    

def convert(binarycode):
    '''
        This function converts a binary code given in the format "opcode addr_mode operand"
        into a hexadecimal code that we want. e.g:
        1c 1 3 ---> 710003
        2 0 41 ---> 080041
    '''
    result = []
    for code in binarycode:

        opcode   = int(code[0],16) 
        addrmode = int(code[1],16)
        operand  = int(code[2],16)

        bopcode = format(opcode, '06b') 
        baddrmode = format(addrmode, '02b') 
        boperand = format(operand, '016b') 
        bin = '0b' + bopcode + baddrmode + boperand 
        ibin = int(bin[2:],2) 
        instr = format(ibin, '06x')
        result.append(instr)
    return result

'''
Addressing mode codes are as follows:
00 : operand is immediate data. ==> e.g. "LOAD 5", "LOAD 'A'"
01 : operand is in given in the register. ==> e.g. "LOAD C"
10 : operand's memory address is given in the register. ==> e.g. "LOAD [A]"
11 : operand is a memory address. ==> e.g. "LOAD [100]"
'''
def operandcheck(operand, labels, immediate=False, memory=False, register=False):
    '''
    This method extracts the operand according to corresponding addressing mode.
    '''
    addressingmode = "x" 
    operandcode = "x"
    if ((operand[0] == '\'' and operand[-1] == '\'') or (operand[0] == '\"' and operand[-1] == '\"')) and immediate:
        addressingmode = "0"
        operandcode = format(ord(operand[1:-1]), 'x')
    elif operand.lower() in labels and immediate:
        addressingmode = "0"
        operandcode = labels[operand.lower()]
    elif operand.lower() == 'pc' and register:
        addressingmode = "1"
        operandcode = "0"
    elif operand.lower() == 'a' and register:
        addressingmode = "1"
        operandcode = "1"            
    elif operand.lower() == 'b' and register:
        addressingmode = "1"
        operandcode = "2"
    elif operand.lower() == 'c' and register:
        addressingmode = "1"
        operandcode = "3"
    elif operand.lower() == 'd' and register:
        addressingmode = "1"
        operandcode = "4"
    elif operand.lower() == 'e' and register:
        addressingmode = "1"
        operandcode = "5"
    elif operand.lower() == 's' and register:
        addressingmode = "1"
        operandcode = "6"
    elif is_hex(operand) and immediate:
        addressingmode = "0"
        operandcode = operand
    elif (operand[0] == '[' and operand[-1] == ']') and memory:
        if operand[1:-1].lower() == 'pc':
            addressingmode = "2"
            operandcode = "0"
        elif operand[1:-1].lower() == 'a':
            addressingmode = "2"
            operandcode = "1"            
        elif operand[1:-1].lower() == 'b':
            addressingmode = "2"
            operandcode = "2"
        elif operand[1:-1].lower() == 'c':
            addressingmode = "2"
            operandcode = "3"
        elif operand[1:-1].lower() == 'd':
            addressingmode = "2"
            operandcode = "4"
        elif operand[1:-1].lower() == 'e':
            addressingmode = "2"
            operandcode = "5"
        elif operand[1:-1].lower() == 's':
            addressingmode = "2"
            operandcode = "6"
        elif is_hex(operand[1:-1]):
            addressingmode = "3"
            operandcode = operand[1:-1]
    return (addressingmode,operandcode)

if __name__ == "__main__":
    input = open(sys.argv[1],"r")
    filename = sys.argv[1][:-4]
    lines = []
    for line in input:
        # Take the input from .asm file line by line.
        line = line.strip()
        tokens = line.split()
        if len(tokens) != 0:
            lines.append(tokens)

    labels = {} # This dictionary is for labels. Each key-value pair is in the format "label" : "address"
    instructions = [] # This list is for holding all the lines except labels in our .asm file.
    i=0
    noflabels = 0

    for line in lines:
        # This for line is for detecting and saving labels in 'labels' dictionary.
        if len(line) == 1 and line[0][-1] == ":":
            # If line is made up of one word and #this word's last character is ':', then it means
            # this line is a label. We save this label as key and its corresponding address as value 
            # in dictionary.   
            labels[line[0][0:-1].lower()] = format((i - noflabels)*3, 'x')
            noflabels += 1
        else:
            # If the line is not in the format of a label, the we save it to our instructions list.
            instructions.append(line)
        i += 1
        # At the end of previous if-else block, we eliminated all the labels in our code file
        # and saved all of the labels and their corresponding address in memory.

    binarycode = [] # This list is the raw binary code. e.g. LOAD 'A' is saved as '2 0 41' in this list
    invalidSyntax = False # This flag is for syntax check.
    for i in range(len(instructions)):
        instr = instructions[i]
        opcode = "x" 
        addressingmode = "x"
        operandcode = "x"
        # These were for error checking. If these are still 'x' at the end of the process, then it means something went wrong.
        binaryline = ""
        mnemonic = instr[0].lower()

        # Our instructions can be one word long or two word long. If a line is one word long
        # then it can be either "HALT" or "NOP". We checked these conditions and set the parts of
        # our binary code line accordingly.
        if len(instr) == 1 and mnemonic == "halt":
            opcode = "1"
            addressingmode = "0"
            operandcode = "0"
        elif len(instr) == 1 and mnemonic == "nop":
            opcode = "e"
            addressingmode = "0"
            operandcode = "0"
        # If our instruction is two word long, then it has several options. Every case has some properties.
        # We checked every case seperately. Their possible addressing modes are also set.
        elif len(instr) == 2:
            immediate = False
            memory = False
            register = False
            if mnemonic == "load":
                opcode = "2"
                immediate = True
                memory = True
                register = True
            elif mnemonic == "store":
                opcode = "3"
                memory = True
                register = True
            elif mnemonic == "add":
                opcode = "4"
                immediate = True
                memory = True
                register = True
            elif mnemonic == "sub":
                opcode = "5"
                immediate = True
                memory = True
                register = True
            elif mnemonic == "inc":
                opcode = "6"
                memory = True
                register = True
            elif mnemonic == "dec":
                opcode = "7"
                memory = True
                register = True
            elif mnemonic == "xor":
                opcode = "8"
                immediate = True
                memory = True
                register = True
            elif mnemonic == "and":
                opcode = "9"
                immediate = True
                memory = True
                register = True
            elif mnemonic == "or":
                opcode = "a"
                immediate = True
                memory = True
                register = True
            elif mnemonic == "not":
                opcode = "b"
                memory = True
                register = True
            elif mnemonic == "shl":
                opcode = "c"
                register = True
            elif mnemonic == "shr":
                opcode = "d"
                register = True
            elif mnemonic == "push":
                opcode = "f"
                register = True
            elif mnemonic == "pop":
                opcode = "10"
                register = True
            elif mnemonic == "cmp":
                opcode = "11"
                immediate = True
                memory = True
                register = True
            elif mnemonic == "jmp":
                opcode = "12"
                immediate = True
            elif mnemonic == "jz" or mnemonic == "je":
                opcode = "13"
                immediate = True
            elif mnemonic == "jnz" or mnemonic == "jne":
                opcode = "14"
                immediate = True
            elif mnemonic == "jc":
                opcode = "15"
                immediate = True
            elif mnemonic == "jnc":
                opcode = "16"
                immediate = True
            elif mnemonic == "ja":
                opcode = "17"
                immediate = True
            elif mnemonic == "jae":
                opcode = "18"
                immediate = True
            elif mnemonic == "jb":
                opcode = "19"
                immediate = True
            elif mnemonic == "jbe":
                opcode = "1a"
                immediate = True
            elif mnemonic == "read":
                opcode = "1b"
                memory = True
                register = True
            elif mnemonic == "print":
                opcode = "1c"
                immediate = True
                memory = True
                register = True
            else:
            # If any of these possibilities are not met, then it means we have an error.
                invalidSyntax = True
                break

            # We decode the line to get addressing mode and operand as numbers.
            operand = instr[1]
            pair = operandcheck(operand, labels, immediate=immediate, memory=memory, register=register)
            (addressingmode,operandcode) = pair
            if pair == ("x","x"):
            # If addressing mode and operand code is returned as 'x', then it means we have an error. 
                invalidSyntax = True
                break

        else:
            # This means there are more than 2 arguments in the line. It should give an error.
            invalidSyntax = True
            break

        binaryline = [opcode, addressingmode, operandcode]
        binarycode.append(binaryline)

    if not(invalidSyntax): # If there is no error, then we can write the results into our output file.
        output = open(filename+".bin", "w")
        result = convert(binarycode)
        for code in result:
            output.write(code.upper())
            output.write("\n")
    else: # If we encounter an error, then we print the error message in terminal and the erroneous line.
        lastline = ""
        for i in range(len(instr)):
            lastline += instr[i]
            if i != len(instr)-1:
                lastline += " "
        print("Syntax Error. Check the line:\n\t\"" + lastline +"\"")
