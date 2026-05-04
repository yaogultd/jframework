package j.core.lang;

import j.core.common.JArray;

import java.lang.reflect.Method;

public final class Methods {
    /**
     * 找出clazz成员方法中，名字为name，参数类型与给定参数args一致的方法
     * @param clazz
     * @param name
     * @param args
     * @return
     */
    public static Method matches(Class clazz, String name, Object[] args){
        try{
            Method[] methods=clazz.getMethods();
            if(methods==null || methods.length==0){
                return null;
            }

            //匹配的方法
            Method matched=null;

            //严格匹配
            for(int i=0; i<methods.length; i++){
                if(matchesStrict(methods[i], name, args)){
                    matched=methods[i];
                    break;
                }
            }

            //宽松匹配（给定参数是参数类型的子类）
            if(matched==null){
                for(int i=0; i<methods.length; i++){
                    if(matches(methods[i], name, args)){
                        matched=methods[i];
                        break;
                    }
                }
            }

            //匹配方法名和参数个数
            if(matched==null){
                for(int i=0; i<methods.length; i++){
                    if(matchesArgsCount(methods[i], name, args)){
                        matched=methods[i];
                        break;
                    }
                }
            }

            return matched;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * method是否名字为name，且参数类型与给定参数args一致
     * @param method
     * @param name
     * @param args
     * @return
     */
    public static boolean matchesStrict(Method method, String name, Object[] args){
        try{
            if(!method.getName().equals(name)) return false;

            Class[] types=method.getParameterTypes();
            if(args==null || args.length==0){//给定参数为空
                return (types==null || types.length==0);//方法的参数必须也为空才相匹配
            }

            //给定参数不为空，方法的参数为空或个数不匹配
            if(types==null || types.length!=args.length) return false;

            boolean matches=true;
            for(int i=0; i<types.length; i++){
                if(args[i]==null) continue;
                if(!Types.equals(types[i], args[i].getClass())){
                    matches=false;
                    break;
                }
            }

            return matches;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * method是否名字为name，且参数个数一致
     * @param method
     * @param name
     * @param args
     * @return
     */
    public static boolean matchesArgsCount(Method method, String name, Object[] args){
        try{
            if(!method.getName().equals(name)) return false;

            Class[] types=method.getParameterTypes();
            if(args==null || args.length==0){//给定参数为空
                return (types==null || types.length==0);//方法的参数必须也为空才相匹配
            }

            //给定参数不为空，方法的参数为空或个数不匹配
            if(types==null || types.length!=args.length) return false;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * method是否名字为name，且参数类型与给定参数args一致
     * @param method
     * @param name
     * @param args
     * @return
     */
    public static boolean matches(Method method, String name, Object[] args){
        try{
            if(!method.getName().equals(name)) return false;

            Class[] types=method.getParameterTypes();
            if(args==null || args.length==0){//给定参数为空
                return (types==null || types.length==0);//方法的参数必须也为空才相匹配
            }

            //给定参数不为空，方法的参数为空或个数不匹配
            if(types==null || types.length!=args.length) return false;

            boolean matches=true;
            for(int i=0; i<types.length; i++){
                if(args[i]==null) continue;
                if(!Types.equals(types[i], args[i].getClass()) && !Types.ofParent(types[i], args[i].getClass())){
                    matches=false;
                    break;
                }
            }
            return matches;
        }catch (Exception e){
            return false;
        }
    }
}