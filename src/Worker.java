import java.io.Serializable;

public class Worker implements Serializable {

    private String name = null;
    private Integer sells = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSells() {
        return sells;
    }

    public void setSells(Integer sells) {
        this.sells = sells;
    }
}
