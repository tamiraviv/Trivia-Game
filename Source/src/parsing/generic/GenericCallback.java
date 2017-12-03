package parsing.generic;

import parsing.entities.Entity;
import parsing.ValueType;
import parsing.util.Row;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static parsing.util.Utils.parseValue;

public class GenericCallback extends Callback {

    boolean isFlipped;
    boolean isCollection;
    ValueType valueType;
    String relationType;
    String keyName;
    final Map<String, ? extends Entity> entities;
    Field field;
    Method collectionAdd;

    public GenericCallback(final Map<String, ? extends Entity> entities, ValueType valueType, String relationType, String keyName) {
        this(entities, valueType, relationType, keyName, false, false);
    }

    public GenericCallback(final Map<String, ? extends Entity> entities, ValueType valueType, String relationType, String keyName, boolean isCollection, boolean isFlipped) {
        this.entities = entities;
        this.valueType = valueType;
        this.relationType = relationType;
        this.keyName = keyName;
        this.isCollection = isCollection;
        this.isFlipped = isFlipped;
        if(entities.size() == 0) {
            System.out.println(entities.getClass() +" is empty :(");
            return;
        }
        Class<?> clazz = entities.values().iterator().next().getClass();
        Class<?> collectionClass = Collection.class;
        try {
            field = clazz.getField(keyName);
            collectionAdd = collectionClass.getMethod("add", Object.class);
        } catch (NoSuchFieldException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reduce(Row row) {
        String s1 = isFlipped ? row.superEntity : row.entity;
        String s2 = isFlipped ? row.entity : row.superEntity;
        try {
            if (isCollection) {
                collectionAdd.invoke(field.get(entities.get(s1)), parseValue(s2, valueType));
            }else{
                field.set(entities.get(s1), parseValue(s2, valueType));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean map(Row row) {
        String s1 = isFlipped ? row.superEntity : row.entity;
        return row.relationType.equals(relationType) && entities.keySet().contains(s1);
    }
}
