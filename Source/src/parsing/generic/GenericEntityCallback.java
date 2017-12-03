package parsing.generic;

import parsing.util.Row;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class GenericEntityCallback<T> extends Callback {
    private final Map<String, T> map;
    private final String prefix;
    Constructor<T> clazzConstructor;

    public GenericEntityCallback(Map<String, T> map, Class<T> clazz, String prefix) {
        this.map = map;
        this.prefix = prefix;
        try {
            clazzConstructor = clazz.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reduce(Row row) {
        try {
            map.put((row.entity), clazzConstructor.newInstance(row.entity));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean map(Row row) {
        return row.superEntity.equals(prefix);
    }
}
