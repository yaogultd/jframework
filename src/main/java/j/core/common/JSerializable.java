package j.core.common;

import j.util.JUtilBean;

import java.io.Serializable;

public class JSerializable implements Serializable{
    @Override
    public String toString(){
        return JUtilBean.bean2Json(this);
    }
}
