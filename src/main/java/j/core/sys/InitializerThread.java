package j.core.sys;

/**
 *
 */
public class InitializerThread implements Runnable{
    private Initializer initializer;

    /**
     *
     * @param initializer
     */
    public InitializerThread(Initializer initializer){
        this.initializer=initializer;
    }

    @Override
    public void run() {
        try{
            if(this.initializer!=null) this.initializer.initialization();
        }catch (Exception e){
            System.out.println("执行初始化（"+this.initializer.getClass().getCanonicalName()+"）时出错：");
            e.printStackTrace();
        }
    }
}
