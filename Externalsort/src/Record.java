
public class Record implements Comparable<Record>{
    private long id;
    private double key;
    public Record(long id, double key)
    {
        this.id = id;
        this.key = key;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public double getKey() {
        return key;
    }
    public void setKey(double key) {
        this.key = key;
    }
    public int compareTo(Record r) {
        if (key > r.getKey())
        {
            return 1;
        }
        else if (r.getKey() > key)
        {
            return -1;
        }
        return 0;
    }
    
}
