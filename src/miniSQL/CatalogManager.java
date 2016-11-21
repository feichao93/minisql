package miniSQL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CatalogManager {
	final static int blockSize = 2048;
	final static String basePath = "C:/Users/sfc/Desktop/data/";
	
	static ArrayList<Table> tableList = new ArrayList<Table>();
	static ArrayList<Index> indexList = new ArrayList<Index>();
	
	static boolean createTable(Instruction instruction, Error error)
	{
		/* data:
		 *   primaryKey: String
		 *   tableName: String
		 * columnList:  ArrayList<Column>
		 *   e.x. [id int, name char(10), height float, primary key(id)]
		 */
		/* possible errors:
		 * 1) table already exist  
		 * 2) column not valid  
		 * 3) primary key is not a column of table
		 */
		String tableName = instruction.data.get("tableName");
		if(hasTable(tableName))
		{
			error.setInfo("table `" + tableName + "' already exists.");
			return false;
		}   // ... 1)
		
		for(Column column: instruction.columnList)
		{
			if(!column.isValid())
			{
				error.setInfo("column " + column + " is not valid.");
				return false;
			}
		}  // ... 2)
		
		String primaryKey = instruction.data.get("primaryKey");
		Table newTable = new Table(tableName, instruction.columnList, primaryKey);
		if(!newTable.hasColumn(primaryKey))
		{
			error.setInfo("primary key: " + primaryKey + " is not a column of table");
			return false;
		}  // ... 3)
		
		// start to create table...
		// file: create data file that contains records & set file handler
		// index: create an empty index for new table & set index handler
		tableList.add(newTable);
		newTable.file = new FileHandler(newTable);
		
		Index newIndex = new Index(tableName, primaryKey, "primaryIndex");  
		indexList.add(newIndex);
		newTable.index = new IndexHandler(newIndex);  // set index handler
		return true;
	}
	
	static boolean dropTable(Instruction instruction, Error error) 
	{
		String tableName = instruction.data.get("tableName");
		if(!hasTable(tableName))
		{
			error.setInfo("table `" + tableName + "' not exists.");
			return false;
		}
		// start to drop table... 
		// 1) delete all indexes on this table
		// 2) delete table data file
		Table table = getTable(tableName);
		List<Index> tempIndexList = new ArrayList<Index>();
		for(Index index : indexList)
			if(index.table == table)
				tempIndexList.add(index);
		for(Index index  : tempIndexList)
			indexList.remove(index);
		tableList.remove(table);
		return true;
	}

	static boolean createIndex(Instruction instruction, Error error) throws IOException
	{
		/* data:
		 *   indexName: String
		 *   tableName: String
		 *   columnName: String
		 */
		/* possible errors: 
		 * 1) table not exist  
		 * 2) column not exist  
		 * 3) index already exist
		 */
		String tableName = instruction.data.get("tableName");
		String columnName = instruction.data.get("columnName");
		String indexName = instruction.data.get("indexName");
		if(!hasTable(tableName))  // table does not exist
		{
			error.setInfo("there does not exist table with name of " + tableName);
			return false;
		}  // ... 1)
		if(!getTable(tableName).hasColumn(columnName)) // column does not exist
		{
			error.setInfo("there does not exist column with name of " + columnName);
			return false;
		}  // ... 2)
		if(hasIndex(tableName, columnName))
		// there already exist an index on tableName.columnName
		{
			error.setInfo("there is already an index on " + tableName + "." + columnName);
			return false;
		}  // ... 3)
		
		// start to create an index 
		Index newIndex = new Index(tableName, columnName, indexName);
		indexList.add(newIndex);
		// insert current records into new index
		Table table = getTable(tableName);
		Column column = table.getColumn(columnName);
		List<Integer> allShifts = table.index.getAllShifts();
		IndexHandler handler = new IndexHandler(newIndex); 
		for(int i: allShifts)
		{
			Record r = table.file.retrive(i);
			Map<Column, Object> map = RecordManager.unpack(table, r);
			handler.insert(map.get(column), i);
		}
		return true;
	}

	static boolean dorpIndex(Instruction instruction, Error error) 
	{
		String indexName = instruction.data.get("indexName");
		if(indexName.equals("primaryIndex"))
		{
			error.setInfo("primaryIndex is not allowed to be droped");
			return false;
		}
		for(int i = 0; i < indexList.size(); i++)
		{
			if(indexList.get(i).indexName.equals(indexName))
			{
				indexList.remove(i);
				return true;
			}
		}
		error.setInfo("Index " + indexName + " not exitst.");
		return false;
	}

	static boolean insert(Instruction instruction, Error error) throws IOException
	{
		/* data:
		 * tableName: String
		 * valueList: ArrayList<String>
		 */
		/* possible errors 
		 * 1) table not exist
		 * 2) valueList.size is not to columnList.size()
		 * 3) value is not compatible with column
		 * 4) there is already another record with the same primary key
		 * 5) there is already another record with the same unique column
		 */
		String tableName = instruction.data.get("tableName");
		if(!hasTable(tableName))
		{
			error.setInfo("table `" + tableName + "' does not exists");
			return false;
		}  // ... 1)
		Table table = getTable(tableName);
		ArrayList<String> valueList = instruction.valueList;
		if(valueList.size() != table.columnList.size())
		{
			error.setInfo("size of valueList is not equal to table.columnList.size()");
			return false;
		}  // ... 2)
		for(int i = 0; i < valueList.size(); i++)
		{
			if(!table.columnList.get(i).isCompatible(valueList.get(i)))
			{
				error.setInfo(valueList.get(i) + " is not compatible with " + table.columnList.get(i));
				return false;
			}
		}  // ... 3)
		String primaryKeyValue = valueList.get(table.primaryKeySubscript);
		int shift = table.index.search(primaryKeyValue);
		if(shift != -1) // exist another record with the same primary key
		{
			error.setInfo("there exist another record with the same primary key");
			return false;
		}  // ... 4)
		List<Index> allIndexOfThisTable = getAllIndexesOfATable(table);
		Record record = RecordManager.pack(table, valueList);
		Map<Column, Object> recordMap = RecordManager.unpack(table, record);
		for(Column column: table.columnList)
		{
			if(column.unique == true)
			{
				Index index = getIndex(table.tableName, column.columnName);
				if(index != null)
				{
					int uniqueShift = (new IndexHandler(index)).search(valueList.get(table.getColumnSubscript(index.columnName)));
					if(uniqueShift != -1) // exist another record with the same primary key
					{
						error.setInfo("there exist another record with a same unique attribute");
						return false;
					}
				}
				else  // need to test every record
				{
					List<Integer> allShifts = table.index.getAllShifts();
					Record r;
					for(int i: allShifts)
					{
						r = table.file.retrive(i);
						Map<Column, Object> m = RecordManager.unpack(table, r);
						if(recordMap.get(column).equals(m.get(column)))
						{
							error.setInfo("there exist another record with a same unique attribute");
							return false;
						}
					}
				}
			}
		} // ...5)
		
		// start to insert
		shift = table.nextAvailable;
		table.index.insert(primaryKeyValue, shift);
		table.file.insert(record, shift);
		// update other indexes
		for(Index index: allIndexOfThisTable)
		{
			if(index == table.index.index)
				continue;
			(new IndexHandler(index)).insert(valueList.get(table.getColumnSubscript(index.columnName)), shift);
		}
		return true;
	}
	
	static boolean selectWithoutCondition(Instruction instruction, Error error) throws IOException 
	{
		/* data:
		 *   columnName: String
		 *   tableName: String
		 */
		/* possible errors 
		 * 1) table not exist
		 * 2) column not exist
		 */
		String tableName = instruction.data.get("tableName");
		if(!hasTable(tableName))
		{
			error.setInfo("table `" + tableName + "' does not exists");
			return false;
		} // ... 1)
		// start to output
		Table table = getTable(tableName);
		table.printTitle();
		int count = 0;
		Record record;
		List<Integer> shifts = table.index.getAllShifts();
		
		for(int shift: shifts)
		{
			record = table.file.retrive(shift);
			RecordManager.printRecord(table, record);
			count ++;
		}
		System.out.println("-------------------------- " + count + " records seleted");
		return true;
	}
	
	static boolean selectWithCondition(Instruction instruction, Error error) throws IOException 
	{
		// TODO   need optimize
		/* data:
		 *   tableName: String
		 *   conditionList: ArrayList<Condition>
		 */
		/* possible errors 
		 * 1) table not exist
		 * 2) column not exist
		 * 3) condition not valid
		 */
		String tableName = instruction.data.get("tableName");
		if(!hasTable(tableName))
		{
			error.setInfo("table `" + tableName + "' does not exists.");
			return false;
		} // ... 1)
		Table table = getTable(tableName);
		for(Condition condition: instruction.conditionList)
		{
			if(!table.hasColumn(condition.columnName))
			{
				error.setInfo("column `" + condition.columnName + "' does not exist.");
				return false;
			}    // ... 2)
			else
			{
				Column column = table.getColumn(condition.columnName);
				if(!column.isValidCondition(condition))
				{
					error.setInfo("condition " + condition + " is not valid.");
					return false;
				} // ... 3)
			}
		}
		int count = 0;
		List<Integer> shifts = table.index.getAllShifts();
		Record record;
		table.printTitle();
		for(int shift: shifts)
		{
			record = table.file.retrive(shift);
			if(RecordManager.satisfy(table, record, instruction.conditionList))
			{
				RecordManager.printRecord(table, record);
				count ++;
			}
		}
		System.out.println("--------------------------" + count + " records selected");
		return true;
	}

	static boolean deleteWithoutCondition(Instruction instruction, Error error) throws IOException 
	{
		/* data:
		 *   tableName: String
		 */
		/* possible errors 
		 * 1) table not exist
		 */
		String tableName = instruction.data.get("tableName");
		if(!hasTable(tableName))
		{
			error.setInfo("table `" + tableName + "' does not exists.");
			return false;
		} // ... 1)
		Table table = getTable(tableName);
		BufferManager.deleteTable(table);
		table.index.reset();
		for(Index index: getAllIndexesOfATable(table))
		{
			if(index == table.index.index)
				continue;
			else
				index.btree.reset();
		}
		table.file.reset();
		table.count = 0;
		table.nextAvailable = 0;
		return true;
	}
	
	static boolean deleteWithCondition(Instruction instruction, Error error) throws IOException
	{
		// TODO need to optimize
		/* data:
		 *   tableName: String
		 *   conditionList: ArrayList<Condition>
		 */
		/* possible errors 
		 * 1) table not exist
		 * 2) column not exist
		 * 3) condition not valid
		 */
		String tableName = instruction.data.get("tableName");
		if(!hasTable(tableName))
		{
			error.setInfo("table `" + tableName + "' does not exists.");
			return false;
		} // ... 1)
		Table table = getTable(tableName);
		for(Condition condition: instruction.conditionList)
		{
			if(!table.hasColumn(condition.columnName))
			{
				error.setInfo("column `" + condition.columnName + "' does not exist.");
				return false;
			}    // ... 2)
			else
			{
				Column column = table.getColumn(condition.columnName);
				if(!column.isValidCondition(condition))
				{
					error.setInfo("condition " + condition + " is not valid.");
					return false;
				} // ... 3)
			}
		}
		int count = 0;
		List<Integer> allShifts = table.index.getAllShifts();
		List<Index> allIndexOfThisTable = getAllIndexesOfATable(table);
		List<IndexHandler> indexHandlerList = new ArrayList<IndexHandler>();
		for(Index index: allIndexOfThisTable)
			indexHandlerList.add(new IndexHandler(index));
		Record record;
		for(int shift: allShifts)
		{
			record = table.file.retrive(shift);
			if(RecordManager.satisfy(table, record, instruction.conditionList))
			{
				Map<Column, Object> map = RecordManager.unpack(table, record);
				for(IndexHandler indexHandler: indexHandlerList)
				{
					Column column = table.columnList.get(table.getColumnSubscript(indexHandler.index.columnName));
					indexHandler.delete(map.get(column));
				}
				count++;
			}
		}
		table.count -= count;
		System.out.println(count + " records deleted");
		return true;
	}

	static boolean dump()
	{
		File file = new File(basePath + "catalog.data");
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			ObjectOutputStream objectOutput = new ObjectOutputStream(out);
			objectOutput.writeObject(tableList);
			objectOutput.writeObject(indexList);
			objectOutput.flush();
			objectOutput.close();
		} catch(IOException e) {
			System.out.println("dump failed");
            e.printStackTrace();
            return false;
		}
		return true;
	}

	static boolean load(Error error)
	{
		File file = new File(basePath + "catalog.data");
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			ObjectInputStream objectInput = new ObjectInputStream(in);
			tableList = (ArrayList<Table>)(objectInput.readObject());
			indexList = (ArrayList<Index>)(objectInput.readObject());
			objectInput.close();
		} catch(IOException | ClassNotFoundException e) {
			error.setInfo("load failed, make sure that you have 'catalog.data' in " + basePath);
            return false;
		}
		return true;
	}
	
	static boolean show() 
	{
		for(Table table: tableList)
		{
			System.out.print(table);
		}
		for(Index index: indexList)
		{
			System.out.print(index);
		}
		if(tableList.isEmpty())
			System.out.println("you don't have table yet");
		return true;
	}

	static boolean quit() 
	{
		try {
			BufferManager.closeAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dump();
		return true;
	}

	// judge if there is a table with name tableName
	static boolean hasTable(String tableName)
	{
		for(Table table: tableList)
		{
			if(table.tableName.equals(tableName))
				return true;
		}
		return false;
	}
	
	static Table getTable(String tableName)
	{
		for(Table table: tableList)
			if(table.tableName.equals(tableName))
				return table;
		return null;
	}

	// judge if there is an index of tableName.columnName
	static boolean hasIndex(String tableName, String columnName)
	{
		for(Index index: indexList)
			if(index.tableName.equals(tableName) && index.columnName.equals(columnName))
				return true;
		
		return false;
	}
	
	static Index getIndex(String tableName, String columnName) 
	{
		for(Index index: indexList)
			if(index.tableName.equals(tableName) && index.columnName.equals(columnName))
				return index;
		
		return null;
	}

	static ArrayList<Index> getAllIndexesOfATable(Table table)
	{
		ArrayList<Index> list = new ArrayList<Index>();
		for(Index index: indexList)
		{
			if(index.table == table)
				list.add(index);
		}
		return list;
	}

}


class Column implements Serializable
{
	private static final long serialVersionUID = -1714059658924301401L;
	String columnName;
	String type;
	boolean unique;
	int size;
	Column(String columnName, String type, boolean unique)
	{
		this.columnName = columnName;
		this.type = type;
		this.unique = unique;
		if(type.equals("int"))
			this.size = 4;
		else if(type.equals("float"))
			this.size = 4;
		else  // char(n)
		{
			this.size = 0;
			int i = 0;
			while(i+5 < type.length())
			{
				size = size *10 + (type.charAt(i+5)) - '0';
				i++;
				if(!(type.charAt(i+5) >= '0' && type.charAt(i+5) <= '9'))
					break;
			}
		}
	}
	
	// TODO
	boolean isValidCondition(Condition condition) 
	{
		return true;
	}
	
	boolean isCompatible(String string) 
	{
		if(type.equals("int"))
		{
			try
			{
				Integer.parseInt(string);
				return true;
			}
			catch(NumberFormatException e)
			{
				return false;
			}
		}
		else if(type.equals("float"))
		{
			try
			{
				Float.parseFloat(string);
				return true;
			} 
			catch(NumberFormatException e)
			{
				return false;
			}
		}
		else // char(n)
		{
			if(string.length() <= size)
				return true;
			else
				return false;
		}
	}
	
	boolean isValid() 
	{
		return true;
	}
	
	public String toString()
	{
		return columnName + " " + type + (unique?" unique":"");
	}
}

class Table implements Serializable
{
	private static final long serialVersionUID = 6333580592823882494L;
	String tableName;
	ArrayList<Column> columnList;
	int columnNumber;
	String primaryKey;
	int primaryKeySubscript;
	String primaryKeyType;
	String tableFileName;
	File tableFile;
	IndexHandler index;
	FileHandler file;
	
	int count;
	int nextAvailable;
	int recordSize;

	Table(String tableName, ArrayList<Column> columnList, String primaryKey)
	{
		this.tableName = tableName;
		this.columnList = columnList;
		this.columnNumber = columnList.size();
		this.primaryKey = primaryKey;
		this.primaryKeySubscript = getColumnSubscript(primaryKey);
		this.primaryKeyType = columnList.get(primaryKeySubscript).type;
		this.tableFileName = tableName + ".data";
		this.tableFile = new File(CatalogManager.basePath+tableFileName);
		try {
			tableFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.count = 0;
		this.nextAvailable = 0;
		this.recordSize = 0;
		for(Column column: columnList)
			this.recordSize += column.size;
	}
	
	// judge if there is a column with name columnName
	boolean hasColumn(String columnName)
	{
		for(Column column: columnList)
		{
			if(column.columnName.equals(columnName))
				return true;
		}
		return false;
	}

	Column getColumn(String columnName)
	{
		for(Column column: columnList)
			if(column.columnName.equals(columnName))
				return column;
		return null;
	}

	int getColumnSubscript(String string) 
	{
		for(int i = 0; i < columnNumber; i++)
			if(string.equals(columnList.get(i).columnName))
				return i;
		return -1;
	}

	public String toString()
	{
		String result = "Table: " + tableName + "\n";
		result += "\tcolumn list: " + columnList + "\n";
		result += "\tprimary key: " + primaryKey + "\n";
		result += "\trecord count: " + count + "\n";
		return result;
	}
	
	int getNextAvailable(int num)
	{
		num += recordSize;
		if((num+recordSize) % CatalogManager.blockSize < recordSize)
			num += CatalogManager.blockSize % recordSize;
		return num;
	}

	void updateNextAvailable() 
	{
		nextAvailable = getNextAvailable(nextAvailable);
	}

	void printTitle() 
	{
		for(Column column: columnList)
		{
			if(column.type.equals("int") || column.type.equals("float"))
				System.out.printf("%-10s", column.columnName);
			else
				System.out.printf("%-" + (max((column.size), column.columnName.length())+1) + "s", column.columnName);
		}
		System.out.println("\n--------------------------");
	}

	static int max(int x, int y) 
	{
		if(x > y)
			return x;
		else
			return y;
	}
	
}

class Index implements Serializable
{
	private static final long serialVersionUID = 8034350026565084828L;
	String tableName;
	String columnName;
	String indexName;
	String columnType;
	Table table;
	BTree<?> btree;
	
	Index(String tableName, String columnName, String indexName)
	{
		this.tableName = tableName;
		this.columnName = columnName;
		this.indexName = indexName;
		this.table = CatalogManager.getTable(tableName);
		this.columnType = table.getColumn(columnName).type;
		if(columnType.equals("int"))
			this.btree = new BTree<Integer>();
		else if(columnType.equals("float"))
			this.btree = new BTree<Float>();
		else
			this.btree = new BTree<String>();
	}
	
	public String toString()
	{
		return "Index: " + indexName + " on " + tableName + "." + columnName + "\n";
	}
}

class IndexHandler implements Serializable
{
	private static final long serialVersionUID = 7505091515547797511L;
	Index index;
	BTree<?> btree;
	String indexType;
	IndexHandler(Index index)
	{
		this.index = index;
		this.btree = index.btree;
		this.indexType = index.columnType;
	}
	
	List<Integer> getAllShifts() 
	{
		return btree.getAllShifts();
	}

	int search(String element)
	{
		if(indexType.equals("int"))
			return ((BTree<Integer>)btree).search(Integer.parseInt(element));
		else if(indexType.equals("float"))
			return ((BTree<Float>)btree).search(Float.parseFloat(element));
		else
			return ((BTree<String>)btree).search(element);
	}
	
	void insert(String element, int shift)
	{
		if(indexType.equals("int"))
			((BTree<Integer>)btree).insert(Integer.parseInt(element), shift);
		else if(indexType.equals("float"))
			((BTree<Float>)btree).insert(Float.parseFloat(element), shift);
		else
			((BTree<String>)btree).insert(element, shift);
	}
	
	void insert(Object object, int shift)
	{
		if(indexType.equals("int"))
			((BTree<Integer>)btree).insert((int)object, shift);
		else if(indexType.equals("float"))
			((BTree<Float>)btree).insert((float)object, shift);
		else
			((BTree<String>)btree).insert((String)object, shift);
	}
	
	void delete(Object object)
	{
		if(indexType.equals("int"))
			((BTree<Integer>)btree).delete((int)object);
		else if(indexType.equals("float"))
			((BTree<Float>)btree).delete((float)object);
		else
			((BTree<String>)btree).delete((String)object);
	}
	
	void reset()
	{
		btree.reset();
	}
}

class FileHandler implements Serializable
{
	private static final long serialVersionUID = -3641044319949249570L;
	File tableFile;
	String tableFileName;
	Table table;
	FileHandler(Table table)
	{
		tableFile = table.tableFile;
		tableFileName = table.tableFileName;
		this.table = table;
	}

	void insert(Record record, int shift) throws IOException
	{
		BufferManager.insert(table, record, shift);
//		RandomAccessFile raf = new RandomAccessFile(tableFile, "rw");
//		raf.seek(shift);
//		raf.write(record.data, 0, record.currentSize);
//		raf.close();
		table.count ++;
		table.updateNextAvailable();
	}
	
	Record retrive(int shift) throws IOException 
	{
		return BufferManager.retrive(table, shift);
//		Record record = new Record(table.recordSize);
//		RandomAccessFile raf = new RandomAccessFile(tableFile, "rw");
//		raf.seek(shift);
//		raf.read(record.data, 0, table.recordSize); 
//		record.currentSize = table.recordSize;
//		raf.close();
//		return record;
	}
	
	void reset() throws IOException
	{
		tableFile.delete();
		tableFile.createNewFile();
	}
}