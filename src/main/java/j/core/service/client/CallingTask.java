package j.core.service.client;

import j.core.hp.asynchronous.Waitings;
import j.core.hp.thread.ThreadTask;
import j.core.nio.DataSourceFile;
import j.core.service.ServiceResponse;
import j.core.service.registry.Registration;

import java.util.Map;

/**
 *
 */
public class CallingTask extends ThreadTask {
    /**
     * @param in
     * @param retries
     */
    public CallingTask(Object[] in, int retries) {
        super(in, retries);
    }

    /**
     *
     * @param in
     * @param retries
     */
    public CallingTask(Object[] in,int retries,String uuid){
        super(in, retries, uuid);
    }

    /**
     *
     * @param in
     * @param retries
     * @param uuid
     * @param resultTimeout
     */
    public CallingTask(Object[] in,int retries,String uuid,long resultTimeout){
        super(in, retries, uuid, resultTimeout);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public Object[] execute() throws Exception {
        Registration reg=(Registration)this.in[0];
        String method=(String)this.in[1];
        Map<String, String> headers=(Map)this.in[2];
        Map<String, String> params=(Map)this.in[3];
        Map<String, DataSourceFile> files=(Map)this.in[4];
        String payload=(String)this.in[5];
        Object[] objects=(Object[])this.in[6];

        ServiceResponse response=j.core.service.ServiceAdapter.call(reg, method, headers, params, files, payload, objects);

        if(response != null) Waitings.setResult(uuid, response);
        return new Object[]{response};
    }

    /**
     * @param other
     * @return
     */
    @Override
    public boolean equalz(ThreadTask other) {
        return false;
    }
}
