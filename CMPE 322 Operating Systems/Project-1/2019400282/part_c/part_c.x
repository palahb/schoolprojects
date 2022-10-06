typedef string str_t<10000>;

struct part_c_result {
    str_t err;
    str_t out;
    bool is_err;
    bool is_out;
};

struct part_c_struct{
	char blackbox[255];
	int num1;
    int num2;
};

program PART_C_PROG{
	version PART_C_VERS{
		part_c_result part_c(part_c_struct)=1;
	}=1;
}=0x12345678;