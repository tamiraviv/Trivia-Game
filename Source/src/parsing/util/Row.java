package parsing.util;

public class Row {
    public String id;
    public String entity;
    public String relationType;
    public String superEntity;

    public Row(String split[]) {
        this.id = split[0];
        this.entity = split[1];
        this.relationType = split[2];
        this.superEntity = split[3];
    }
}
