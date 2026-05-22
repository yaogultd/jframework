package j.I18N;

import j.util.*;
import org.json.JSONArray;

import java.io.InputStream;
import java.util.List;

public class Languages {
    private static ConcurrentMap<String, Language> languages = new ConcurrentMap<>();

    static {
        load();
    }

    /**
     *
     */
    private static void load(){
        String resourcePath = "/j/I18N/full_bcp47_languages.json";

        try(InputStream inputStream = Languages.class.getResourceAsStream(resourcePath)){
            if(inputStream==null){
                System.out.println("resource not found => " + resourcePath);
                return;
            }
            String json = JUtilInputStream.string(inputStream, "UTF-8");
            if(JUtilString.isBlank(json)) return;

            JSONArray array = JUtilJSON.array(json);
            for(int i=0; i<array.length(); i++){
                Language language = (Language)JUtilBean.json2Bean(Language.class, JUtilJSON.get(array, i));
                if(JUtilString.isBlank(language.getBcp47Code())) continue;
                if("无语言内容".equals(language.getChineseName())) continue;

                //无NativeName时，使用英文名
                if(language.getChineseName().equals(language.getNativeName())){
                    language.setNativeName(language.getEnglishName());
                }

                language.setBaseCodeFromBcp47Code();
                languages.put(language.getBcp47Code().toUpperCase(), language);

                System.out.println("load language info => " + language);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     */
    public static List<Language> getLanguages(){
        return languages.listValues();
    }

    /**
     *
     * @param bcp47Code
     * @return
     */
    public static Language getLanguage(String bcp47Code){
        return JUtilString.isBlank(bcp47Code) ? null : languages.get(bcp47Code.toUpperCase());
    }

    public static void main(String[] args){
        System.out.println(getLanguage("zh-Hans"));
    }
}
