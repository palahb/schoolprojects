import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.*;
/**
 * Implementation of Spring 2021 CMPE 260 Project 1
 * In this project, we developed a translator called mylang2IR for a language called MyLang.
 * mylang2IR generates low-level LLVM IR code that will compute and output MyLang language statements.
 * 
 * @author Halil Burak Pala
 * @author Huseyin Turker Erdem
 */
public class Main{

    static int temp = 0;    // This variable is used for temporary variables.
    static int ifCondCounter = 0;   // This variable is for if condition branches.
    static int whileCondCounter = 0;    // This variable is for while condition branches.
    static boolean inIfCondition = false;	// This variable is to determine if the processed
    										// line is in an if block or not
    static boolean inWhileCondition = false;	// This variable is to determine if the processed
    											// line is in a while block or not
    static String irCode = "";  // This is our IR Code. As we progress through the code,
                                // this IR Code is updated.
    public static void main(String args[]) throws FileNotFoundException{
    	
        Scanner input = new Scanner(new File(args[0]));
        
        // Output file name is generated from the input file name
        PrintStream output = new PrintStream(new File(fromMyToLl(args[0])));
        
        ArrayList<String> lines = new ArrayList<String>();  // This ArrayList keeps every line
                                                            // as String at first.
        Set<String> variables = new TreeSet<String>();  // This is a Set where we keep our overall
                                                        // variables so that we can initialize them
                                                        // at the top of our IR code.
        boolean thereIsChoose = false;  // This is a flag which we keep whether our code has a choose
                                        // function or not so that we define choose function at the top 
                                        // of our IR code.
        
        // Here, we eliminated space and tab characters from our lines.
        while(input.hasNextLine()){
            String line = input.nextLine();
            line = line.replace(" ", "");
            line = line.replace("\t", "");
            lines.add(line);
        }
        
        // Here, we are searching for our variables which are by definition can start with a letter
        // and continue with any alphanumerical character. This is done by the help of RegEx. 
        String variableRegEx = "^[a-zA-Z][a-zA-Z0-9]*$";
        Pattern r = Pattern.compile(variableRegEx);
        Matcher m;
        
        // This variable will be used to give output if there is a syntax error on that line.
        int lineNum = 0;

        
        for(String line : lines){
        	++lineNum;
            StringTokenizer tokenizer = new StringTokenizer(line, "=+-*/(){}#,", true);
            while(tokenizer.hasMoreTokens()){
                String var = tokenizer.nextToken();
                m = r.matcher(var);
                // We control if the found variable candidate is one of our reserved words. If not so
                // we add it to our variables Set.
                if(m.find()){
                    if(!var.equals("while") && !var.equals("if") && !var.equals("choose") && !var.equals("print")){
                        variables.add(var);
                    } else{
                        var = tokenizer.nextToken();
                        if(!var.equals("(")){
                            output.println(printSyntaxError(lineNum));
                            return;
                        }
                    }
                }
            }
        }
        // This part was only for searching and initializing our vaiables.

        // This 2D ArrayList lexemes of every line one by one. For example let
        // line x be "z = (6 + 8*(choose(y, 4, 5, z)))". Then lexemesList[x] is
        // ["6", "+", "8", "*", "(", "choose", "(", "y", ",", "4", ...]
        ArrayList<ArrayList<String>> lexemesList = new ArrayList<ArrayList<String>>();
        
        // Here we are searching for lexemes of every line.
        for(int i = 0 ; i < lines.size() ; i++){
            ArrayList<String> lexemes = new ArrayList<String>();
            String line = lines.get(i);
            StringTokenizer tokenizer = new StringTokenizer(line, "=+-*/(){}#,", true);
            while(tokenizer.hasMoreTokens()){
                String token = tokenizer.nextToken();
                if(token.equals("choose")) thereIsChoose = true;

                // Here, we handle the case of negative numbers. For example when a line like
                // "y=-3+5" or "y=x+(-5+6)" or "y=choose(-9,-3,-4,-1)" is given, we combine the
                // minus sign and subsequent number so that we do not have two different lexemes
                // such as ["-","9"]. Instead, we took them together as one lexeme, ["-9"]. 
                if(token.equals("-")){
                    int lastIndex;
                    if(lexemes.size() > 0){
                        lastIndex = lexemes.size()-1;
                    } else{
                        lexemes.add(token);
                        continue;
                    }
                    if(lexemes.get(lastIndex).equals("=") || lexemes.get(lastIndex).equals(",")
                    || lexemes.get(lastIndex).equals("+") || lexemes.get(lastIndex).equals("*") 
                    || lexemes.get(lastIndex).equals("/") || lexemes.get(lastIndex).equals("(")){
                        String nextToken = tokenizer.nextToken();
                        token = token + nextToken;
                    } 
                    // In this part, we handle the case of multiple minus signs. For example
                    // we assumed that "5--7" is valid and parsed to ["5","-","-7"]. But in the 
                    // case of more than 2 minus signs, we assumed that it is not valid and for example
                    // "7---5" is parsed to ["7", "-","-","-","5"].
                    else if(lexemes.get(lastIndex).equals("-")){
                        try{
                            if(!lexemes.get(lastIndex-1).equals("-")){
                                String nextToken = tokenizer.nextToken();
                                token = token + nextToken;
                            }
                        }catch(Exception e){
                            lexemes.add(token);
                            continue;
                        }
                    }
                }

                // Here, we ignore all the comments that are written after the "#" sign,
                // we do not take anything after "#" to our lexemes.
                if(token.equals("#")){
                    while(tokenizer.hasMoreTokens()){
                        tokenizer.nextToken();
                    }
                    continue;
                }
                lexemes.add(token);
            }

            // We add our lexemes at line to lexemesList.
            lexemesList.add(lexemes);
        }
        
        // This is the very first part of our IR code. It is written always at the top.
        irCode = irCode + ("; ModuleID = \'mylang2ir\'\n"
        + "declare i32 @printf(i8*, ...)\n"
        + "@print.str = constant [4 x i8] c\"%d\\0A\\00\"\n\n");
        
        // We set choose function in our IR code to use if there is any. 
        if(thereIsChoose){
            irCode = irCode + ("define i32 @choose(i32 %a, i32 %b, i32 %c, i32 %d){\n"
            +"entry:\n"
            +"  %retval = alloca i32\n"
            +"  %0 = icmp sge i32 %a, 0\n"
            +"  br i1 %0, label %gtez, label %neg\n\n"
            
            +"gtez:\n"
            +"  %1 = icmp eq i32 %a, 0\n"
            +"  br i1 %1, label %zero, label %pos\n\n"
            
            +"zero:\n"
            +"  store i32 %b, i32* %retval\n"
            +"  br label %end\n\n"
            
            +"pos:\n"
            +"  store i32 %c, i32* %retval\n"
            +"  br label %end\n\n"
            
            +"neg:\n"
            +"  store i32 %d, i32* %retval\n"
            +"  br label %end\n\n"
            
            +"end:\n"
            +"  %2 = load i32* %retval\n"
            +"  ret i32 %2\n"
            +"}\n\n");
        }

        // We begin main function of IR code.
        irCode = irCode + "define i32 @main() {\n";

        // Here, we allocate memory for all variables.
        for(String var : variables){
            irCode = irCode + "  %_" + var + " = alloca i32\n";
        }

        irCode = irCode + "\n";

        // We initialize all variables to zero.
        for(String var : variables){
            irCode = irCode + "  store i32 0, i32* %_" + var + "\n";
        }

        // Line number is re-initialized so that if there is a syntax error, 
        // then related error message will be given as output.
        lineNum = 0;
        
        try {
        	for(ArrayList<String> singleLineAsLexemes:lexemesList) {
        		++lineNum;
            	processLine(singleLineAsLexemes);
            }
        	// At the end of the input file, if any "{" is not matched, then throw exception
        	if(inIfCondition || inWhileCondition) {
				throw new CompilerSyntaxException("last line is not \"}\"");
			}
        	
        	irCode = irCode.concat("\n  ret i32 0\n}");
            
        	// Print IR code if there is not any exception so far.
        	// This line is also the last line of main.
        	output.println(irCode);

		} catch (CompilerSyntaxException e) {
            output.println(printSyntaxError(lineNum));
		}
        
    }

    /**
     * This function creates an IR code that prints "Line 6: syntax error". 
     * Line number is the parameter. If error is on line 6, then parameter will
     * be given to the function as 6, but the output IR code will print: 
     * "Line 5: syntax error"
     * @param i Line number to be printed.
     * @return A string of IR code.
     */
    private static String printSyntaxError(int i) {
    	String s = "; ModuleID = \'mylang2ir\'\n" +
					"declare i32 @printf(i8*, ...)\n" +
					"@print.str = constant [23 x i8] c\"Line %d: syntax error\\0A\\00\"\n\n" +  
					"define i32 @main() {\n" +
					"call i32 (i8*, ...)* @printf(i8* getelementptr ([23 x i8]* @print.str, i32 0, i32 0), i32 " + (i-1) +")\n" +
   					"ret i32 0\n" +
					"}";
		//TODO burada IR code olusturulacak
		return s;
	}

    /**
     * This function converts file name from "filename.my" to "filename.ll".
     * @param s Input file name with ".my" extension.
     * @return Output file name with ".ll" extension.
     */
	private static String fromMyToLl(String s) {
    	return (s.substring(0, s.length()-2) + "ll");
    }

	/**
     * This function prints IR Code of any given postfix expression.
     * @param postfix Postfix representation of an expression. It can include
     * IR code temporary variable.
     * @return Temporary variable number of the result of expression.
     */
    public static int printIrCodeOfExpression(Queue<String> postfix){
        Stack<String> stack = new Stack<String>();  // This stack is used for printing the corresponding
                                                    // postfix expression.
        if(postfix.size() == 1){    // If postfix includes only one operand,
            String id = postfix.remove();
            if(isNum(id)){  // and this operand is a number, just put it into a temporary
                            // variable by adding 0 to it.
            	irCode = irCode.concat("  %_" + ++temp + " = add i32 0, " + id + "\n");
            }else{  // If this one operand is not a number,
                if(id.length() >= 15){ // and its length is more than 15,
                    if(id.substring(0, 6).equals("choose")){ // and it is a choose function,
                        // get 4 expressions of this choose function and put them in exprsOfChoose.
                        // By calling expression fuction for each of them, get postfix representation
                        // of them. Then call this function recursively so that IR Codes of the expressions 
                        // that are inside of the choose function are printed.
                        ArrayList<ArrayList<String>> exprsOfChoose = getExpressionsofChoose(id.substring(6));

                        Queue<String> e1postfix = new LinkedList<String>();
                        expression(exprsOfChoose.get(0), false, e1postfix);
                        int e1 = printIrCodeOfExpression(e1postfix);

                        Queue<String> e2postfix = new LinkedList<String>();
                        expression(exprsOfChoose.get(1), false, e2postfix);
                        int e2 = printIrCodeOfExpression(e2postfix);

                        Queue<String> e3postfix = new LinkedList<String>();
                        expression(exprsOfChoose.get(2), false, e3postfix);
                        int e3 = printIrCodeOfExpression(e3postfix);

                        Queue<String> e4postfix = new LinkedList<String>();
                        expression(exprsOfChoose.get(3), false, e4postfix);
                        int e4 = printIrCodeOfExpression(e4postfix);

                        // Print the choose function call by placing expressions.
                        irCode = irCode.concat("  %_" + ++temp + " = call i32 @choose(i32 %_" + e1 + ", i32 %_" + e2 + ", i32 %_" + e3 + ", i32 %_" + e4 + ")\n");
                    } else{
                        // If the operand is 15 characters long but not choose function, then it is
                        // an ordinary variable. 
                    	irCode = irCode.concat("  %_" + ++temp + " = load i32* %_" + id + "\n");
                    }
                }else{
                    // If the length of operand is less than 15 and it is not number, then it is
                    // an ordinary variable.
                    irCode = irCode.concat("  %_" + ++temp + " = load i32* %_" + id + "\n");
                }
            }
            // Return the number of last temporary variable.
            return temp;
        }
        // If postfix has more than one elements inside it, do the following:
        postfix.add("_end_"); // This indicates the end of the expression.
        // Get two elements which are first two operands at the postfix and put them
        // in the stack. Remove them from postfix.
        stack.push(postfix.remove());   
        stack.push(postfix.remove());
        while(!stack.peek().equals("_end_")){ // While you don't reach the end of the expression
            String headOfPostfix = postfix.peek(); // Look at the top of postfix.
            
            // If there is not an operation at the head of postfix, that means it is an operand.
            // Simply push it into the stack.
            if(!headOfPostfix.equals("+") && !headOfPostfix.equals("-") && !headOfPostfix.equals("*") && !headOfPostfix.equals("/")){
                stack.push(postfix.remove());
            } 

            // If there is an operation at the head of postfix, apply this operation to 
            // first two operands at the top of stack and push it into the stack. Just ordinary
            // postfix evaluation.
            else {
                String operation = postfix.remove(); // Operation to be applied
                String id1 = stack.pop(); // First operand
                String id2 = stack.pop(); // Second operand
                int t1, t2; // These are used for temporary variables of IR code.
                            // These temporary variables are ultimately printed for 
                            // evaluating the expression.
                
                if(!isNum(id1)){ // If our first operand is not a number,
                    if(id1.charAt(0) == '%'){ // If it is a variable in IR Code,
                        // Just get the number of it and assign it to the first temporary
                        // variable that will be used to evaluate the expression.
                        t1 = Integer.parseInt(id1.substring(2)); 
                    } 
                    else if(id1.length() >= 15){    // If it is not a variable of IR Code and at least
                                                    // 15 characters long
                        if(id1.substring(0, 6).equals("choose")){ // and it is a choose function,
                            // get 4 expressions of this choose function and put them in exprsOfChoose.
                            // By calling expression fuction for each of them, get postfix representation
                            // of them. Then call this function recursively so that IR Codes of the expressions 
                            // that are inside of the choose function are printed.
                            ArrayList<ArrayList<String>> exprsOfChoose = getExpressionsofChoose(id1.substring(6));

                            Queue<String> e1postfix = new LinkedList<String>();
                            expression(exprsOfChoose.get(0), false, e1postfix);
                            int e1 = printIrCodeOfExpression(e1postfix);

                            Queue<String> e2postfix = new LinkedList<String>();
                            expression(exprsOfChoose.get(1), false, e2postfix);
                            int e2 = printIrCodeOfExpression(e2postfix);

                            Queue<String> e3postfix = new LinkedList<String>();
                            expression(exprsOfChoose.get(2), false, e3postfix);
                            int e3 = printIrCodeOfExpression(e3postfix);

                            Queue<String> e4postfix = new LinkedList<String>();
                            expression(exprsOfChoose.get(3), false, e4postfix);
                            int e4 = printIrCodeOfExpression(e4postfix);

                            t1 = ++temp; // Get a new temporary variable
                            // and assign it to the rsult of choose function that is
                            // evaluated in the IR Code.
                            irCode = irCode.concat("  %_" + t1 + " = call i32 @choose(i32 %_" + e1 + ", i32 %_" + e2 + ", i32 %_" + e3 + ", i32 %_" + e4 + ")\n\n");
                            
                        }else{
                            // If the operand is 15 characters long but not choose function, then it is
                            // an ordinary variable. Get a new temporary variable and assign it to this
                            // variable.
                            t1 = ++temp;
                            irCode = irCode.concat("  %_" + t1 + " = load i32* %_" + id1 + "\n");
                        }
                    } 
                    else{
                        // If the length of operand is less than 15 and it is not number, then it is
                        // an ordinary variable. Get a new temporary variable and assign it to this
                        // variable.
                        t1 = ++temp;
                        irCode = irCode.concat("  %_" + t1 + " = load i32* %_" + id1 + "\n");
                    }
                } else{
                    // If the first operand is a number, assign it to a new temporary variable by
                    // adding 0 to it. 
                    t1 = ++temp;
                    irCode = irCode.concat("  %_" + t1 + " = add i32 0, " + id1 + "\n");
                }
                if(!isNum(id2)){ // If our second operand is not a number,
                    if(id2.charAt(0) == '%'){ // If it is a variable in IR Code,
                        // Just get the number of it and assign it to the first temporary
                        // variable that will be used to evaluate the expression.
                        t2 = Integer.parseInt(id2.substring(2));
                    }
                    else if(id2.length() >= 15){    // If it is not a variable of IR Code and at least
                                                    // 15 characters long
                        if(id2.substring(0, 6).equals("choose")){ // and it is a choose function,
                            // get 4 expressions of this choose function and put them in exprsOfChoose.
                            // By calling expression fuction for each of them, get postfix representation
                            // of them. Then call this function recursively so that IR Codes of the expressions 
                            // that are inside of the choose function are printed.
                            ArrayList<ArrayList<String>> exprsOfChoose = getExpressionsofChoose(id2.substring(6));

                            Queue<String> e1postfix = new LinkedList<String>();
                            expression(exprsOfChoose.get(0), false, e1postfix);
                            int e1 = printIrCodeOfExpression(e1postfix);

                            Queue<String> e2postfix = new LinkedList<String>();
                            expression(exprsOfChoose.get(1), false, e2postfix);
                            int e2 = printIrCodeOfExpression(e2postfix);

                            Queue<String> e3postfix = new LinkedList<String>();
                            expression(exprsOfChoose.get(2), false, e3postfix);
                            int e3 = printIrCodeOfExpression(e3postfix);

                            Queue<String> e4postfix = new LinkedList<String>();
                            expression(exprsOfChoose.get(3), false, e4postfix);
                            int e4 = printIrCodeOfExpression(e4postfix);

                            t2 = ++temp;
                            irCode = irCode.concat("  %_" + t2 + " = call i32 @choose(i32 %_" + e1 + ", i32 %_" + e2 + ", i32 %_" + e3 + ", i32 %_" + e4 + ")\n");
                        } else{
                            t2 = ++temp;
                            irCode = irCode.concat("  %_" + t2 + " = load i32* %_" + id2 + "\n");
                        }
                    }
                    else{
                        t2 = ++temp; // Get a new temporary variable
                        // and assign it to the rsult of choose function that is
                        // evaluated in the IR Code.
                        irCode = irCode.concat("  %_" + t2 + " = load i32* %_" + id2 + "\n");
                    }
                } 
                else{                    
                    // If the second operand is a number, assign it to a new temporary variable by
                    // adding 0 to it. 
                    t2 = ++temp;
                    irCode = irCode.concat("  %_" + t2 + " = add i32 0, " + id2 + "\n");
                }
                if(operation.equals("+")){
                    // If operation is +, then print the addition code.
                    irCode = irCode.concat("  %_" + ++temp + " = add i32 %_" + t2 + ", %_" + t1 + "\n\n");
                } 
                else if(operation.equals("*")){
                    // If operation is *, then print the multilication code.
                    irCode = irCode.concat("  %_" + ++temp + " = mul i32 %_" + t2 + ", %_" + t1 + "\n\n");
                } 
                else if(operation.equals("-")){
                    // If operation is -, then print the subtraction code.
                    irCode = irCode.concat("  %_" + ++temp + " = sub i32 %_" + t2 + ", %_" + t1 + "\n\n");
                } 
                else if(operation.equals("/")){
                    // If operation is /, then print the signed division code.
                    irCode = irCode.concat("  %_" + ++temp + " = sdiv i32 %_" + t2 + ", %_" + t1 + "\n\n");
                }
                // At the end of this process, the result of the computed expression is
                // stored in the temporary variable. Push it to the stack as an IR Code 
                // temporary variable.
                stack.push(("%_"+(temp)));
            }
        }
        // At the end overall process, result of the expression is at the temporary varible. Return this
        // temporary variable as a number.
        return temp;
    }

    /**
     * Returns 4 expressions of a valid choose function. For example
     * let "choose(x+y, y*6, 5, (4))" is given. Then this function returns
     * [["x", "+", "y"],["y", "*", "6"], ["5"], ["(","4",")"]].
     * @param choose A choose function given as a string.
     * @return An ArrayList which contains 4 expressions of 
     * choose function as ArrayLists which contains lexemes
     * of these expressions.
     */
    public static ArrayList<ArrayList<String>> getExpressionsofChoose(String choose){
        
        ArrayList<String> lexemes = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(choose, "+-*/(),", true);

        // Here, we took every lexeme of the expressions.
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();

            // Here, we handle the case of negative numbers. For example when a line like
            // "y=-3+5" or "y=x+(-5+6)" or "y=choose(-9,-3,-4,-1)" is given, we combine the
            // minus sign and subsequent number so that we do not have two different lexemes
            // such as ["-","9"]. Instead, we took them together as one lexeme, ["-9"]. 
            if(token.equals("-")){
                int lastIndex;
                if(lexemes.size() > 0){
                    lastIndex = lexemes.size()-1;
                } else{
                    lexemes.add(token);
                    continue;
                }
                if(lexemes.get(lastIndex).equals("=") || lexemes.get(lastIndex).equals(",") 
                || lexemes.get(lastIndex).equals("+") || lexemes.get(lastIndex).equals("*") 
                || lexemes.get(lastIndex).equals("/") || lexemes.get(lastIndex).equals("(")){
                    String nextToken = tokenizer.nextToken();
                    token = token + nextToken;
                } 

                // In this part, we handle the case of multiple minus signs. For example
                // we assumed that "5--7" is valid and parsed to ["5","-","-7"]. But in the 
                // case of more than 2 minus signs, we assumed that it is not valid and for example
                // "7---5" is parsed to ["7", "-","-","-","5"].
                else if(lexemes.get(lastIndex).equals("-")){
                    try{
                        if(!lexemes.get(lastIndex-1).equals("-")){
                            String nextToken = tokenizer.nextToken();
                            token = token + nextToken;
                        }
                    }catch(Exception e){
                        lexemes.add(token);
                        continue;
                    }
                }
            }
            lexemes.add(token);
        }

        ArrayList<ArrayList<String>> expressions = new ArrayList<ArrayList<String>>();
        
        // After this part, we get the expressions from all lexemes. The tricky part was
        // case of nested choose, but we handled it by a function named nestedChoose.

        ArrayList<String> expr1 = new ArrayList<String>();
        String lexeme = lexemes.get(1);
        int i;
        for(i = 1 ; !lexeme.equals(","); i++){
            if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
                i = nestedChoose(expr1, lexemes, lexeme, i);
                lexeme = lexemes.get(i+1);
                continue;
            }
            expr1.add(lexeme);
            lexeme = lexemes.get(i+1);
        }


        ArrayList<String> expr2 = new ArrayList<String>();
        lexeme = lexemes.get(i+1);
        i++;
        for(; !lexeme.equals(","); i++){
            if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
                i = nestedChoose(expr2, lexemes, lexeme, i);
                lexeme = lexemes.get(i+1);
                continue;
            }
            expr2.add(lexeme);
            lexeme = lexemes.get(i+1);
        }

        
        ArrayList<String> expr3 = new ArrayList<String>();
        lexeme = lexemes.get(i+1);
        i++;
        for(; !lexeme.equals(","); i++){
            if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
                i = nestedChoose(expr3, lexemes, lexeme, i);
                lexeme = lexemes.get(i+1);
                continue;
            }
            expr3.add(lexeme);
            lexeme = lexemes.get(i+1);
        }


        ArrayList<String> expr4 = new ArrayList<String>();
        lexeme = lexemes.get(i+1);
        i++;
        for(; i<lexemes.size()-1; i++){
            if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
                i = nestedChoose(expr4, lexemes, lexeme, i);
                lexeme = lexemes.get(i+1);
                continue;
            }
            expr4.add(lexeme);
            lexeme = lexemes.get(i+1);
        }

        // After getting 4 expressions of choose function, we add them to
        // expressions ArrayList and return it.
        expressions.add(expr1);
        expressions.add(expr2);
        expressions.add(expr3);
        expressions.add(expr4);

        return expressions;
    }

    /**
     * Checks whether given string is a number or not.
     * @param n String
     * @return Whether n is number or not.
     */
    public static boolean isNum(String n){
        try{
            Integer.parseInt(n);
        } catch(Exception e){
            return false;
        }
        return true;
    }

    /**
     * Checks whether given string is a valid variable.
     * @param s String
     * @return Whether s is a valid variable name or not.
     */
    public static boolean isVar(String s) {
        String variableRegEx = "^[a-zA-Z][a-zA-Z0-9]*$";
        Pattern r = Pattern.compile(variableRegEx);
        Matcher m = r.matcher(s);
        if(m.find()){
        	return true;
        }
        else {
        	return false;
        }
	}

    // After this point, the functions that are required for syntax of our language
    // are implemented. BNF Notation of this syntax is as follows:
    // <expr> -> <term> <moreterms>
    // <moreterms> -> "+" <term> <moreterms> | "-" <term> <moreterms> | ""
    // <term> -> <factor> <morefactors>
    // <morefactors> -> "*" <factor> <morefactors> | "/" <factor> <morefactors> | ""
    // <factor> -> "("<expr>")" | <id> | <num> | <choose>
    // <choose> -> "choose(" <expr> "," <expr> "," <expr> "," <expr> ")"
    
    /**
     * This function checks whether given lexemes list is an ArrayList with size 1
     * and this only element is a number.
     * @param lexemes An ArrayList containing one or more lexemes.
     * @param fromChooseCall Whether this function is called from a choose function. If it is so
     * the lexemes are not added to postfix.
     * @param postfix Postfix representation of an expression which can be updated
     * by this function.
     * @return Whether given lexemes list is an ArrayList with size 1
     * and this only element is a number.
     */
    public static boolean num(ArrayList<String> lexemes, boolean fromChooseCall, Queue<String> postfix){
        if(lexemes.size() == 1){
            String numCandidate = lexemes.get(0);
            try{
                Integer.parseInt(numCandidate);
            } catch(Exception e){
                return false;
            }
            if(!fromChooseCall){
                postfix.add(numCandidate);
            }
            return true; 
        } else return false;
    }

    /**
     * This function checks whether given lexemes list is an ArrayList with size 1
     * and this only element is a valid variable.
     * @param lexemes An ArrayList containing some lexemes.
     * @param fromChooseCall Whether this function is called from a choose function. If it is so
     * the lexemes are not added to postfix.
     * @param postfix Postfix representation of an expression which can be updated
     * by this function.
     * @return Whether given lexemes list is an ArrayList with size 1
     * and this only element is a valid variable.
     */
    public static boolean id(ArrayList<String> lexemes, boolean fromChooseCall, Queue<String> postfix){
        if(lexemes.size() == 1){
            if(isVar(lexemes.get(0))){
                if(!fromChooseCall){
                    postfix.add(lexemes.get(0));
                }
                return true;
            } else return false;
        }
        else return false;
    }

    /**
     * This function handles the nested choose functions in the given code.
     * @param exprCandidate Expression candidate that is going to be checked.
     * @param lexemes Lexemes
     * @param lexeme Lexeme
     * @param i Index of lexeme.
     * @return Index of the lexeme that is after all 'nested choose's.
     */
    public static int nestedChoose(ArrayList<String> exprCandidate, ArrayList<String> lexemes, String lexeme, int i){
        Stack<String> stack = new Stack<String>(); // This stack is for checking paratnheses equality.
	    stack.push(lexeme);
	    exprCandidate.add(lexeme);
	    exprCandidate.add(lexemes.get(i+1));
	    lexeme = lexemes.get(i+2);
	    i+=2;
	    while(!stack.empty()){
            // Equality checks are done here.
	        if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
	            stack.push(lexeme);
	            exprCandidate.add(lexeme);
	            exprCandidate.add(lexemes.get(i+1));
	            lexeme = lexemes.get(i+2);
	            i+=2;
	        } else if(lexeme.equals("(")){
	            stack.push(lexeme);
	            exprCandidate.add(lexeme);
	            lexeme = lexemes.get(i+1);
	            i++;
	        }else if(lexeme.equals(")")){
	            stack.pop();
	            exprCandidate.add(lexeme);
	            lexeme = lexemes.get(i+1);
	            i++;
	        } else{
	            exprCandidate.add(lexeme);
	            lexeme = lexemes.get(i+1);
	            i++;
	        }
	        if(i > lexemes.size()) return -1;
	    }
	    return i-1;
	}

    /**
     * Checks whether given list of lexemes obeys the syntax of choose function in MyLang.
     * @param lexemes A list of lexemes.
     * @param fromChooseCall Whether this function is called from a choose function. If it is so
     * the lexemes are not added to postfix.
     * @param postfix Postfix representation of an expression which can be updated by
     * this function.
     * @return Whether given list of lexemes obeys the syntax of choose function in MyLang.
     */
    public static boolean choose(ArrayList<String> lexemes, boolean fromChooseCall, Queue<String> postfix){

        // The overall function is in a try-catch block. By doing so we can handle some exceptions
        // such as index-out-of-bounds exception by returning false in these cases.
        try{
            // If first element of lexemes is "choose", second element is "(" and the last one is ")", these
            // lexemes can be investigated by this function:
            if(lexemes.get(0).equals("choose") && lexemes.get(1).equals("(") && lexemes.get(lexemes.size()-1).equals(")")){
                
                // We create four 'candidate' variables which are then checked whether they
                //obey the syntax of an expression.
                ArrayList<String> exprCandidate1 = new ArrayList<String>();
                String lexeme = lexemes.get(2); // This is the first element after the first "(". We start
                // checking lexemes until we hit first of the three commas of this choose function.
                int i; // This is the index of last lexeme that is to be checked.
                for(i = 2 ; !lexeme.equals(","); i++){

                        // There can be nested 'choose's. In that case, searching for a comma would not work.
                        // So our nestedChoose function handles this case by adding these nested 'choose's 
                        // until there are none of them.
                        if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
                            i = nestedChoose(exprCandidate1, lexemes, lexeme, i);
                            lexeme = lexemes.get(i+1);
                            if(i == -1) return false;
                            continue;
                        }
                        exprCandidate1.add(lexeme);
                        lexeme = lexemes.get(i+1);
    
                }

                // The procedure followed in expression candidate 1 is repeated for 2nd, 3rd
                // and 4th expression candidates. 
                ArrayList<String> exprCandidate2 = new ArrayList<String>();
                lexeme = lexemes.get(i+1);
                i++;
                for(; !lexeme.equals(","); i++){
                        if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
                            i = nestedChoose(exprCandidate2, lexemes, lexeme, i);
                            lexeme = lexemes.get(i+1);
                            if(i == -1) return false;
                            continue;
                        }
                        exprCandidate2.add(lexeme);
                        lexeme = lexemes.get(i+1);
    
                }
    
                
                ArrayList<String> exprCandidate3 = new ArrayList<String>();
                lexeme = lexemes.get(i+1);
                i++;
                for(; !lexeme.equals(","); i++){
                        if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
                            i = nestedChoose(exprCandidate3, lexemes, lexeme, i);
                            lexeme = lexemes.get(i+1);
                            if(i == -1) return false;
                            continue;
                        }
                        exprCandidate3.add(lexeme);
                        lexeme = lexemes.get(i+1);
    
                }
    
    
                ArrayList<String> exprCandidate4 = new ArrayList<String>();
                lexeme = lexemes.get(i+1);
                i++;
                for(; i<lexemes.size()-1; i++){
                        if(lexeme.equals("choose") && lexemes.get(i+1).equals("(")){
                            i = nestedChoose(exprCandidate4, lexemes, lexeme, i);
                            lexeme = lexemes.get(i+1);
                            if(i == -1) return false;
                            continue;
                        }
                        exprCandidate4.add(lexeme);
                        lexeme = lexemes.get(i+1);
    
                }
                
                // If all these candidates are in fact expression, return true.
                if (expression(exprCandidate1, true, postfix) && expression(exprCandidate2, true, postfix) 
                && expression(exprCandidate3, true, postfix) && expression(exprCandidate4, true, postfix)){
                    
                    if(!fromChooseCall){
                        // If this choose call is not from another choose call, add overall choose
                        // function to postfix expression.
                        String s = "";
                        for(String x : lexemes){
                            s = s+x;
                        }
                        postfix.add(s);
                    }
                    
                    return true;
                } else return false;
    
            }
            else return false;

        }catch(Exception e){
            return false;
        }
        
    }

    /**
     * Checks whether given list of lexemes obeys the syntax of <factor> in MyLang.
     * @param lexemes A list of lexemes.
     * @param fromChooseCall Whether this function is called from a choose function. If it is so
     * the lexemes are not added to postfix.
     * @param postfix Postfix representation of an expression which can be updated by
     * this function.
     * @return Whether given list of lexemes obeys the syntax of <factor> in MyLang.
     */
    public static boolean factor(ArrayList<String> lexemes, boolean fromChooseCall, Queue<String> postfix){
        // Factor can be an expression between parantheses. This part handles this case.
        if(lexemes.get(0).equals("(") && lexemes.get(lexemes.size()-1).equals(")")){
            ArrayList<String> exprCandidate = new ArrayList<String>(lexemes.subList(1, lexemes.size()-1));
            return expression(exprCandidate, fromChooseCall, postfix);
        }
        // Factor can be a variable,
        else if(id(lexemes, fromChooseCall, postfix)){
            return true;
        }
        // or a number,
        else if(num(lexemes, fromChooseCall, postfix)){
            return true;
        }
        // or a choose function.
        else if(choose(lexemes, fromChooseCall, postfix)){
            return true;
        }
        // If it is none of them, return false.
        else return false;
    }

    /**
     * Checks whether given list of lexemes obeys the syntax of <morefactors> in MyLang.
     * @param lexemes A list of lexemes.
     * @param fromChooseCall Whether this function is called from a choose function. If it is so
     * the lexemes are not added to postfix.
     * @param postfix Postfix representation of an expression which can be updated by
     * this function.
     * @return Whether given list of lexemes obeys the syntax of <morefactors> in MyLang.
     */
    public static boolean morefactors(ArrayList<String> lexemes, boolean fromChooseCall, Queue<String> postfix){

        try{
            // Morefactors can be empty string.
            if(lexemes.size() == 0){
                return true;
            }
            
            // Morefactors can be in the form of '* <factor> <morefactors>' or
            // '/ <factor> <morefactors>'.
            else if(lexemes.get(0).equals("*") || lexemes.get(0).equals("/")){
                ArrayList<String> factorCandidate = new ArrayList<String>(); // This is for our factor candidate
                ArrayList<String> morefactorsCandidate = new ArrayList<String>();   // and this is for morefactors
                                                                                    // candidate.
                int i = 1;  // This is the index of lexemes.
                while(i < lexemes.size()){
                    String lexeme = lexemes.get(i);
                    // We add everything that is before *, / or ( to factor candidate.
                    if(!lexeme.equals("*") && !lexeme.equals("/") && !lexeme.equals("(")){
                        factorCandidate.add(lexeme);
                    }
                    // There can be something between the parantheses. In that case, we should 
                    // take everything between this parantheses and add them to our factor candidate. 
                    else if(lexeme.equals("(")){
                        // This process is done by help of a stack. Everytime we see an opening paranthesis,
                        // we add some mark to our stack. Everytime we see a closing paranthesis, we pop this
                        // mark. When the stack becomes empty, that means no paranthesis left.
                        factorCandidate.add(lexeme);
                        Stack<String> stack = new Stack<String>();
                        stack.push("x");
                        while(!stack.empty()){
                            lexeme = lexemes.get(i+1);
                            if(lexeme.equals("(")){
                                stack.push("x");
                            } else if(lexeme.equals(")")){
                                stack.pop();
                            }
                            i++;
                            factorCandidate.add(lexeme);
                            if(i >= lexemes.size()) return false; // There can be a case that some closing paranthesis
                            // is missed. If we do not check that case, infinite loop occurs. If we reach the end of
                            // our lexemes but still have something in our stack, that means some closing paranthesis is
                            // missed.
                        }
                    } else{
                        break;
                    }
                    i++;
                }

                // Everything left in lexemes is added to the morefactors candidate.
                while(i < lexemes.size()){
                    String lexeme = lexemes.get(i);
                    morefactorsCandidate.add(lexeme);
                    i++;
                }
                
                // If our factor candidate obeys the syntax of factor,
                if(factor(factorCandidate, fromChooseCall, postfix)){
                    if(!fromChooseCall){ // and this function is not called from a choose call,
                        postfix.add(lexemes.get(0));    // add the operation that is first element of
                                                        // lexemes. 
                    }
                    // If our more factors candidate obeys the syntax of morefactors, return true.
                    if(morefactors(morefactorsCandidate, fromChooseCall, postfix)){
                        return true; 
                    } else return false;
                } else return false;
            }
                
            else{
                return false;
            }

        }catch(Exception e){
            return false;
        }

    }

    /**
     * Checks whether given list of lexemes obeys the syntax of <term> in MyLang.
     * @param lexemes A list of lexemes.
     * @param fromChooseCall Whether this function is called from a choose function. If it is so
     * the lexemes are not added to postfix.
     * @param postfix Postfix representation of an expression which can be updated by
     * this function.
     * @return Whether given list of lexemes obeys the syntax of <term> in MyLang.
     */
    public static boolean term(ArrayList<String> lexemes, boolean fromChooseCall, Queue<String> postfix){

        try{
            // Term's syntax is <factor> <morefactors>. We need to seperate factor and morefactor
            ArrayList<String> factorCandidate = new ArrayList<String>();
            ArrayList<String> morefactorsCandidate = new ArrayList<String>();
            int i = 0;
            String lexeme;
            while(i < lexemes.size()){
                lexeme = lexemes.get(i);
                // To do this seperation, we need to add everything until a *, / or ( to our 
                // factor Candidate. 
                if(!lexeme.equals("*") && !lexeme.equals("/") && !lexeme.equals("(")){
                    factorCandidate.add(lexeme);
                }

                // There can be something between the parantheses. In that case, we should 
                // take everything between this parantheses and add them to our factor candidate. 
                else if(lexeme.equals("(")){
                    // This process is done by help of a stack. Everytime we see an opening paranthesis,
                    // we add some mark to our stack. Everytime we see a closing paranthesis, we pop this
                    // mark. When the stack becomes empty, that means no paranthesis left.
                    factorCandidate.add(lexeme);
                    Stack<String> stack = new Stack<String>();
                    stack.push("x");
                        while(!stack.empty()){
                            lexeme = lexemes.get(i+1);
                            if(lexeme.equals("(")){
                                stack.push("x");
                            } else if(lexeme.equals(")")){
                                stack.pop();
                            }
                            i++;
                            factorCandidate.add(lexeme);
                            if(i >= lexemes.size()) return false; // There can be a case that some closing paranthesis
                            // is missed. If we do not check that case, infinite loop occurs. If we reach the end of
                            // our lexemes but still have something in our stack, that means some closing paranthesis is
                            // missed.
                        }
                }
                else break;
                i++;
            }
            // Everything left in lexemes is added to the morefactors candidate.
            while(i < lexemes.size()){
                lexeme = lexemes.get(i);
                morefactorsCandidate.add(lexeme);
                i++;
            }
            
            // If these obey the corresonding rules, return true;
            return factor(factorCandidate, fromChooseCall, postfix) && morefactors(morefactorsCandidate, fromChooseCall, postfix);

        }catch(Exception e){
            return false;
        }

    }

    /**
     * Checks whether given list of lexemes obeys the syntax of <moreterms> in MyLang.
     * @param lexemes A list of lexemes.
     * @param fromChooseCall Whether this function is called from a choose function. If it is so
     * the lexemes are not added to postfix.
     * @param postfix Postfix representation of an expression which can be updated by
     * this function.
     * @return Whether given list of lexemes obeys the syntax of <moreterms> in MyLang.
     */
    public static boolean moreterms(ArrayList<String> lexemes, boolean fromChooseCall, Queue<String> postfix){

        // Moreterms can be empty string.
        if(lexemes.size() == 0){
            return true;
        }

        // Moreterms can be in the form of '+ <term> <moreterms>' or
        // '/ <term> <moreterms>'.      
        else if(lexemes.get(0).equals("+") || lexemes.get(0).equals("-")){
            ArrayList<String> termCandidate = new ArrayList<String>();
            ArrayList<String> moretermsCandidate = new ArrayList<String>();
            int i = 1;
            while(i < lexemes.size()){
                String lexeme = lexemes.get(i);

                // We add everything that is before +, - or ( to factor candidate.
                if(!lexeme.equals("+") && !lexeme.equals("-") && !lexeme.equals("(")){
                    termCandidate.add(lexeme);
                } 
                // There can be something between the parantheses. In that case, we should 
                // take everything between this parantheses and add them to our factor candidate. 
                else if(lexeme.equals("(")){
                    // This process is done by help of a stack. Everytime we see an opening paranthesis,
                    // we add some mark to our stack. Everytime we see a closing paranthesis, we pop this
                    // mark. When the stack becomes empty, that means no paranthesis left.
                    termCandidate.add(lexeme);
                    Stack<String> stack = new Stack<String>();
                    stack.push("x");
                    try{
                        while(!stack.empty()){
                            lexeme = lexemes.get(i+1);
                            if(lexeme.equals("(")){
                                stack.push("x");
                            } else if(lexeme.equals(")")){
                                stack.pop();
                            }
                            i++;
                            termCandidate.add(lexeme);
                            if(i >= lexemes.size()) return false; // There can be a case that some closing paranthesis
                            // is missed. If we do not check that case, infinite loop occurs. If we reach the end of
                            // our lexemes but still have something in our stack, that means some closing paranthesis is
                            // missed.
                        }
                    }catch(Exception e){
                        return false;
                    }
                } 
                else break;
                i++;
            }

            // Everything left in lexemes is added to the moreterms candidate.
            while(i < lexemes.size()){
                String lexeme = lexemes.get(i);
                moretermsCandidate.add(lexeme);
                i++;
            }

            // If our factor candidate obeys the syntax of factor,   
            if(term(termCandidate, fromChooseCall, postfix)){
                if(!fromChooseCall){// and this function is not called from a choose call,
                    postfix.add(lexemes.get(0));    // add the operation that is first element of
                                                    // lexemes. 
                }
                // If our more factors candidate obeys the syntax of morefactors, return true.
                if(moreterms(moretermsCandidate, fromChooseCall, postfix)){
                    return true;
                } else return false;
            } else return false;
        }
        
        else{
            return false;
        }

    }

    /**
     * Checks whether given list of lexemes obeys the syntax of <expression> in MyLang.
     * @param lexemes A list of lexemes.
     * @param fromChooseCall Whether this function is called from a choose function. If it is so
     * the lexemes are not added to postfix.
     * @param postfix Postfix representation of an expression which can be updated by
     * this function.
     * @return Whether given list of lexemes obeys the syntax of <expression> in MyLang.
     */
    public static boolean expression(ArrayList<String> lexemes, boolean fromChooseCall, Queue<String> postfix){
        // Expression needs to be in the form '<term> <moreterms>'. We need to seperate them.
        ArrayList<String> termCandidate = new ArrayList<String>();
        ArrayList<String> moretermsCandidate = new ArrayList<String>();
        int i = 0;
        String lexeme;
        while(i < lexemes.size()){
            // To do this seperation, we need to add everything until a +, - or ( to our 
            // term candidate. 
            lexeme = lexemes.get(i);
            if(!lexeme.equals("+") && !lexeme.equals("-") && !lexeme.equals("(")){
                termCandidate.add(lexeme);
            }

            // There can be something between the parantheses. In that case, we should 
            // take everything between this parantheses and add them to our term candidate. 
            else if(lexeme.equals("(")){
                // This process is done by help of a stack. Everytime we see an opening paranthesis,
                // we add some mark to our stack. Everytime we see a closing paranthesis, we pop this
                // mark. When the stack becomes empty, that means no paranthesis left.
                termCandidate.add(lexeme);
                Stack<String> stack = new Stack<String>();
                stack.push("x");
                try{
                    while(!stack.empty()){
                        lexeme = lexemes.get(i+1);
                        if(lexeme.equals("(")){
                            stack.push("x");
                        } else if(lexeme.equals(")")){
                            stack.pop();
                        }
                        i++;
                        termCandidate.add(lexeme);
                        if(i >= lexemes.size()) return false; // There can be a case that some closing paranthesis
                        // is missed. If we do not check that case, infinite loop occurs. If we reach the end of
                        // our lexemes but still have something in our stack, that means some closing paranthesis is
                        // missed.
                    }
                }catch(Exception e){
                    return false;
                }
            }
            else break;
            i++;
        }
        // Everything left in lexemes is added to the moreterms candidate.
        while(i < lexemes.size()){
            lexeme = lexemes.get(i);
            moretermsCandidate.add(lexeme);
            i++;
        }
        // If these obey the corresonding rules, return true;
        return term(termCandidate, fromChooseCall, postfix) && moreterms(moretermsCandidate, fromChooseCall, postfix);

    }
    
    /**
     * This function processes a single line of lexemes. The line can be an assignment, 
     * while block, etc. If there is a syntax error on the process, then it throws an exception.
     * @param line An array list of lexemes that consists of all lexemes in a line, 
     * other than tabs, blank spaces and comments 
     * @throws CompilerSyntaxException The exception to show there is a syntax error on compilation
     */
    public static void processLine(ArrayList<String> line) throws CompilerSyntaxException {
    	
    	try {
			
    		// This is the main control block for the line.
    		// If the given line is empty, then do nothing with that line.
	    	if(line.isEmpty()) {
	    		// do nothing
	    	}
	    	// If the line has only one element which is a closing brace,
	    	else if(line.get(0).equals("}")) {
	    		// and if the current line is in an if block,
	    		if(inIfCondition) {
	    			// then update IR code,
	    			irCode = irCode.concat("  br label %_ifend" + ifCondCounter + "\n\n_ifend" + ifCondCounter + ":\n");
	    			// and the following line will not be in an if block anymore.
	    			inIfCondition = false;
	    		}
	    		// and if the current line is in a while block,
	    		else if(inWhileCondition) {
	    			// then update IR code,
	    			irCode = irCode.concat("  br label %_whcond" + whileCondCounter + "\n\n_whend" + whileCondCounter + ":\n");
	    			// and the following line will not be in a while block anymore.
	    			inWhileCondition = false;
	    		}
	    		// or if the current line is neither in an if block nor while block,
	    		// then throw exception as follows.
	    		else {
	    			throw new CompilerSyntaxException("\"}\" symbol is not expected");
	    		}
	    	}
	    	// If the second lexeme is an equality sign,
	    	else if(line.get(1).equals("=")) {
	    		// and the first element is a variable as expected, (if not, throw exception)
	    		if(isVar(line.get(0))){
	        		
	            	Queue<String> postfix = new LinkedList<String>();
	        		
	            	String var = line.get(0);
	        		line.remove(0);
	        		line.remove(0);
	        		// and if RHS is an expression, update IR code. Otherwise, throw exception.
	        		if(expression(line, false, postfix)) {
	        			int temp = printIrCodeOfExpression(postfix);
	        			irCode = irCode.concat("  store i32 %_" + temp + ", i32* %_" + var +"\n");
	        		}
	        		else {
	        			throw new CompilerSyntaxException("right hand side is not an expression");
	        		}
	    		}
	            else{
	            	throw new CompilerSyntaxException("left hand side is not a variable");
	            }
	    		
	    	}
	    	
	    	// If the line is an arraylist as follows:
	    	// ["print", "(", ... , ")"]
	    	// (implicitly, if the line is a print statement)
	    	else if(line.get(0).equals("print")&&line.get(1).equals("(")&&line.get(line.size()-1).equals(")")){
	    		
	    		// line is now reduced from "print(<expr>)" to "<expr>", so that we can continue processing.
	    		line.remove(0);
	    		line.remove(0);
	    		line.remove(line.size()-1);
	    		
	    		// and if the reduced line is a proper expression, then update IR code.
	    		Queue<String> postfix = new LinkedList<String>();
	    		if (expression(line, false, postfix)) {
					int t = printIrCodeOfExpression(postfix);
					irCode = irCode.concat("  call i32 (i8*, ...)* @printf(i8* getelementptr ([4 x i8]* @print.str, i32 0, i32 0), i32 %_" + t + ")\n");
				} 
	    		else {
	    			throw new CompilerSyntaxException("parameter of the print function is not an expression");
				}
	    	}
	    	
	    	// If the line is an array list as follows:
	    	// ["while", "(", ... , ")", "{"]
	    	// (implicitly, if the line is the beginning of a while block)
	    	else if(line.get(0).equals("while")&&line.get(1).equals("(")&&line.get(line.size()-1).equals("{")&&line.get(line.size()-2).equals(")")) {
	    		
	    		// We control if the newly created block is in another if or while block. 
	    		// If in an "if" or a "while" block, then throw exception.
	    		if (inIfCondition) {
					throw new CompilerSyntaxException("\"if\" block in an \"while\" block is not supported");
				}
	    		if (inWhileCondition) {
	    			throw new CompilerSyntaxException("\"while\" block in an \"while\" block is not supported");
	    		}
	    		
	    		// Line is reduced to an expression, so that we continue processing the expression.
	    		line.remove(0);
	    		line.remove(0);
	    		line.remove(line.size()-1);
	    		line.remove(line.size()-1);
	    		
	    		Queue<String> postfix = new LinkedList<String>();
	    		
	    		// If the condition of the while block is an expression, then update IR code.
	    		if (expression(line, false, postfix)) {
	    			irCode = irCode.concat("  br label %_whcond"  + ++whileCondCounter +  "\n");
	    			irCode = irCode.concat("\n_whcond"  + whileCondCounter + ":\n");
	    			
	    			inWhileCondition = true;
	    			
					int t = printIrCodeOfExpression(postfix);
					
					irCode = irCode.concat("  %_" + ++temp + " = icmp ne i32 %_" + t + ", 0\n");
					irCode = irCode.concat("  br i1 %_" + temp + ", label %_whbody" + whileCondCounter + ", label %_whend" + whileCondCounter + "\n");
					irCode = irCode.concat("\n_whbody" + whileCondCounter + ":\n");
					
				}
	    		else {
	    			throw new CompilerSyntaxException("condition of while statement is not an expression");
				}
	    	}
	    	// If the line is an array list as follows:
	    	// ["if", "(", ... , ")", "{"]
	    	// (implicitly, if the line is the beginning of an if block)
	    	else if(line.get(0).equals("if")&&line.get(1).equals("(")&&line.get(line.size()-1).equals("{")&&line.get(line.size()-2).equals(")")) {
	    		
	    		// We control if the newly created block is in another if or while block. 
	    		// If in an "if" or a "while" block, then throw exception.
	    		if (inIfCondition) {
	    			throw new CompilerSyntaxException("\"if\" block in an \"if\" block is not supported");
	    		}
	    		if (inWhileCondition) {
	    			throw new CompilerSyntaxException("\"while\" block in an \"if\" block is not supported");
	    		}
	    		
	    		// Line is reduced from "if(<expr>){" to "<expr>" so that we can easily process the expression.
	    		line.remove(0);
	    		line.remove(0);
	    		line.remove(line.size()-1);
	    		line.remove(line.size()-1);
	    		
	    		Queue<String> postfix = new LinkedList<String>();
	    		
	    		// If the condition of if block is an expression, then update IR code.
	    		if (expression(line, false, postfix)) {
	    			++ifCondCounter;
	    			inIfCondition = true;
	    			
					int t = printIrCodeOfExpression(postfix);
					
					irCode = irCode.concat("  %_" + ++temp + " = icmp ne i32 %_" + t + ", 0\n");
					irCode = irCode.concat("  br i1 %_" + temp + ", label %_ifbody" + ifCondCounter + ", label %_ifend" + ifCondCounter + "\n");
					irCode = irCode.concat("\n_ifbody" + ifCondCounter + ":\n");
					
				}
	    		else {
	    			throw new CompilerSyntaxException("condition of if statement is not an expression");
				}
	    	}
	    	
	    	// If the line is none of them, then the line is not supposed to be defined in our language. 
	    	// Because the line can be an empty string, a single closing brace, an assignment, 
	    	// the beginning of a while block or the beginning of an if block. If the line is none of
	    	// these, then throw exception.
	    	else {
	    		throw new CompilerSyntaxException("Line can be empty, an assignment, an if statement, a while statement or a curly bracket");
			}
    	}
    	// On the try block, we have <ArrayList>.get(int) operation which throws IndexOutOfBoundsException. 
    	// If the line is as follows:
    	// ")"
    	// then first two if conditions are passed by but the third condition checks 
    	// .....&&line.get(1).......
    	// which throws IndexOutOfBoundsException.
    	catch (IndexOutOfBoundsException e) {
    		throw new CompilerSyntaxException("Line can be empty, an assignment, an if statement, a while statement or a curly bracket");
    	}
    }
}

/**
 * This class is a simple Exception class that we have created in order to 
 * easily process main class by try catch blocks.
 * @author Halil Burak Pala
 * @author Huseyin Turker Erdem
 */
class CompilerSyntaxException extends Exception{
	public CompilerSyntaxException(String s) {
		super(s);
	}
}