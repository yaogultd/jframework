package j.core.fs.storage;

import j.core.annotation.description.ClassDescription;
import lombok.Getter;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

@ClassDescription(author = "肖炯", date = "2022/04/19", description = "文件存储策略")
@Getter
public class StoragePolicy {
    //ID
    private String id;

    //文件存储列表
    private List<Storage> storages=new ArrayList<>();

    /**
     *
     * @param config
     * @throws Exception
     */
    public StoragePolicy(Element config) throws Exception{
        this.id=config.attributeValue("id");

        List<Element> storageElements=config.elements("storage");
        for(int i=0; storageElements!=null && i<storageElements.size(); i++){
            Element e=storageElements.get(i);
            String supplier=e.attributeValue("supplier");
            try{
                Storage storage=(Storage)Class.forName(supplier).getConstructor(List.class).newInstance(e.elements("property"));
                this.storages.add(storage);
            }catch (Exception ex){
                throw new ClassNotFoundException("class "+supplier+" is not found.");
            }
        }
    }
}
