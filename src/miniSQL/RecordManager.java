package miniSQL;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecordManager {

    public static boolean satisfy(Table table, Record record, ArrayList<Condition> conditionList) {
        for (Condition condition : conditionList) {
            Map<Column, Object> map = unpack(table, record);
            Column column = table.getColumn(condition.columnName);
            if (column.type.equals("int")) {
                int num = (int) map.get(column);
                int value = Integer.parseInt(condition.value);
                if (judge(num, condition.operator, value) == false)
                    return false;
            } else if (column.type.equals("float")) {
                float f = (float) map.get(column);
                float value = Float.parseFloat(condition.value);
                if (judge(f, condition.operator, value) == false)
                    return false;
            } else {
                String string = (String) map.get(column);
                String value = condition.value;
                if (judge(string, condition.operator, value) == false)
                    return false;
            }
        }
        return true;
    }

    // !=  <  >  =  <=  >=
    private static boolean judge(int num, String operator, int value) {
        if (operator.equals("!=") || operator.equals("<>"))
            return num != value;
        else if (operator.equals("<"))
            return num < value;
        else if (operator.equals(">"))
            return num > value;
        else if (operator.equals("="))
            return num == value;
        else if (operator.equals("<="))
            return num <= value;
        else if (operator.equals(">="))
            return num >= value;
        else
            return false;
    }

    // !=  <  >  =  <=  >=
    private static boolean judge(float f, String operator, float value) {
        if (operator.equals("!=") || operator.equals("<>"))
            return f != value;
        else if (operator.equals("<"))
            return f < value;
        else if (operator.equals(">"))
            return f > value;
        else if (operator.equals("="))
            return f == value;
        else if (operator.equals("<="))
            return f <= value;
        else if (operator.equals(">="))
            return f >= value;
        else
            return false;
    }

    // !=  <  >  =  <=  >=
    private static boolean judge(String string, String operator, String value) {
        if (operator.equals("!=") || operator.equals("<>"))
            return !string.equals(value);
        else if (operator.equals("<"))
            return string.compareTo(value) < 0;
        else if (operator.equals(">"))
            return string.compareTo(value) > 0;
        else if (operator.equals("="))
            return string.equals(value);
        else if (operator.equals("<="))
            return string.compareTo(value) <= 0;
        else if (operator.equals(">="))
            return string.compareTo(value) >= 0;
        else
            return false;
    }

    public static Record pack(Table table, ArrayList<String> list) {
        Record record = new Record(table.recordSize);
        for (int i = 0; i < table.columnNumber; i++) {
            String type = table.columnList.get(i).type;
            if (type.equals("int")) {
                record.append(Integer.parseInt(list.get(i)));
            } else if (type.equals("float")) {
                record.append(Float.parseFloat(list.get(i)));
            } else // String
            {
                record.append(list.get(i), table.columnList.get(i).size);
            }
        }
        return record;
    }

    public static Map<Column, Object> unpack(Table table, Record record) {
        Map<Column, Object> map = new HashMap<Column, Object>();
        int shift = 0;
        for (Column column : table.columnList) {
            String type = column.type;
            if (type.equals("int")) {
                int num = 0x0FF & (record.data[shift + 0]);
                num <<= 8;
                num += 0xFF & (record.data[shift + 1]);
                num <<= 8;
                num += 0xFF & (record.data[shift + 2]);
                num <<= 8;
                num += 0xFF & (record.data[shift + 3]);
                shift += 4;
                map.put(column, num);
            } else if (type.equals("float")) {
                int intBits = 0x0FF & (record.data[shift + 0]);
                intBits <<= 8;
                intBits += 0x0FF & (record.data[shift + 1]);
                intBits <<= 8;
                intBits += 0x0FF & (record.data[shift + 2]);
                intBits <<= 8;
                intBits += 0x0FF & (record.data[shift + 3]);
                float f = Float.intBitsToFloat(intBits);
                shift += 4;
                map.put(column, f);
            } else // char(n)
            {
                int blankByte = 0;
                while (blankByte < column.size && record.data[shift] == (byte) 0) {
                    shift++;
                    blankByte++;
                }
                byte[] bytes = new byte[column.size - blankByte];
                for (int j = 0; j < bytes.length; j++) {
                    bytes[j] = record.data[shift];
                    shift++;
                }
                try {
                    String string = new String(bytes, "UTF8");
                    map.put(column, string);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    static void printRecord(Table table, Record record) {
        int shift = 0;
        for (int i = 0; i < table.columnNumber; i++) {
            Column column = table.columnList.get(i);
            String type = column.type;
            if (type.equals("int")) {
                int num = 0x0FF & (record.data[shift + 0]);
                num <<= 8;
                num += 0xFF & (record.data[shift + 1]);
                num <<= 8;
                num += 0xFF & (record.data[shift + 2]);
                num <<= 8;
                num += 0xFF & (record.data[shift + 3]);
                System.out.printf("%-10d", num);
                shift += 4;
            } else if (type.equals("float")) {
                int intBits = 0x0FF & (record.data[shift + 0]);
                intBits <<= 8;
                intBits += 0x0FF & (record.data[shift + 1]);
                intBits <<= 8;
                intBits += 0x0FF & (record.data[shift + 2]);
                intBits <<= 8;
                intBits += 0x0FF & (record.data[shift + 3]);
                float f = Float.intBitsToFloat(intBits);
                System.out.printf("%-10f", f);
                shift += 4;
            } else // char(n)
            {
                int blankByte = 0;
                while (blankByte < table.columnList.get(i).size) {
                    if (record.data[shift] == (byte) 0) {
                        shift++;
                        blankByte++;
                    } else
                        break;
                }
                int columnSize = table.columnList.get(i).size;
                byte[] bytes = new byte[columnSize - blankByte];
                for (int j = 0; j < bytes.length; j++) {
                    bytes[j] = record.data[shift];
                    shift++;
                }
                try {
                    String string = new String(bytes, "UTF8");
                    System.out.printf("%-" + (Table.max(columnSize, column.columnName.length()) + 1) + "s", string);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println();
    }

//	public static void main(String[] args)
//	{
//		// for test
//        Properties props = System.getProperties();
//        Enumeration<?> names = props.propertyNames();
//        while ( names.hasMoreElements() ) 
//        {
//        	String key = (String)names.nextElement();
//        	String value = props.getProperty(key);
//        	System.out.println(key+":"+value);
//        }
//	}
}

class Record implements Serializable {
    private static final long serialVersionUID = 7874194821689687578L;
    int currentSize;
    byte[] data;

    Record(int recordSize) {
        currentSize = 0;
        data = new byte[recordSize];
    }

    void append(String string, int size) {
        for (int i = 0; i < size - string.length(); i++)
            append((byte) 0);
        try {
            append(string.getBytes("UTF8"), string.length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    void append(int num) {
        append((byte) ((0xff000000 & num) >> 24));
        append((byte) ((0xff0000 & num) >> 16));
        append((byte) ((0xff00 & num) >> 8));
        append((byte) (0xff & num));
    }

    void append(float f) {
        int num = Float.floatToIntBits(f);
        append(num);
    }

    void append(byte b) {
        data[currentSize++] = b;
    }

    void append(byte[] b, int n) {
        for (int i = 0; i < n; i++)
            append(b[i]);
    }

    void append(byte[] b) {
        for (int i = 0; i < data.length; i++)
            append(b[i]);
    }
}