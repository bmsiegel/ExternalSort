
public class MinHeap {
    public Record[] data;
    private int pos;
    private int last;
    public MinHeap()
    {
        data = new Record[4096];
        last = 0;
        pos = 0;
    }
    public boolean isEmpty()
    {
        return last == 0;
    }
    public boolean add(Record r)
    {
        if (last < data.length - 1)
        {
            data[last] = r;
            last++;
            return true;
        }
        return false;
    }
    public void setLast(int last)
    {
        this.last = last;
    }
    public void buildMinHeap()
    {
        for (int c = last/2 - 1; c >= 0; c--)
        {
            siftDown(c);
        }
    }
    private int leftChild(int pos)
    {
        if (pos >= last/2)
        {
            return -1;
        }
        return 2*pos + 1;
    }
    private boolean isLeaf(int pos)
    {
        return pos >= last/2 && pos < last;
    }
    public String toString()
    {
        String result = "";
        for (int c = 0; c < last; c++)
        {
            result += data[c].getId() + "\t" + data[c].getKey() + "\n";
        }
        return result;
    }
    public void removeMinRS(Record r)
    {
        data[0] = data[last - 1];
        data[last - 1] = r;
        last--;
     }
    public int getLast()
    {
        return last;
    }
    public void resetLast()
    {
        boolean broken = false;
        for (int c = 0; c < data.length; c++)
        {
            if (data[c] == null)
            {
                last = c;
                broken = true;
                break;
            }
        }
        if (!broken)
        {
            last = data.length;
        }
    }
    public Record removeMin()
    {
        Record temp = null;
        if (!isEmpty())
        {
            temp = data[0];
            data[0] = data[last - 1];
            data[last - 1] = temp;
            last--;
            siftDown(0);
        }
        return temp;
    }
    public Record remove(int pos)
    {
        Record temp = data[pos];
        data[pos] = null;
        return temp;
    }
    public void modify(int pos, Record r)
    {
        data[pos] = r;
    }
    public void siftDown(int pos)
    {
        if (!(pos < 0 || pos >= last))
        {
            while (!isLeaf(pos))
            {
                int min = leftChild(pos);
                if (min < (last - 1) && data[min].compareTo(data[min + 1]) > 0)
                {
                    min++;
                }
                if (data[pos].compareTo(data[min]) <= 0)
                {
                    break;
                }
                Record temp = data[pos];
                data[pos] = data[min];
                data[min] = temp;
                pos = min;
            }
        }
    }
}
