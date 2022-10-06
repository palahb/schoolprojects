typedef string str_t<10000>;

struct part_b_result {
    str_t err;
    str_t out;
    bool is_err;
    bool is_out;
};

struct part_b_struct{
	char blackbox[255];
	int num1;
    int num2;
};

program PART_B_PROG{
	version PART_B_VERS{
		part_b_result part_b(part_b_struct)=1;
	}=1;
}=0x12345678;