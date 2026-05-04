package j.core.service;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.service.Service;
import j.core.cache.CachedMap;
import j.core.fs.JDFSFile;
import j.core.nio.DataSourceFile;
import j.core.nvwa.Nvwa;
import j.core.service.client.Client;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021-07-25",
        description = "测试",
        reviewers = {"肖炯"})
@Service(path = "/service/test")
public class ServiceTest extends ServiceBase{
    @Service(path = "add")
    public ServiceResponse<Long> add(Map<String, String> headers,
                                     Map<String, String> params,
                                     Map<String, DataSourceFile> files,
                                     String payload,
                                     Object[] objects) {
        System.out.println("receive files -> "+files);
        DataSourceFile file=files.get("theFile");
        if(file!=null){
            System.out.println("receive file -> "+file.getNameOriginal()+" -> "+file.getSource().getAbsolutePath()+" -> \r\n"+ JDFSFile.read(file.getSource()));
        }

        if(objects != null && objects.length>0){
            return new ServiceResponse(true,"1","I got ->>> "+objects[0],1L);
        }else{
            return new ServiceResponse(true,"1","I got nothing",2L);
        }
    }

    @Service(path = "add")
    public ServiceResponse<Long> add(Object[] objects) {
        if(objects != null && objects.length>0){
            return new ServiceResponse(true,"1","I got "+objects[0],1L);
        }else{
            return new ServiceResponse(true,"1","I got nothing",2L);
        }
    }

    @Service(path = "hello")
    public ServiceResponse<String> hello(Integer a, String b, Object[] c) {
        return new ServiceResponse(true,"1","I got a = "+a+", b = "+b+", c = "+c,"ABCDEFE");
    }

    public static void main(String[] args) throws Exception{
        //Method m= Methods.matches(ServiceTest.class, "hello", new Object[]{1, "b", new Object[]{"c","d"}});
        //System.out.println("m="+m);
        //System.out.println(Types.ofParent(Object.class, String.class));

        Nvwa.startup();
        while(!Nvwa.isScanned()){
            System.out.println("waiting for startup--------------");
            try{
                Thread.sleep(1000);
            }catch(Exception e){}
        }
        try{
            Thread.sleep(5000);
        }catch(Exception e){}

        CachedMap cm = new CachedMap("aaaaaaaaa");
        cm.addOne("abc", "def-");
        int loops=0;
        while (true){
            long t1=System.currentTimeMillis();
            Object x=cm.get("abc");
            long t3=System.currentTimeMillis();
            System.out.println("resp"+loops+"-------------->"+x+" = "+(t3-t1));
            loops++;
        }

        /*
        ServiceTest serviceTest=(ServiceTest)j.core.service.ServiceAdapter.getService(ServiceTest.class);

        ServiceResponse<Long> resp= serviceTest.add(new Object[]{"AAA", "BBB"});
        System.out.println("resp1-------------->"+resp.getMessage());

        resp=j.core.service.ServiceAdapter.call("/service/test/add", new Object[]{new String[]{"AAA", "BBB"}});
        System.out.println("resp2-------------->"+resp.getMessage());


        ServiceBase service=j.core.service.ServiceAdapter.getService("/service/test/add");
        resp=j.core.service.ServiceAdapter.call(service, "add", null, null, null, null, new Object[]{"DDD", "FFF"});
        System.out.println("resp3-------------->"+resp.getMessage());

        ServiceResponse<String> resp2= serviceTest.hello(1, "b", new Object[]{"c"});
        System.out.println("resp4-------------->"+resp2.getMessage());

        for(int i=10; i<11; i++){
            ServiceTestThread th=new ServiceTestThread(i);
            (new Thread(th)).start();
        }*/
    }
}
