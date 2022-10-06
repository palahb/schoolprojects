/**
 * @file part_c_logger.c
 * @author Halil Burak Pala
 * @date December 2021
 * @brief Logger program which logs the input and output values of the RPC function requested by
 * the client. 
 * 
 * This program is the implementation of the Logger of the Part C of the CmpE 322 Project 1 in the 
 * Fall 2021 semester.
 * This program serves as a server for logging the input and output values of the RPC function call. 
 * It creates a socket, binds the socket to the server address. Then waits until a log arrives from 
 * the client. Everytime a log arrives, it prints this log to the given log file.
 * 
 * I used the explanation and the code snippet from this address:
 * https://www.geeksforgeeks.org/udp-server-client-implementation-c/
 * 
 * How to compile and run Part C (A makefile is provided with the code):
 *  make
 *  ./part_c_logger.out <log file> <PORT_NUMBER> &
 *  ./part_c_server.out <LOGGER_IP_ADDRESS> <PORT_NUMBER> &
 *  ./part_c_client.out <path of the executable file> <output file> <SERVER_IP_ADDRESS>
 * 
 */
#include "part_c.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>

// Driver code of the logger:
int main(int argc, char const *argv[]) {
	int sockfd; // Socket file descriptor
	char buffer[MAXLINE]; // Incoming message
	struct sockaddr_in servaddr, cliaddr; // Server address and client address
	const char * logfile = argv[1]; // Logging file
    int PORT = atoi(argv[2]); // Port number 

	// Creating socket file descriptor
	if ( (sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0 ) {
		perror("socket creation failed");
		exit(EXIT_FAILURE);
	}
	
    // Initializing the server address:
	memset(&servaddr, 0, sizeof(servaddr));
    // Initializing the client address:
	memset(&cliaddr, 0, sizeof(cliaddr));
	
	// Filling server information
	servaddr.sin_family = AF_INET; // We use IPv4. Here, we state that.
	servaddr.sin_addr.s_addr = INADDR_ANY;  // IP address of the server. Set to 0.0.0.0. As far as I understand, 
                                            // That means "listen on all available interfaces in this computer".
	servaddr.sin_port = htons(PORT); // Port that we will wait for the connections
	
	// Bind the socket with the server address:
    if ( bind(sockfd, (const struct sockaddr *)&servaddr,sizeof(servaddr)) < 0 ){
        perror("bind failed");
        exit(EXIT_FAILURE);
    }
    // After binding the socket, keep listening and receiving the logs from the part_c_server.out:
    while(1){ 
        FILE* out = fopen(logfile, "a"); // Opening the log file
        int len, n; // Length of the client address and the log taken from the client
        len = sizeof(cliaddr); 

        // Receiving the log to the buffer from the part_c_server.out:
        n = recvfrom(sockfd, (char *)buffer, MAXLINE, MSG_WAITALL, (struct sockaddr *) &cliaddr, &len);
        buffer[n] = '\0'; // Last char of the string is '\0'

        fprintf(out,"%s", buffer); // Printing the log to the log file.
        fclose(out);
    }
	
	return 0;
}
