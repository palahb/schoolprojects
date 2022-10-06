/**
 * @file part_c.h
 * This file is created by rpcgen. So I will just explain the parts that I added.
 */

#ifndef _PART_C_H_RPCGEN
#define _PART_C_H_RPCGEN

// Definitions written by me:
#define INT_BUFFER_SIZE 12 // Given integer input can be at most 11 characters (longest integer is -2147483648)
#define ERR_BUFFER_SIZE 10000 // Max buffer size for the error message
#define READ_END 0 // Read end for pipes
#define WRITE_END 1  // Write end for pipes
#define MAXLINE 35 // Max line length of the logging file. (3*(longest integer)+2(space))

#include <rpc/rpc.h>


#ifdef __cplusplus
extern "C" {
#endif

/**
 * Following three variables are created for logging purposes. Since we get the values of these in the
 * part_c_server.c file and do the logging in the part_c_svc.c file, We need to keep three global 
 * variables that can be seen by both part_c_server.c file and part_c_svc.c file. These variables
 * serve for this purpose:
 */

/**
 * @brief Log of the first parameter (Refer to part_c.h for the explanation)
 */
extern char num1_log[INT_BUFFER_SIZE];
/**
 * @brief Log of the second parameter (Refer to part_c.h for the explanation)
 */
extern char num2_log[INT_BUFFER_SIZE];
/**
 * @brief Log of the result (Refer to part_c.h for the explanation)
 */
extern char result_log[INT_BUFFER_SIZE];

// Rest is created by rpcgen autoatically. No info.
typedef char *str_t;

struct part_c_result {
	str_t err;
	str_t out;
	bool_t is_err;
	bool_t is_out;
};
typedef struct part_c_result part_c_result;

struct part_c_struct {
	char blackbox[255];
	int num1;
	int num2;
};
typedef struct part_c_struct part_c_struct;

#define PART_C_PROG 0x12345678
#define PART_C_VERS 1

#if defined(__STDC__) || defined(__cplusplus)
#define part_c 1
extern  part_c_result * part_c_1(part_c_struct *, CLIENT *);
extern  part_c_result * part_c_1_svc(part_c_struct *, struct svc_req *);
extern int part_c_prog_1_freeresult (SVCXPRT *, xdrproc_t, caddr_t);

#else /* K&R C */
#define part_c 1
extern  part_c_result * part_c_1();
extern  part_c_result * part_c_1_svc();
extern int part_c_prog_1_freeresult ();
#endif /* K&R C */

/* the xdr functions */

#if defined(__STDC__) || defined(__cplusplus)
extern  bool_t xdr_str_t (XDR *, str_t*);
extern  bool_t xdr_part_c_result (XDR *, part_c_result*);
extern  bool_t xdr_part_c_struct (XDR *, part_c_struct*);

#else /* K&R C */
extern bool_t xdr_str_t ();
extern bool_t xdr_part_c_result ();
extern bool_t xdr_part_c_struct ();

#endif /* K&R C */

#ifdef __cplusplus
}
#endif

#endif /* !_PART_C_H_RPCGEN */
