package parsing.generic;

import parsing.util.Row;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class GenericObjectLinkCallback<S, T> extends Callback {
    private final Map<String, S> map1;
    private final Map<String, T> map2;
    private final String relationType;
    private final Method collectionAdd;
    private final boolean isCollection;
    private final boolean isFlipped;
    Field field;

    public GenericObjectLinkCallback(Map<String, S> map1, Map<String, T> map2, Class<S> clazz, String relationType, String name) {
        this(map1, map2, clazz, relationType, name, true, false);
    }

    public GenericObjectLinkCallback(Map<String, S> map1, Map<String, T> map2, Class<S> clazz, String relationType, String name, boolean isCollection, boolean isFlipped) {
        this.map1 = map1;
        this.map2 = map2;
        this.relationType = relationType;
        this.isCollection = isCollection;
        this.isFlipped = isFlipped;
        Class<?> collectionClass = Collection.class;
        try {
            field = clazz.getField(name);
            collectionAdd = collectionClass.getMethod("add", Object.class);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reduce(Row row) {
        String s1 = isFlipped ? row.superEntity : row.entity;
        String s2 = isFlipped ? row.entity : row.superEntity;
        try {
            if (isCollection) {
                collectionAdd.invoke(field.get(map1.get(s1)), map2.get(s2));
            } else {
                field.set(map1.get(s1), map2.get(s2));
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean map(Row row) {
        String s1 = isFlipped ? row.superEntity : row.entity;
        String s2 = isFlipped ? row.entity : row.superEntity;
        return row.relationType.equals(relationType) && map1.containsKey(s1) && map2.containsKey(s2);
    }
}
