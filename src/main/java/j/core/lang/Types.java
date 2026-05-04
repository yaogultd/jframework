package j.core.lang;

import java.util.HashMap;
import java.util.Map;

public final class Types {
    private static final Map<String, String> alikes=new HashMap<>();

    static {
        alikes.put("int", "Integer");
        alikes.put("float", "Float");
        alikes.put("double", "Double");
        alikes.put("short", "Short");
        alikes.put("byte", "Byte");
        alikes.put("long", "Long");
        alikes.put("int[]", "Integer[]");
        alikes.put("float[]", "Float[]");
        alikes.put("double[]", "Double[]");
        alikes.put("short[]", "Short[]");
        alikes.put("byte[]", "Byte[]");
        alikes.put("long[]", "Long[]");
    }

    /**
     *
     * @param c1
     * @param c2
     * @return
     */
    public static boolean equals(Class c1, Class c2){
        if(c1.getCanonicalName().equals(c2.getCanonicalName())) return true;

        String cName1=c1.getSimpleName();
        if(alikes.containsKey(cName1)) cName1=alikes.get(cName1);

        String cName2=c2.getSimpleName();
        if(alikes.containsKey(cName2)) cName2=alikes.get(cName2);

        return cName1.equals(cName2);
    }

    /**
     *
     * @param parent
     * @param child
     * @return
     */
    public static boolean ofParent(Class parent, Class child){
        String parentName=parent.getSimpleName();
        if(alikes.containsKey(parentName)) parentName=alikes.get(parentName);

        String childName=child.getSimpleName();
        if(alikes.containsKey(childName)) childName=alikes.get(childName);

        boolean isArray=childName.endsWith("[]");

        Class p=child.getSuperclass();
        while(p != null){
            if(parentName.equals(getName(p,isArray))) return true;
            p=p.getSuperclass();
        }

        Class[] interfaces=child.getInterfaces();
        for(int i=0; interfaces!=null && i<interfaces.length; i++){
            if(parentName.equals(getName(interfaces[i],isArray))) return true;
        }

        return false;
    }

    /**
     *
     * @param clazz
     * @param isArray
     * @return
     */
    private static String getName(Class clazz, boolean isArray){
        String name=clazz.getSimpleName();
        if(isArray && !name.endsWith("[]")) name+="[]";
        return name;
    }
}
