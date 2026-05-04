package j.core.dao.config;

import j.util.ConcurrentMap;
import j.util.JUtilMath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Table {
    private String name;
    private long minUuid;
    private long maxUuid;
    private String dbKeyPrefix;
    private boolean timeLoadAsLocal=false;
    private ConcurrentMap<String, Column> columns=new ConcurrentMap<>();

    /**
     *
     * @param name
     * @param minUuid
     * @param maxUuid
     * @param dbKeyPrefix
     */
    public Table(String name,String minUuid,String maxUuid,String dbKeyPrefix){
        this.name=name;
        this.minUuid= JUtilMath.isLong(minUuid)?Long.parseLong(minUuid):0;
        this.maxUuid=JUtilMath.isLong(maxUuid)?Long.parseLong(maxUuid):Long.MAX_VALUE;
        this.dbKeyPrefix=dbKeyPrefix;
    }

    /**
     *
     * @param column
     */
    public void setColumn(Column column){
        this.columns.put(column.getName().toLowerCase(), column);
    }

    /**
     *
     * @param name
     * @return
     */
    public Column getColumn(String name){
        return this.columns.get(name.toLowerCase());
    }
}
