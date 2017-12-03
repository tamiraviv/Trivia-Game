package parsing.generic;

import parsing.util.Row;

public abstract class Callback {

    public abstract void reduce(Row row);

    public abstract boolean map(Row row);
}
