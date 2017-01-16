package Common;

import java.io.Serializable;

public class ResultEntry implements Serializable {
    public String price;
    public String product;
    public String shipper;

    public ResultEntry(String price, String product, String shipper) {
        this.price = price;
        this.product = product;
        this.shipper = shipper;
    }
}
