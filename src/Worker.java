import java.io.Serializable;

class Worker implements Serializable {

    private String name = null;
    private Integer sells = null;

    String getName() {
        return name;
    }

    void setSells(Integer sells) {
        this.sells = sells;
    }

    public Integer getSells() {
        return sells;
    }
}
