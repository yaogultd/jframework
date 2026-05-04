package j.core.ai;

import j.util.JUtilBean;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Model implements Cloneable{
    private String id;
    private String name;
    private List<Integer> usages;
    private double pricePer1MTokensInput = 0d;
    private double pricePer1MTokensInputCached = 0d;
    private double pricePer1MTokensOutput = 0d;
    private int inputTokenLimit=128000;
    private int outTokenLimit=128000;

    //速度（1~5，数字越大代表速度越高）
    private int speedRank = 1;

    //推理能力（1~5，数字越大代表能力越强）
    private int reasoningRank = 1;

    public Model(String id,
                 String name,
                 List<Integer> usages,
                 double pricePer1MTokensInput,
                 double pricePer1MTokensInputCached,
                 double pricePer1MTokensOutput,
                 int speedRank,
                 int reasoningRank,
                 int inputTokenLimit,
                 int outTokenLimit){
        this.id = id;
        this.name = name;
        this.usages = usages;
        this.pricePer1MTokensInput = pricePer1MTokensInput;
        this.pricePer1MTokensInputCached = pricePer1MTokensInputCached;
        this.pricePer1MTokensOutput = pricePer1MTokensOutput;
        this.speedRank = speedRank;
        this.reasoningRank = reasoningRank;
        this.inputTokenLimit = inputTokenLimit;
        this.outTokenLimit = outTokenLimit;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        }catch(Exception e) {}
        return null;
    }

    public Model cloneMe() {
        try {
            return (Model)this.clone();
        }catch(Exception e) {}
        return null;
    }

    public boolean canBeUsedFor(Integer usedFor){
        return this.usages.contains(usedFor);
    }

    @Override
    public String toString(){
        return JUtilBean.bean2Json(this);
    }
}
