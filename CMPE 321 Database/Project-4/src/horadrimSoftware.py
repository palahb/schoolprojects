# CMPE 321 Spring 2022 Project 4
# This project is a simulation for a Database Management System.
# More information can be found in the project report.
# Halil Burak Pala - 2019400282
# Berat Damar - 2018400039

# In this implementation bplustree library is used.
# Here is the github link of the library:
# https://github.com/NicolasLM/bplustree
from bplustree.tree import BPlusTree
from bplustree.serializer import StrSerializer
import os, sys, time, glob, re

RECORD_IN_PAGE = 8 # Number of records in a page
PAGE_IN_FILE = 4 # Number of pages in a file
RECORD_DATA_LENGTH = 240 # Size of the data of a record in bytes
PAGE_LENGTH = 1 + (1+RECORD_DATA_LENGTH) * RECORD_IN_PAGE # Total page length

# File, page and record header structures are as follows:
# Every record has a 1 byte header indicating if the record is in use, deleted or empty.
# If in use, it is 0. If deleted, it is 1. If empty, it is 2.
# Every page has a 1 byte header indicating how many records are in use in that page.
# Every file has a header indicating which page is in use. If it is 0,
# then it means that file is full.

########## FUNCTION DEFINITIONS ##########

# This function is for creating a log in a csv file.
def create_log(input, success):
    logfile = open("horadrimLog.csv", "a")
    unixtime = str(int(time.time()))
    scs = "success" if success else "failure"
    logfile.write(unixtime + "," + " ".join(input) + "," + scs + "\n")
    logfile.close()

# This function is for getting all type information in the system catalog.
def get_all_types(debug="aaa"):
    try:
        f = open("systemCatalog.csv", "r")
    except:
        return []
    typeTuples = []

    for line in f:
        line = line.strip()
        words = line.split(",")
        typeTuples.append((*words, ))
    f.close()
    return typeTuples

# This function is for getting primary key information for a type 
# in the system catalog.
def get_primary_key(search_type,debug="aaa"):
    for type in get_all_types(debug):
        if type[0] == search_type:
            return (int(type[1]), type[2])

# This function is for getting available file number. It simply returns
# the maximum id of existing files.
def get_available_file_no():
    files = glob.glob("file*")
    filenos = []
    for file in files:
        fileno = int(file[4:])
        filenos.append(fileno)
    if len(filenos) == 0:
        return 0
    else:
        return max(filenos)

# This function creates a new file. A file includes PAGE_IN_FILE number of
# pages and every page includes RECORD_IN_PAGE number of records.
def create_file(file_no):
    with open("file{}".format(file_no), "w+b") as f:
        f.write(b"1") # Header of the file: 1. pagede yer var
        for i in range(PAGE_IN_FILE):
            f.write(b"0") # Header of a page: 0 tane dolu yer var
            for j in range(RECORD_IN_PAGE):
                f.write(b"2") # Header of a record: 
                f.write(bytearray(RECORD_DATA_LENGTH)) # A record

# This function is for getting a specific page in a file.
def get_page(file_name, page_id):
    with open(file_name, "r+b") as f:
        page_head_position = 1 + PAGE_LENGTH * (page_id-1)
        f.seek(page_head_position)
        page_content = f.read(PAGE_LENGTH)
    return page_content

# This function is overwriting a specific page in a file.
def write_page(page_content, file_name, page_id):
    with open(file_name, "r+b") as f:
        page_position = 1 + PAGE_LENGTH * (page_id-1)
        f.seek(page_position)
        f.write(page_content)

# Inserts source bytes array to a specific position in the 
# destination bytes array. (This function is necessary since
# bytes object is immutable.)
def write_bytes(source,destination,position):
    l_source = list(source)
    l_dest = list(destination)
    length = len(l_source)
    l_dest[position:position+length] = l_source
    return bytes(l_dest)

# This function updates the file header.
def update_file_header(file_name,page_id): 
    # If page id does not indicate the last page of the file
    # we just increment the file header.
    if page_id < PAGE_IN_FILE:
        updated_file_header = bytes(str(page_id + 1),"ascii")
    # If page id indicates the last page of the file, then we
    # just set file header to 0 which indicates the file is full.
    else:
        updated_file_header = bytes("0","ascii")
    with open(file_name,"r+b") as f:
        f.seek(0)
        f.write(updated_file_header)

# Checks if all records in a file is deleted. If so,
# deletes the file.
def check_deleted(file_name):
    deletedCount = 0
    emptyCount = 0
    with open(file_name,"r+b") as f:
        f.read(1)
        for i in range(PAGE_IN_FILE):
            f.read(1)
            for j in range(RECORD_IN_PAGE):
                record_header = f.read(1)
                if record_header == b"1":
                    deletedCount += 1
                if record_header == b"2":
                    emptyCount += 1
                f.read(RECORD_DATA_LENGTH)
    if deletedCount+emptyCount != PAGE_IN_FILE*RECORD_IN_PAGE:
        return False
    else:
        os.remove(file_name)
        return True

# Searches a record in the given location and writes it to the output file. 
# A location includes location info in the form page-file-record_no_in_page.
def search_record(location_in_bytes, out_file_name):
    location = location_in_bytes.decode("ascii")
    location = location.split("-")

    file_no = location[0]
    page_id = location[1]
    slot = location[2]

    file_name = "file" + file_no
    page_content = get_page(file_name, int(page_id))

    record_pos = 1 + (1+RECORD_DATA_LENGTH) * int(slot)
    record = page_content[record_pos:record_pos+241]
    field_values = []
    for i in range(12):
        record_in_bytes = record[1+20*i:1+20*(i+1)]
        record_str = record_in_bytes.rstrip(b"\x00").decode("ascii")
        if(len(record_str) != 0):
            field_values.append(record_str)
    
    fields_str = ' '.join(field_values)

    with open(out_file_name,"a+") as out:
        out.write(fields_str + "\n")

########## THE PROGRAM ##########

input = open(sys.argv[1], "r")

# Input file name is given in format "input_<input_no>.txt"
input_name = re.findall(r"input_\d+.txt", sys.argv[1])[0]
input_no = input_name[6:-4]

# Output file name:
out_file_name = sys.argv[2]

# Taking input file line by line:
lines = []
for line in input:
    line = line.strip()
    tokens = line.split()
    if len(tokens) != 0:
        lines.append(tokens)

input.close()

# Creating the first file if not exists:
if get_available_file_no() == 0:
    create_file(1)

# For every line in the input file:
for line in lines:
    operation = line[0] + " " + line[1]

    if operation == "create type":

        # create type <type-name><number-of-fields><primary-key-order><field1-name><field1-type><field2-name>...
        type_name = line[2]

        # IF CREATE TYPE IS 3 WORDS LONG, IT MEANS A SYNTAX ERROR:
        # (ALTHOUGH WE ASSUMED NO SYNTAX ERROR, THERE IS A SYNTAX ERROR
        # IN TEST CASES, SO WE ADDED THIS TRY-EXCEPT PART. WE ASSUMED
        # NO SYNTAX ERROR CAN OCCUR.)
        try:
            nof_fields = line[3]
        except:
            create_log(line,False)
            continue

        primary_key_order = line[4]

        # If there is already a type with the given name: Fail
        types = [x[0] for x in get_all_types()]
        if type_name in types:
            create_log(line,False)
            continue

        ### If not fail:
        # 1. Insert type info into the system catalog: 
        #    <type_name>, <position_of_prim_key>, <data_type_of_prim_key>
        # 2. Create a new B+ Tree for this type.
        i = 5
        while i < len(line):
            field_name = line[i]
            data_type = line[i+1]
            position = str(int(((i+1)-4) / 2))

            if position == primary_key_order:
                with open("systemCatalog.csv", "a") as types_system_catalog:
                    types_system_catalog.write(type_name + "," + primary_key_order + ","+ data_type +"\n")

                tree_dir = "./"+type_name+"Tree.db"
                if(data_type == "str"):
                    tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
                    tree.close()
                else:
                    tree = BPlusTree(filename=tree_dir,order=50)
                    tree.close()
            i += 2
        create_log(line,True)

    elif operation == "delete type":
        # delete type <type-name>
        type_name = line[2]

        # If there is already a type with the given name: Fail
        types = [x[0] for x in get_all_types()]
        if type_name not in types:
            create_log(line,False)
            continue
        
        # If not fail:
        # 1. For every record id in the B+ Tree, set the record header of these
        #    records as 1 which indicates the record is deleted.
        # 2. Check if all records in the file of deleted record is deleted. If so,
        #    delete the file.
        # 3. Delete the B+ tree of the type.
        # 4. Delete the type information in the system catalog.
        tree_dir = "./"+type_name+"Tree.db"
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            for location_in_bytes in list(tree.values()):
                if location_in_bytes != b"-1":
                    location = location_in_bytes.decode("ascii")
                    location = location.split("-")

                    file_no = location[0]
                    page_id = location[1]
                    slot = location[2]
                    
                    file_name = "file" + file_no
                    page_content = get_page(file_name, int(page_id))

                    record_pos = 1 + (1+RECORD_DATA_LENGTH) * int(slot)

                    page_content = write_bytes(b"1", page_content, record_pos)
                    write_page(page_content,file_name,int(page_id))
                    check_deleted(file_name)
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            for location_in_bytes in list(tree.values()):
                if location_in_bytes != b"-1":
                    location = location_in_bytes.decode("ascii")
                    location = location.split("-")

                    file_no = location[0]
                    page_id = location[1]
                    slot = location[2]
                    
                    file_name = "file" + file_no
                    page_content = get_page(file_name, int(page_id))

                    record_pos = 1 + (1+RECORD_DATA_LENGTH) * int(slot)

                    page_content = write_bytes(b"1", page_content, record_pos)
                    write_page(page_content,file_name,int(page_id))
                    check_deleted(file_name)
            tree.close()
        
        # Deleting the tree:
        os.remove(tree_dir)

        # Deleting the type from system catalog:
        with open("systemCatalog.csv", "r") as f:
            cataloglines = f.readlines()
        
        with open("systemCatalog.csv", "w") as f:
            for cline in cataloglines:
                if cline.split(",")[0] != type_name:
                    f.write(cline)

        create_log(line,True)


    elif operation == "list type":
        
        # If there is no type: Fail
        types_tuples = get_all_types()
        if len(types_tuples) == 0:
            create_log(line,False)
            continue
        
        # If not fail:
        # 1. Simply write every type name to output file.
        with open(out_file_name,"a+") as out:
            for tuple in types_tuples:
                out.write(tuple[0] + "\n")
        create_log(line,True)

    elif operation == "create record":
        # create record <type-name><field1-value><field2-value>...
        type_name = line[2]
        field_values = line[3:]
        # Get data type of the primary key:
        primary_index, primary_data_type = get_primary_key(type_name)
        if primary_data_type == "str":
            primary_key = field_values[primary_index-1]
        else:
            primary_key = int(field_values[primary_index-1])

        ## Creating a record for a nonexisting type: Fail
        types = [x[0] for x in get_all_types()]
        if type_name not in types:
            create_log(line,False)
            continue

        ## Creating a record with a primary key of an existing record:
        # Check corresponding B+ tree for a record
        # If there is a record, Fail
        tree_dir = "./"+type_name+"Tree.db"
        
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            if((tree.get(primary_key) is not None) and (tree.get(primary_key) != b"-1")):
                create_log(line,False)
                tree.close()
                continue
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            if((tree.get(primary_key) is not None) and (tree.get(primary_key) != b"-1")):
                create_log(line,False)
                tree.close()
                continue
            tree.close()

        # If not fail:
        # 1. Get available file number.
        # 2. Check file header: If file is full, create new file.
        # 3. Get the available page in the file to the memory.
        # 4. Get number of record slots in use from the page header. 
        #    Then calculate the available slot number.
        # 5. Create the record. Then write it to the page in memory.
        # 6. Update page header. If page is full after this insertion,
        #    update the file header.
        # 7. Write page in memory back to its file.
        # 8. Insert record id to the B+ Tree. Record id includes the location
        #    of the record in our file structure.

        # Insert record id to B+ tree.
        file_no = get_available_file_no()
        file_name = "file{}".format(file_no)

        with open(file_name,"r+b") as f:
            available_page_id = int.from_bytes(f.read(1),"big")-48
        
        if available_page_id == 0: # There is no available page, create new file
            create_file(file_no+1)
            file_no += 1
            file_name = "file{}".format(file_no)
            available_page_id = 1

        page_content = get_page(file_name,available_page_id)

        # Page header includes the number of occupied record slots:    
        nof_busy_slots = page_content[0]-48

        # Available slot position in the page:
        available_slot_pos = 1 + (1+RECORD_DATA_LENGTH) * (nof_busy_slots)

        # Create the record in bytes.
        record = b"0" # Record header which indicates whether it is deleted or not
        for field_value in field_values:
            record += bytes(field_value,"ascii") + bytearray(20-len(field_value))
        record += bytearray(1+RECORD_DATA_LENGTH-len(record)) 

        page_content = write_bytes(record, page_content, available_slot_pos)
        

        # Increment page header
        incremented_header=bytes(str((page_content[0]-48)+1),"ascii")
        page_content = write_bytes(incremented_header, page_content, 0)
        write_page(page_content, file_name, available_page_id)

        if incremented_header == b"8":
            update_file_header(file_name,available_page_id)
        
        # Insert record to B+ tree:
        # Value of the node of the tree is the location of the record in 
        # our file structure.
        record_location = "{}-{}-{}".format(file_no,available_page_id,nof_busy_slots)
        tree_value = bytes(record_location,"ascii")

        tree_dir = "./"+type_name+"Tree.db"
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            tree[primary_key] = tree_value
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            tree[primary_key] = tree_value
            tree.close()

        create_log(line, True)


    elif operation == "delete record":
        # delete record <type-name><primary-key>
        type_name = line[2]

        # Get data type of the primary key:
        primary_index, primary_data_type = get_primary_key(type_name)
        if primary_data_type == "str":
            primary_key = line[3]
        else:
            primary_key = int(line[3])

        # 1. Get file and page location of the record from B+ Tree:
        tree_dir = "./"+type_name+"Tree.db"
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            location_in_bytes = tree.get(primary_key)
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            location_in_bytes = tree.get(primary_key)
            tree.close()

        # Deleting a nonexisting record: Fail
        if location_in_bytes is None:
            create_log(line,False)
            continue

        # Deleting a deleted record: Fail
        if location_in_bytes == b"-1":
            create_log(line,False)
            continue
        
        # If not fail:
        # 2. Get corresponding page
        # 3. Mark record header as 1 which indicates the record is deleted.
        # 4. Mark record id in B+ tree as None (We don't have delete functionality in B+ tree)
        # 5. Check file if all records are deleted by inspecting record headers.
        #    If all of the records are deleted, delete the file.
        
        # Get the location info:
        location = location_in_bytes.decode("ascii")
        location = location.split("-")

        file_no = location[0]
        page_id = location[1]
        slot = location[2]
        
        file_name = "file" + file_no
        page_content = get_page(file_name, int(page_id))

        record_pos = 1 + (1+RECORD_DATA_LENGTH) * int(slot)

        # Update record header in the page:
        page_content = write_bytes(b"1", page_content, record_pos)

        # Write page back to memory:
        write_page(page_content,file_name,int(page_id))

        # Update the B+ tree:
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            tree[primary_key] = b"-1"
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            tree.get(primary_key)
            tree[primary_key] = b"-1"
            tree.close()

        # Check if all records in the file is deleted:
        check_deleted(file_name)
        
        create_log(line, True)

    elif operation == "update record":
        # update record <type-name><primary-key><field1-value><field2-value>...
        type_name = line[2]

        primary_index, primary_data_type = get_primary_key(type_name)
        if primary_data_type == "str":
            primary_key = line[3]
        else:
            primary_key = int(line[3])

        field_values = line[4:]

        # 1. Get file no, page id, slot from B+ tree.
        # 2. Bring the page to the memory.
        # 3. Overwrite the corresponding record in page.
        # 4. Write page back to the file.

        tree_dir = "./"+type_name+"Tree.db"
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            location_in_bytes = tree.get(primary_key)
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            tree.get(primary_key)
            location_in_bytes = tree.get(primary_key)
            tree.close()

        location = location_in_bytes.decode("ascii")
        location = location.split("-")

        file_no = location[0]
        page_id = location[1]
        slot = location[2]

        file_name = "file" + file_no
        page_content = get_page(file_name, int(page_id))

        record_pos = 1 + (1+RECORD_DATA_LENGTH) * int(slot)

        record = b"0" # Record header which indicates whether it is deleted or not
        for field_value in field_values:
            record += bytes(field_value,"ascii") + bytearray(20-len(field_value))
        record += bytearray(1+RECORD_DATA_LENGTH-len(record)) # 241 byte
        page_content = write_bytes(record, page_content, record_pos)

        write_page(page_content, file_name, int(page_id))

        create_log(line,True)

    elif operation == "search record":
        # search record <type-name><primary-key>
        type_name = line[2]

        primary_index, primary_data_type = get_primary_key(type_name,debug="bbb")

        if primary_data_type == "str":
            primary_key = line[3]
        else:
            primary_key = int(line[3])

        # 1. Get file no, page id, slot from B+ tree.
        #    If there is no such record in the tree or it is deleted from 
        #    the tree: Fail
        # If not fail:
        # 2. Bring the page to the memory.
        # 3. Go the corresponding record location. Get the record fields.
        # 4. Write the field values to the output file.
        # 5. Write page back to the file.

        tree_dir = "./"+type_name+"Tree.db"
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            location_in_bytes = tree.get(primary_key)
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            tree.get(primary_key)
            location_in_bytes = tree.get(primary_key)
            tree.close()

        # If record not exists or deleted: Fail
        if (location_in_bytes is None) or (location_in_bytes == b"-1"):
            create_log(line,False)
            continue

        search_record(location_in_bytes, out_file_name)

        create_log(line,True)
        
        
    elif operation == "list record":
        # list record <type-name>
        type_name = line[2]

        # Get data type of the primary key:
        _, primary_data_type = get_primary_key(type_name)

        # If there is no type with the given name: Fail
        types = [x[0] for x in get_all_types()]
        if type_name not in types:
            create_log(line,False)
            continue

        # 1. For every node in the tree of the given type
        #    perform search operation. (It is described above.)
        tree_dir = "./"+type_name+"Tree.db"
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            for location_in_bytes in list(tree.values()):
                if location_in_bytes != b"-1":
                    search_record(location_in_bytes, out_file_name)
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            for location_in_bytes in list(tree.values()):
                if location_in_bytes != b"-1":
                    search_record(location_in_bytes, out_file_name)
            tree.close()

        create_log(line,True)

    elif operation == "filter record":
        # filter record <type-name><condition>

        # Assumption: Condition statements are given in the correct
        # form always: <primary-key-name> <condition-operator> <query-value>
        # 1. Parse the given condition statement:
        type_name = line[2]
        condition = line[3]
        operator = re.findall(r"[<>=]",condition)[0]
        queried = condition.split(operator)[1]

        # Get data type of the primary key:
        _, primary_data_type = get_primary_key(type_name)

        # If there is no type with the given name: Fail
        types = [x[0] for x in get_all_types()]
        if type_name not in types:
            create_log(line,False)
            continue
        
        # If not fail:
        # 2. For every node in the B+ tree:
        #   2.1. Create boolean condition variable for the key of the tree 
        #        (which is the primary key) for given condition statement.
        #   2.2. Get the location information of the record.
        #   2.3. If the condition is satisfied and the record is not deleted 
        #        perform search operation described above.

        tree_dir = "./"+type_name+"Tree.db"
        if(primary_data_type == "str"):
            tree = BPlusTree(filename=tree_dir,order=50,key_size=20,serializer=StrSerializer())
            keys = tree.keys()
            for key in keys:
                
                if operator == ">":
                    cond = key > queried
                elif operator == "<":
                    cond = key < queried
                else:
                    cond = key == queried

                location_in_bytes = tree[key]
                if cond and location_in_bytes != b"-1":
                    search_record(location_in_bytes, out_file_name)
            tree.close()
        else:
            tree = BPlusTree(filename=tree_dir,order=50)
            for key in list(tree.keys()):
                if operator == ">":
                    cond = key > int(queried)
                elif operator == "<":
                    cond = key < int(queried)
                else:
                    cond = key == int(queried)

                location_in_bytes = tree[key]
                if cond and location_in_bytes != b"-1":
                    search_record(location_in_bytes, out_file_name)
            tree.close()
        
        create_log(line,True)