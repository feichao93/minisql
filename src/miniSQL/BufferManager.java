package miniSQL;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;

public class BufferManager {
    final static int poolSize = 100;
    final static int blockSize = CatalogManager.blockSize;
    // BufferManager.insert(table, record, shift);
    // RandomAccessFile raf = new RandomAccessFile(tableFile, "rw");
    // raf.seek(shift);
    // raf.write(record.data, 0, record.currentSize);
    // raf.close();

    // return BufferManager.retrive(table, shift, recordSize);
    //	Record record = new Record(table.recordSize);
    //	RandomAccessFile raf = new RandomAccessFile(tableFile, "rw");
    //	raf.seek(shift);
    //	raf.read(record.data, 0, table.recordSize);
    //	record.currentSize = table.recordSize;
    //	raf.close();
    //	return record;

    private static ArrayList<Table> tableList = new ArrayList<Table>();
    private static ArrayList<RandomAccessFile> fileList = new ArrayList<RandomAccessFile>();
    private static Block[] pool = new Block[poolSize];
    private static BlockInfo[] poolInfo = new BlockInfo[poolSize];
    private static int lastAccessTime = 0;

    public static int getAndUpdateLastAccessTime() {
        int result = lastAccessTime;
        lastAccessTime++;
        return result;
    }

    public static void insert(Table table, Record record, int shift) throws IOException {
        int index = prepare(table, shift);
        pool[index].assignData(shift % blockSize, shift % blockSize + table.recordSize, record.data);
        poolInfo[index].updateLastAccessTime();
        poolInfo[index].changed = true;
    }

    public static Record retrive(Table table, int shift) throws IOException {
        int index = prepare(table, shift);
        Record record = new Record(table.recordSize);
        record.append(pool[index].getData(shift % blockSize, shift % blockSize + table.recordSize));
        poolInfo[index].updateLastAccessTime();
        return record;
    }

    public static void deleteTable(Table table) throws IOException {
        // clear blocks in pool
        for (int i = 0; i < poolSize; i++) {
            if (poolInfo[i] != null && poolInfo[i].table == table) {
                poolInfo[i] = null;
                pool[i] = null;
            }
        }
        // remove table from tableList and close file&remove file from fielList
        int tableIndex = tableList.indexOf(table);
        if (tableIndex != -1) {
            tableList.remove(tableIndex);
            fileList.get(tableIndex).close();
            fileList.remove(tableIndex);
        }
    }

    public static void closeAll() throws IOException {
        flush();
        for (RandomAccessFile raf : fileList) {
            raf.close();
        }
    }

    public static String printPool() {
        String result = "Buffer: \n";
        for (int i = 0; i < poolSize; i++) {
            if (poolInfo[i] != null) {
                result += "\t" + i + ": " + poolInfo[i].table.tableName + " " + poolInfo[i].blockShift + "\n";
            }
        }
        return result;
    }

    private static int prepare(Table table, int shift) throws IOException {
        // judge if table is in tableList;				// ... 1)
        // ... if not, add this table into tableList	// ... 2)
        // ... ... open table file						// ... 3)
        // if corresponding block is in pool			// ... 4)
        // ... if the pool is not full
        // ... ... create a new block buffer			// ... 5)
        // ... if the pool is full
        // ... ... dump block							// ... 6)
        // ... load block								// ... 7)
        int tableIndex = tableList.indexOf(table);
        if (tableIndex == -1) // table file not open yet // ... 1)
        {
            tableList.add(table);                        // ... 2)
            fileList.add(new RandomAccessFile(table.tableFile, "rw")); // ... 3)
            tableIndex = tableList.size() - 1;
        }
        RandomAccessFile currentFile = fileList.get(tableIndex);
        int blockShift = shift / blockSize;
        int index = searchBlock(table, blockShift);
        if (index == -1)                                // block is not in pool
        {                                                // ... 4)
            index = replaceStratege();                                // find the block for operation
            BlockInfo blockInfo = poolInfo[index];
            if (blockInfo == null)                        // it is a new block
            {
                pool[index] = new Block();
                poolInfo[index] = new BlockInfo();
            }                                            // ... 5)
            else if (blockInfo.changed == true)            // the pool is full. LRU returns an used block
            {
                dumpBlock(fileList.get(blockInfo.tableIndex), blockInfo.blockShift, pool[index]);
            }                                            // ... 6)
            // else LRU return an unchanged block, we can load data to this block
            loadBlock(currentFile, blockShift, pool[index]); // ... 7)
            poolInfo[index].assign(table, tableIndex, false, 0, blockShift);
        }
        return index;
    }

    private static int replaceStratege() {
        for (int i = 0; i < poolSize; i++) {
            if (poolInfo[i] == null)
                return i;
        }
        Date dt = new Date();
        Long time = dt.getTime();
        return (int) (time % poolSize);
    }

    private static void loadBlock(RandomAccessFile file, int blockShift, Block block) throws IOException {
        file.seek(blockShift * blockSize);
        file.read(block.data, 0, blockSize);
    }

    private static void dumpBlock(RandomAccessFile file, int blockShift, Block block) throws IOException {
        file.seek(blockShift * blockSize);
        file.write(block.data);
    }

    private static int searchBlock(Table table, int blockShift) {
        for (int i = 0; i < poolSize; i++) {
            if (poolInfo[i] != null && poolInfo[i].table == table && poolInfo[i].blockShift == blockShift)
                return i;
        }
        return -1;
    }

    private static void flush() throws IOException {
        // write all changed block back into file
        for (int i = 0; i < poolSize; i++) {
            if (poolInfo[i] != null && poolInfo[i].changed == true) {
                BlockInfo blockInfo = poolInfo[i];
                dumpBlock(fileList.get(blockInfo.tableIndex), blockInfo.blockShift, pool[i]);
                blockInfo.changed = false;
            }
        }
    }
}

class Block {
    byte[] data;

    Block() {
        data = new byte[CatalogManager.blockSize];
    }

    byte[] getData(int startIndex, int endIndex) {
        byte[] result = new byte[endIndex - startIndex];
        for (int i = 0; i < result.length; i++)
            result[i] = data[i + startIndex];
        return result;
    }

    public void assignData(int startIndex, int endIndex, byte[] b) {
        for (int i = startIndex, j = 0; i < endIndex; i++, j++)
            data[i] = b[j];
    }
}

class BlockInfo {
    Table table;
    int tableIndex;
    boolean changed;
    int lastAccessTime;
    int blockShift;

    public void assign(Table table, int tableIndex, boolean changed, int lastAccessTime, int blockShift) {
        this.table = table;
        this.tableIndex = tableIndex;
        this.changed = changed;
        this.lastAccessTime = lastAccessTime;
        this.blockShift = blockShift;
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = BufferManager.getAndUpdateLastAccessTime();
    }

    BlockInfo() {
        changed = false;
    }
}
