package j.core.hp.asynchronous;

public class WaitingTest {
    public static void main(String[] args){
        Waitings.waiting("AAA", 30000, "CCC");

        (new Thread(new WaitingTestThread())).start();

        Object result=Waitings.getResult("AAA");

        System.out.println("result = "+result);
    }
}

class WaitingTestThread implements Runnable{
    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        System.out.println("waiting ...... ");
        try{
            Thread.sleep(3000);
        }catch (Exception e){}
        System.out.println("setResult ...... ");
        Waitings.setResult("AAA", "BBB");
    }
}
