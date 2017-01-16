package Common;

import java.io.Serializable;

public class DimEntry implements Serializable {
    public String id;
    public String arg;

    public DimEntry(String id, String arg) {
        this.id = id;
        this.arg = arg;
    }
}
