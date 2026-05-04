package j.core.cache;

import j.core.serialize.JSerialization;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 肖炯
 * @date 2023/7/31
 */
@Getter
@Setter
public class SerializedObject {
    private byte[] bytes;
    private Map<Object, Object> keyedValues=new HashMap<>();

    public void addKeyedValue(Object key, Object value){
        this.keyedValues.put(key, value);
    }

    public void removeKeyedValue(Object key){
        this.keyedValues.remove(key);
    }

    public Object getKeyedValue(Object key){
        return this.keyedValues.get(key);
    }

    public void clearKeyedValue(){
        this.keyedValues.clear();
    }

    /**
     *
     * @param object
     * @return
     */
    public static SerializedObject serialize(Serializable object) throws Exception{
        SerializedObject serialized=new SerializedObject();
        serialized.setBytes(JSerialization.serialize(object));
        return serialized;
    }

    /**
     *
     * @param object
     * @param keyedValues
     * @return
     */
    public static SerializedObject serialize(Serializable object, Map<Object, Object> keyedValues) throws Exception{
        SerializedObject serialized=new SerializedObject();
        serialized.setBytes(JSerialization.serialize(object));
        serialized.setKeyedValues(keyedValues);
        return serialized;
    }

    /**
     *
     * @param serialized
     * @return
     */
    public static Object deserialize(SerializedObject serialized)throws Exception{
        return JSerialization.deSerialize(serialized.getBytes());
    }
}
