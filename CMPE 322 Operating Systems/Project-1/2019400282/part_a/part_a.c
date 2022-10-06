/**
 * @file part_a.c
 * @author Halil Burak Pala
 * @date December 2021
 * @brief A program that creates a child process which executes the given binary file in the argument.
 * 
 * This program is the implementation of the Part A of the CmpE 322 Project 1 in the Fall 2021 semester.
 * In this program, our main program creates one child process that executes the given executable
 * file as argument which takes two integer values as inputs and gives one integer value to stdout 
 * or an error message to stderr as output. Parent process takes two integer values and gives these 
 * integer values to the child process via a pipe. In the child process, before executing the given 
 * executable, the child associates its own stdin and stderr to the pipes so that input of the program
 * is taken from the parent process and output of the program is given to the parent process. Then
 * parent process writes the output to an output file according to whether the output is taken from
 * the stdout or stderr.
 * 
 * How to compile and run (A makefile is provided with the code):
 *  make
 *  ./part_a.out <executable file> <output file> (e.g. ./part_a.out ./blackbox ./part_a_output.txt)
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <string.h>
#include <limits.h>

#define INT_BUFFER_SIZE 12 // Given integer input can be at most 11 characters (longest integer is -2147483648)
#define ERR_BUFFER_SIZE 10000 // Max buffer size for the error message
#define READ_END	0 
#define WRITE_END	1 

int main(int argc, char *argv[]){
    
    char * blackbox = argv[1];  // Path of the executable file
    FILE* outfile = fopen(argv[2], "a"); // Output file
    
    int pipe1[2];   // This pipe is for sending data from the parent to the child.
                    // This pipe's read end will be connected to the child, its write end 
                    // will be connected to the stdin of the child. (P >===> C)

    int pipe2[2];   // This pipe's read end will be connected to the parent, its write end 
                    // will be connected to the stdout of the child. (P <===< C)

    int pipe3[2];   // This pipe's read end will be connected to the parent, its write end
                    // will be connected to the stderr of the child. (P <===< C)
    
    if(pipe(pipe1) != 0) return -1;
    if(pipe(pipe2) != 0) return -1;
    if(pipe(pipe3) != 0) return -1;

    pid_t pid;
    pid = fork(); 

    if(pid > 0){
        /**
         * This is the code for the parent process.
         */

        /**
         * The parent should close the ends of the pipes that it will not use.
         */
        close(pipe1[READ_END]);
        close(pipe2[WRITE_END]);
        close(pipe3[WRITE_END]);

        char str_num1[INT_BUFFER_SIZE], str_num2[INT_BUFFER_SIZE]; // The numbers taken from the console.

        scanf("%s %s", str_num1, str_num2);

        char str[2*INT_BUFFER_SIZE] = "\0"; // We will send given integers to the child process in the format 
                                            // "<num1> <num2>". We will construct this format in this str variable.
                                            // It will be taken from stdin in the child process.
        strcat(str,str_num1); // str is "<num1>"
        strcat(str," "); // str is "<num1> "
        strcat(str,str_num2); // str is "<num1> <num2>"

        write(pipe1[WRITE_END], str, strlen(str)+1); // We send these numbers to the child process by this pipe.

        close(pipe1[WRITE_END]); // Then we close the pipe.

        wait(NULL); // Wait until the chil process is done.
        
        char read_from_stdout[INT_BUFFER_SIZE]; // This is the result read from the child process in case it is
                                                // read from stdout.
        if(read(pipe2[READ_END], read_from_stdout, INT_BUFFER_SIZE)){
            /**
             * If the resultant integer is successfully read, "SUCCESS" and the result
             * is printed to the output file.
             */
            fprintf(outfile,"SUCCES:\n%d\n", atoi(read_from_stdout));
        }

        char read_from_stderr[ERR_BUFFER_SIZE]; // This is the error message read from the child process in case
                                                // it is read from stderr.
        if(read(pipe3[READ_END], read_from_stderr, ERR_BUFFER_SIZE)){
            /**
             * If the error message is read instead of a result from stdout, "FAIL" and the message
             * is printed to the output file.
             */
            fprintf(outfile,"FAIL:\n%s", read_from_stderr);
        }

        close(pipe2[READ_END]);
        close(pipe3[READ_END]);
        fclose(outfile);
    }
    else if(pid == 0){
        /**
         * This is the code for the child process
         */

        /**
         * The child should close the ends of the pipes that it will not use.
         */
        close(pipe1[WRITE_END]);
        close(pipe2[READ_END]);
        close(pipe3[READ_END]);

        dup2(pipe1[READ_END], STDIN_FILENO); // Read end of the pipe1 is connected to the stdin.
        close(pipe1[READ_END]);

        dup2(pipe2[WRITE_END], STDOUT_FILENO); // Write end of the pipe2 is connected to the stdout.
        close(pipe2[WRITE_END]);

        dup2(pipe3[WRITE_END], STDERR_FILENO); // Write end of the pipe3 is connected to the stderr.
        close(pipe3[WRITE_END]);

        execlp(blackbox, blackbox, NULL); // Child executes the given executable file.
        
        // If the program reaches here, it means an unsuccessfull exec();
        return -1;
    }
    else{
        // If the program reaches here, it means an unsuccessfull fork();
        return -1;
    }
    return 0;
}