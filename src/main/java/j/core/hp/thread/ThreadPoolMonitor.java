package j.core.hp.thread;

/**
 *
 */
public interface ThreadPoolMonitor {
    /**
     * 当线程池开始时
     * @return
     */
    public Object onStart();

    /**
     * 当线程池重置时
     * @return
     */
    public Object onReset();

    /**
     * 当线程池内所有任务全部已执行最少一次时
     * @return
     */
    public Object onExecuted();
}
