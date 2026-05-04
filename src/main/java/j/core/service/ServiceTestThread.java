package j.core.service;

public class ServiceTestThread implements Runnable{
    private int index;

    public ServiceTestThread(int index){
        this.index=index;
    }

    @Override
    public void run() {
        try{
            while(true){
                try{
                    Thread.sleep(10);
                }catch (Exception e){}
                long t1=System.currentTimeMillis();
                ServiceResponse<String> resp3 = j.core.service.ServiceAdapter.call("/service/test/hello", new Object[]{2, "bb", new Object[]{"cc"}});

                long t2=System.currentTimeMillis();
                System.out.println(this.index+" -> resp5-------------->(cost "+(t2-t1)+"ms) -> " + resp3.getMessage());
            }
        }catch (Exception e){}
    }
}
