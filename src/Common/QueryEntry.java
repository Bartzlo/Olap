package Common;

import java.io.Serializable;

public class QueryEntry implements Serializable {
    public String table;
    public String column;
    public String arg;

    public QueryEntry(String table, String column, String arg){
        this.table = table;
        this.column = column;
        this.arg = arg;
    }
}
