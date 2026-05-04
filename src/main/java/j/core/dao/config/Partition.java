package j.core.dao.config;

import j.util.ConcurrentList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Partition {
    private String dbName;
    private ConcurrentList<String> tableNames=new ConcurrentList<>();

    /**
     *
     * @param dbName
     */
    public Partition(String dbName){
        this.dbName=dbName;
    }

    /**
     *
     * @param tableName
     */
    public void addTable(String tableName){
        this.tableNames.add(tableName.toLowerCase());
    }

    /**
     *
     * @param tableName
     * @return
     */
    public boolean responsible(String tableName){
        return this.tableNames.contains(tableName.toLowerCase());
    }
}
