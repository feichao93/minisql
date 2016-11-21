package miniSQL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import static java.text.MessageFormat.format;

public class Interpreter {
    public static void main(String[] args) throws IOException {
        String input;
        Instruction instruction;
        Error error;
        boolean b;
        Scanner in = new Scanner(System.in);
        while (true) {
            input = getUserInput(in);
            instruction = new Instruction(input);
            error = new Error("");
            Parser.parseInput(input, instruction, error);

//			instruction.print();
            if (instruction.opCode == OpCode.error)
                error.print();
            else if (instruction.opCode == OpCode.quit) {
                runInstruction(instruction, error);
                break;
            } else if (instruction.opCode == OpCode.execfile) {
                File file = new File(CatalogManager.basePath + instruction.data.get("fileName"));
                try {
                    FileReader fileReader = new FileReader(file);
                    Scanner fileIn = new Scanner(fileReader);
                    while (true) {
                        input = getUserInput(fileIn);
                        Parser.parseInput(input, instruction, error);
                        if (instruction.opCode == OpCode.error)
                            error.print();
                        else {
//							System.out.println("running  " + input);
                            b = runInstruction(instruction, error);
                            if (!b) {
                                error.print();
                            } else {
//								System.out.println("command execute successfully");
                            }
                        }
                    }
                } catch (NoSuchElementException e) {

                } catch (FileNotFoundException e) {
                    System.out.println("file:" + instruction.data.get("fileName") + " can not open.");
                }
            } else // start to run instruction
            {
                b = runInstruction(instruction, error);
                if (b == false)
                    error.print();
                else
                    System.out.println("command execute successfully");
//				System.out.print(BufferManager.printPool());
            }
        }
        System.out.print("Interpreter terminated");
        in.close();
    }

    private static String getUserInput(Scanner in) {
        String result = "";
        while (true) {
            result += in.nextLine();
            if (result.trim().endsWith(";"))
                break;
            else
                result += " ";
        }
        return result;
    }

    private static boolean runInstruction(Instruction instruction, Error error) throws IOException {
        if (instruction.opCode == OpCode.createTable)
            return CatalogManager.createTable(instruction, error);
        else if (instruction.opCode == OpCode.dropTable)
            return CatalogManager.dropTable(instruction, error);
        else if (instruction.opCode == OpCode.createIndex)
            return CatalogManager.createIndex(instruction, error);
        else if (instruction.opCode == OpCode.dropIndex)
            return CatalogManager.dorpIndex(instruction, error);
        else if (instruction.opCode == OpCode.insert)
            return CatalogManager.insert(instruction, error);
        else if (instruction.opCode == OpCode.selectWithoutCondition)
            return CatalogManager.selectWithoutCondition(instruction, error);
        else if (instruction.opCode == OpCode.selectWithCondition)
            return CatalogManager.selectWithCondition(instruction, error);
        else if (instruction.opCode == OpCode.deleteWithoutCondition)
            return CatalogManager.deleteWithoutCondition(instruction, error);
        else if (instruction.opCode == OpCode.deleteWithCondition)
            return CatalogManager.deleteWithCondition(instruction, error);
        else if (instruction.opCode == OpCode.dump)
            return CatalogManager.dump();
        else if (instruction.opCode == OpCode.load)
            return CatalogManager.load(error);
        else if (instruction.opCode == OpCode.show)
            return CatalogManager.show();
        else if (instruction.opCode == OpCode.quit)
            return CatalogManager.quit();
        else
            return true;
    }
}

class Instruction {
    String input;
    boolean parseDone;
    int opCode;
    Map<String, String> data;
    ArrayList<Condition> conditionList;
    ArrayList<Column> columnList;
    ArrayList<String> valueList;

    Instruction(String input) {
        this.input = input;
        opCode = 0;
        parseDone = false;
        data = new HashMap<String, String>();
    }

    void print() {
        System.out.print(this);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(format(
                "Instruction:\nopCode is {0}({1})\n\tparseDone flag is {2}\n\tdata is as follows: \n",
                OpCode.getOperationName(opCode), opCode, parseDone));

        for (String s : data.keySet()) {
            result.append(format("\t\t{0}:{1}\n", s, data.get(s)));
        }
        if (conditionList != null) {
            result.append(format("conditionList: {0}\n", conditionList));
        }
        if (columnList != null) {
            result.append(format("columnList: {0}\n", columnList));
        }
        if (valueList != null) {
            result.append(format("valueList: {0}\n", valueList));
        }
        return result.toString();
    }
}

class Error {
    String info;

    Error(String info) {
        this.info = info;
    }

    void print() {
        System.out.println("Error: ");
        System.out.println("\t" + info);
    }

    public void setInfo(String info) {
        this.info = info;
    }
}

class Parser {
    static void parseInput(String input, Instruction instruction, Error error) {

        Str s = new Str(input);
        String token;
        token = getToken(s);
        if (token.equals("create")) {
            if (expect(s, "table"))
                parseCreateTable(s, instruction, error);
            else if (expect(s, "index"))
                parseCreateIndex(s, instruction, error);
            else {
                instruction.opCode = OpCode.error;
                error.setInfo("expected `table' or `index' after `create'.");
            }
        } else if (token.equals("drop")) {
            if (expect(s, "table"))
                parseDropTable(s, instruction, error);
            else if (expect(s, "index"))
                parseDropIndex(s, instruction, error);
            else {
                instruction.opCode = OpCode.error;
                error.setInfo("expected `table' or `index' after `drop'");
            }
        } else if (token.equals("insert")) {
            parseInsert(s, instruction, error);
        } else if (token.equals("select")) {
            parseSelect(s, instruction, error);
        } else if (token.equals("delete")) {
            parseDelete(s, instruction, error);
        } else if (token.equals("quit")) {
            parseQuit(s, instruction, error);
        } else if (token.equals("execfile")) {
            parseExecfile(s, instruction, error);
        } else if (token.equals("dump")) {
            parseDump(s, instruction, error);
        } else if (token.equals("load")) {
            parseLoad(s, instruction, error);
        } else if (token.equals("show")) {
            parseShow(s, instruction, error);
        } else // unknown command
        {
            parseUnkownCmd(s, instruction, error);
        }
    }

    static void parseCreateTable(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.createTable;
        instruction.columnList = new ArrayList<Column>();
        if (expect(s, ""))  // tableName is empty
        {
            instruction.opCode = OpCode.error;
            error.setInfo("table name is missing");
        } else {
            instruction.data.put("tableName", getToken(s));
            instruction.data.put("tableContent", s.s.substring(s.shift));
            if (!expectChar(s, '('))  // `(' is missing
            {
                instruction.opCode = OpCode.error;
                error.setInfo("expected `(' after table name");
            } else {
                // parse columns
                while (true) {
                    if (!parseOneColumn(s, instruction, error))
                        break;
                }
                if (!expectChar(s, ')')) {
                    instruction.opCode = OpCode.error;
//					error.setInfo("expected `)' after column info");
                    error.setInfo("error... but it is hard to tell you where is wrong.");
                } else if (!getString(s).equals(";")) {
                    instruction.opCode = OpCode.error;
                    error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
                } else
                    instruction.parseDone = true;
            }
        }
    }

    private static boolean parseOneColumn(Str s, Instruction instruction, Error error) {
        //  columnName type [unique][,]
        //  primary key (primaryKeyName),
        skipBlankChar(s);
        int temp = s.shift;
        String columnName, type, primaryKey;
        boolean unique = false;
        if (expect(s, "")) // columnName is empty
        {
            s.shift = temp;
            return false;
        } else if (expect(s, "primary") && expect(s, "key") && expectChar(s, '(')) {
            if (expect(s, "")) // key name is missing
            {
                s.shift = temp;
                return false;
            } else {
                primaryKey = getToken(s);
                if (expectChar(s, ')')) {
                    instruction.data.put("primaryKey", primaryKey);
                    expectChar(s, ',');
                    return true;
                } else // `)' is missing
                {
                    s.shift = temp;
                    return false;
                }
            }
        } else {
            columnName = getToken(s);
            type = getType(s);
            if (type == null) {
                s.shift = temp;
                return false;
            } else  // type is OK
            {
                if (expect(s, "unique"))
                    unique = true;
                expectChar(s, ',');
                instruction.columnList.add(new Column(columnName, type, unique));
                return true;
            }
        }
    }

    private static void parseDropTable(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.dropTable;
        if (expect(s, "")) {
            instruction.opCode = OpCode.error;
            error.setInfo("table name is missing");
        } else {
            instruction.data.put("tableName", getToken(s));
            if (!getString(s).equals(";")) {
                instruction.opCode = OpCode.error;
                error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
            } else
                instruction.parseDone = true;
        }
    }

    private static void parseCreateIndex(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.createIndex;
        if (expect(s, "")) // indexName is empty
        {
            instruction.opCode = OpCode.error;
            error.setInfo("index name is missing");
        } else {
            instruction.data.put("indexName", getToken(s));
            if (!expect(s, "on"))    // `on' is missing
            {
                instruction.opCode = OpCode.error;
                error.setInfo("expected `on' after the index name");
            } else if (expect(s, ""))    // table name is missing
            {
                instruction.opCode = OpCode.error;
                error.setInfo("expected table name after the `on' ");
            } else {
                instruction.data.put("tableName", getToken(s));
                if (!expectChar(s, '(')) {
                    instruction.opCode = OpCode.error;
                    error.setInfo("expected `(' after table name ");
                } else if (expect(s, ""))    // column name is missing
                {
                    instruction.opCode = OpCode.error;
                    error.setInfo("expected column name after `(' ");
                } else {
                    instruction.data.put("columnName", getToken(s));
                    if (!expectChar(s, ')'))  // `)' is missing
                    {
                        instruction.opCode = OpCode.error;
                        error.setInfo("expected `)' after column name ");
                    } else {
                        if (!getString(s).equals(";")) {
                            instruction.opCode = OpCode.error;
                            error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
                        } else
                            instruction.parseDone = true;
                    }
                }

            }
        }
    }

    private static void parseDropIndex(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.dropIndex;
        if (expect(s, "")) {
            instruction.opCode = OpCode.error;
            error.setInfo("index name is missing");
        } else {
            instruction.data.put("indexName", getToken(s));
            if (!getString(s).equals(";")) {
                instruction.opCode = OpCode.error;
                error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
            } else
                instruction.parseDone = true;
        }
    }

    private static void parseInsert(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.insert;
        instruction.valueList = new ArrayList<String>();
        if (!expect(s, "into")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected `from' after `insert'");
        } else if (expect(s, "")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected table name after the `from' ");
        } else {
            instruction.data.put("tableName", getToken(s));
            if (!expect(s, "values")) {
                instruction.opCode = OpCode.error;
                error.setInfo("expected `values'");
            } else if (!expectChar(s, '('))  // `(' is missing
            {
                instruction.opCode = OpCode.error;
                error.setInfo("expected `(' after table name");
            } else {
                while (true) {
                    String value = getValue(s);
                    instruction.valueList.add(value);
                    if (!expectChar(s, ',')) {
                        break;
                    }
                }
                if (!expectChar(s, ')')) { // `)' is missing
                    instruction.opCode = OpCode.error;
                    error.setInfo("expected `)' in `values(...)'");
                } else {
                    if (!getString(s).equals(";")) {
                        instruction.opCode = OpCode.error;
                        error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
                    } else
                        instruction.parseDone = true;
                }
            }
        }
    }

    private static void parseSelect(Str s, Instruction instruction, Error error) {
        if (expect(s, "")) // column is missing
        {
            instruction.opCode = OpCode.error;
            error.setInfo("column name or * is missing");
        } else {
            instruction.data.put("columnName", getToken(s));
            if (!expect(s, "from")) {
                instruction.opCode = OpCode.error;
                error.setInfo("expected `from' after the column name");
            } else if (expect(s, "")) // table name is missing
            {
                instruction.opCode = OpCode.error;
                error.setInfo("expected table name after the `from' ");
            } else {
                instruction.data.put("tableName", getToken(s));
                if (expectString(s, ";")) {
                    instruction.opCode = OpCode.selectWithoutCondition;
                    instruction.parseDone = true;
                } else if (!expect(s, "where")) {
                    instruction.opCode = OpCode.error;
                    error.setInfo("expected `where' or ';' after table name");
                } else {
                    instruction.data.put("conditions", s.s.substring(s.shift));
                    instruction.conditionList = new ArrayList<Condition>();
                    while (true) {
                        if (!parseOneCondition(s, instruction, error))
                            break;
                        else if (expect(s, "and"))
                            continue;
                        else
                            break;
                    }
                    if (!getString(s).equals(";")) {
                        instruction.opCode = OpCode.error;
                        error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
                    } else {
                        instruction.opCode = OpCode.selectWithCondition;
                        instruction.parseDone = true;
                    }
                }
            }
        }
    }

    private static void parseDelete(Str s, Instruction instruction, Error error) {
        if (!expect(s, "from")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected `from' after `delete'");
        } else if (expect(s, "")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected table name after the `from' ");
        } else {
            instruction.data.put("tableName", getToken(s));
            if (expectString(s, ";")) {
                instruction.opCode = OpCode.deleteWithoutCondition;
                instruction.parseDone = true;
            } else if (!expect(s, "where")) {
                instruction.opCode = OpCode.error;
                error.setInfo("expected `where' or `)' after table name");
            } else {
                instruction.data.put("conditions", s.s.substring(s.shift));
                instruction.conditionList = new ArrayList<Condition>();
                while (true) {
                    if (!parseOneCondition(s, instruction, error))
                        break;
                    else if (expect(s, "and"))
                        continue;
                    else
                        break;
                }
                if (!getString(s).equals(";")) {
                    instruction.opCode = OpCode.error;
                    error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
                } else {
                    instruction.opCode = OpCode.deleteWithCondition;
                    instruction.parseDone = true;
                }
            }
        }
    }

    private static boolean parseOneCondition(Str s, Instruction instruction, Error error)
    // column operator value
    {
        skipBlankChar(s);
        int temp = s.shift;
        String columnName, operator, value;
        if (expect(s, "")) // columnName is empty
        {
            s.shift = temp;
            return false;
        } else {
            columnName = getToken(s);
            operator = getOperator(s);
            value = getValue(s);
            if (operator.equals("")) {
                s.shift = temp;
                return false;
            } else if (value.equals("")) {
                s.shift = temp;
                return false;
            } else {
                instruction.conditionList.add(new Condition(columnName, operator, value));
                return true;
            }
        }
    }

    private static void parseQuit(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.quit;
        if (!getString(s).equals(";")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
        } else
            instruction.parseDone = true;
    }

    private static void parseDump(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.dump;
        if (!getString(s).equals(";")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
        } else
            instruction.parseDone = true;
    }

    private static void parseLoad(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.load;
        if (!getString(s).equals(";")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
        } else
            instruction.parseDone = true;
    }

    private static void parseShow(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.show;
        if (!getString(s).equals(";")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
        } else
            instruction.parseDone = true;
    }

    private static void parseExecfile(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.execfile;
        String fileName = getFileName(s);
        System.out.println("filename is " + fileName);
        if (fileName == null) {
            instruction.opCode = OpCode.error;
            error.setInfo("file name is invalid");
        } else if (!getString(s).equals(";")) {
            instruction.opCode = OpCode.error;
            error.setInfo("expected an ';' to finish input. But ';' is missing or something is there after ';'.");
        } else {
            instruction.data.put("fileName", fileName);
            instruction.parseDone = true;
        }
    }

    private static void parseUnkownCmd(Str s, Instruction instruction, Error error) {
        instruction.opCode = OpCode.error;
        instruction.parseDone = true;
        error.setInfo("unknown command");
    }

    private static String getToken(Str s) {
//		if(s.shift >= s.s.length())
//			return "";
        skipBlankChar(s);

        int temp = s.shift;
        while (s.shift < s.s.length()) {
            char c = s.s.charAt(s.shift);
            boolean b = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '*';
            // b = b && (s.shift < s.s.length());
            if (!b)
                break;
            else
                s.shift++;
        }
        return s.s.substring(temp, s.shift);
    }

    private static String getValue(Str s) {
        skipBlankChar(s);
        int temp = s.shift;
        String result;
        if (s.s.charAt(s.shift) == '"') {
            s.shift++;
            while (s.shift < s.s.length() && s.s.charAt(s.shift) != '"') {
                s.shift++;
            }
            if (s.shift == s.s.length()) {
                s.shift = temp;
                return "";
            }
            result = s.s.substring(temp + 1, s.shift);
            s.shift++;
            return result;
        } else if (s.s.charAt(s.shift) == '\'') {
            s.shift++;
            while (s.shift < s.s.length() && s.s.charAt(s.shift) != '\'') {
                s.shift++;
            }
            if (s.shift == s.s.length()) {
                s.shift = temp;
                return "";
            }
            result = s.s.substring(temp + 1, s.shift);
            s.shift++;
            return result;
        } else {
            while (s.shift < s.s.length()) {
                char c = s.s.charAt(s.shift);
                boolean b = (c >= '0' && c <= '9') || c == '.' || c == '-';
                if (!b)
                    break;
                else
                    s.shift++;
            }
            return s.s.substring(temp, s.shift);
        }
    }

    private static boolean expect(Str s, String exp) {
        int temp = s.shift;
        String token = getToken(s);
        if (token.equals(exp)) {
            return true;
        } else {
            s.shift = temp;
            return false;
        }
    }

    private static String getString(Str s) {
        skipBlankChar(s);
        int temp = s.shift;
        while (s.shift < s.s.length() && (s.s.charAt(s.shift) != ' ' && s.s.charAt(s.shift) != '\t'))
            s.shift++;

        return s.s.substring(temp, s.shift);
    }

    private static boolean expectString(Str s, String exp) {
        int temp = s.shift;
        String token = getString(s);
        if (token.equals(exp))
            return true;
        else {
            s.shift = temp;
            return false;
        }
    }

    private static boolean expectChar(Str s, char c) {
        while (s.shift < s.s.length() && (s.s.charAt(s.shift) == ' ' || s.s.charAt(s.shift) == '\t'))
            s.shift++;
        if (s.shift < s.s.length() && s.s.charAt(s.shift) != c)
            return false;
        else {
            s.shift++;
            return true;
        }
    }

    private static String getOperator(Str s) {
        skipBlankChar(s);
        int temp = s.shift;
        while (s.shift < s.s.length()) {
            char c = s.s.charAt(s.shift);
            boolean b = (c == '=' || c == '<' || c == '>' || c == '!');
            if (!b)
                break;
            else
                s.shift++;
        }
        String operator = s.s.substring(temp, s.shift);
        if (operator.equals("!=") || operator.equals("<") || operator.equals(">") || operator.equals("=")
                || operator.equals("<=") || operator.equals(">=") || operator.equals("<>"))
            return operator;
        else {
            s.shift = temp;
            return "";
        }
    }

    private static void skipBlankChar(Str s) {
        while (s.shift < s.s.length() && (s.s.charAt(s.shift) == ' ' || s.s.charAt(s.shift) == '\t'))
            s.shift++;
    }

    private static String getType(Str s) {
        skipBlankChar(s);
        int temp = s.shift;
        String numStr;
        int num;
        if (expect(s, "int"))
            return "int";
        else if (expect(s, "float"))
            return "float";
        else if (expect(s, "char")) {
            if (s.s.charAt(s.shift) != '(') {
                s.shift = temp;
                return null;
            } else {
                s.shift++;
                numStr = getToken(s);
                num = Integer.parseInt(numStr);
                if (!(num >= 1 && num <= 255)) {
                    s.shift = temp;
                    return null;
                } else if (s.s.charAt(s.shift) != ')') {
                    s.shift = temp;
                    return null;
                } else // it is ')'
                {
                    s.shift++;
                    return s.s.substring(temp, s.shift);
                }
            }

        } else {
            s.shift = temp;
            return null;
        }
    }

    private static String getFileName(Str s) {
        skipBlankChar(s);
        int temp = s.shift;
        while (s.shift < s.s.length()) {
            char c = s.s.charAt(s.shift);
            boolean b = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '*' || c == '.' || c == '_';
            if (!b)
                break;
            else
                s.shift++;
        }

        return s.s.substring(temp, s.shift);
    }


//	static boolean isNextStringEqualTo(Str s, String exp)
//	{
//		int temp = s.shift;
//		boolean b = getString(s).equals(exp);
//		s.shift = temp;
//		return b;
//	}
}

class Str {
    String s;
    int shift;

    Str(String input) {
        s = input;
        shift = 0;
    }
}

class OpCode {
    final static int createTable = 1;
    final static int dropTable = 2;
    final static int createIndex = 3;
    final static int dropIndex = 4;
    final static int insert = 5;
    final static int selectWithoutCondition = 6;
    final static int selectWithCondition = 7;
    final static int deleteWithoutCondition = 8;
    final static int deleteWithCondition = 9;
    final static int quit = 10;
    final static int execfile = 11;
    final static int error = 12;
    final static int dump = 13;
    final static int load = 14;
    final static int show = 15;

    static String getOperationName(int opCode) {
        if (opCode == createTable)
            return "createTable";
        else if (opCode == dropTable)
            return "dropTable";
        else if (opCode == createIndex)
            return "createIndex";
        else if (opCode == dropIndex)
            return "dropIndex";
        else if (opCode == insert)
            return "insert";
        else if (opCode == selectWithoutCondition)
            return "selectWithoutCondition";
        else if (opCode == selectWithCondition)
            return "selectWithCondtion";
        else if (opCode == deleteWithoutCondition)
            return "deleteWithoutCondition";
        else if (opCode == deleteWithCondition)
            return "deleteWithCondition";
        else if (opCode == quit)
            return "quit";
        else if (opCode == execfile)
            return "execfile";
        else if (opCode == error)
            return "error";
        else if (opCode == dump)
            return "dump";
        else if (opCode == load)
            return "load";
        else if (opCode == show)
            return "show";
        else
            return "badOpCode";
    }
}

class Condition {
    String columnName;
    String operator;
    String value;

    Condition() {
        columnName = "";
        operator = "";
        value = "";
    }

    Condition(String columnName, String operator, String value) {
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
    }

    public String toString() {
        return "Condition: \n" + "\n\tcolumnName: "
                + columnName + "\n\toperator: " + operator + "\n\tvalue: " + value + "\n";
    }
}
