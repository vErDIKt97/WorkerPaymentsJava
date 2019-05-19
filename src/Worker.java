import java.io.Serializable;

class Worker implements Serializable {

    private String name = null;
    private Integer sells = null;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    Integer getSells() {
        return sells;
    }

    void setSells(Integer sells) {
        this.sells = sells;
    }
}
