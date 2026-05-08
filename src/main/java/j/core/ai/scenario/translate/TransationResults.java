package j.core.ai.scenario.translate;

import j.util.JUtilBean;
import j.util.JUtilJSON;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TransationResults {
    //多段文本的翻译结果，结果顺序与源文本输入顺序一致
    private List<String> translations = new ArrayList<>();

    public TransationResults(){

    }

    /**
     *
     * @param jsonArray
     */
    public TransationResults(JSONArray jsonArray){
        this.fromJson(jsonArray);
    }

    /**
     *
     * @param jsonArrayString
     */
    public TransationResults(String jsonArrayString){
        this.fromJson(jsonArrayString);
    }

    /**
     *
     * @param jsonArrayString
     */
    public void fromJson(String jsonArrayString){
        this.fromJson(JUtilJSON.array(jsonArrayString));
    }

    /**
     *
     * @param jsonArray
     */
    public void fromJson(JSONArray jsonArray){
        this.translations.clear();
        List<String> parsed = JUtilJSON.json2Beans(String.class, jsonArray);
        if(parsed != null && !parsed.isEmpty()) this.translations.addAll(parsed);
    }

    @Override
    public String toString(){
        return JUtilBean.bean2Json(this);
    }

    public static void main(String[] args) throws Exception{
        String s="[\"SpaceX's R&D iteration speed is extremely fast.\",\"I am very good at butterfly stroke.\",\"This is a beautiful small mountain village.\"]";
        TransationResults results = new TransationResults();
        results.fromJson(s);

        System.out.println(results);
    }
}
