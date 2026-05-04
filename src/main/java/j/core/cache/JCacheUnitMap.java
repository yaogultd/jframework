package j.core.cache;

import j.core.annotation.description.FieldDescription;
import j.core.cache.storage.cluster.StorageService;
import j.core.nvwa.Nvwa;
import j.core.type.index.IndexCreator;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilSorter;
import j.util.JUtilString;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class JCacheUnitMap extends JCacheUnit{
	@FieldDescription(description = "日志输出")
	private static Logger log = Logger.create(JCacheUnitMap.class);

	private ConcurrentMap container=null;

	/**
	 *
	 * @param lifeCircle
	 * @param timeout
	 * @param initializer
	 * @throws Exception
	 */
	public JCacheUnitMap(int lifeCircle, long timeout, JCacheInitializer initializer) throws Exception{
		super(lifeCircle, timeout, initializer);
		this.container=new ConcurrentMap();
		this.init();
	}

	@Override
	public int getUnitType(){
		return JCache.UNIT_MAP;
	}

	@Override
	public void setIndexCreator(IndexCreator indexCreator){
		super.setIndexCreator(indexCreator);
		this.container.setIndexCreator(indexCreator);
	}

	@Override
	public void addOne(Object key,Object value) throws Exception{
		using();
		container.put(key, value);
	}


	@Override
	public void addAll(Map mappings) throws Exception{
		using();
		this.container.putAll(mappings);
	}


	@Override
	public boolean contains(JCacheParams params) throws Exception{
		using();

		if(params==null||(params.key==null&&params.value==null&&params.keyFilter==null&&params.valueFilter==null)){
			return false;
		}
		
		if(params.key!=null) return container.containsKey(params.key);
		
		if(params.value!=null) return container.containsValue(params.value);
		
		if(params.keyFilter!=null){//是否有匹配指定key的
			List keys=container.listKeys();
			for(int i=0;i<keys.size();i++){
				if(params.keyFilter.matches(keys.get(i))){
					return true;
				}
			}
		}
		
		if(params.valueFilter!=null){//是否有匹配指定value的
			List values=container.listValues();
			for(int i=0;i<values.size();i++){
				if(params.valueFilter.matches(values.get(i))){
					return true;
				}
			}
		}
		
		return false;
	}


	@Override
	public int size() throws Exception{
		using();
		
		return container.size();
	}


	@Override
	public int size(JCacheParams params) throws Exception{
		return this.values(params).size();
	}


	@Override
	public Object get(JCacheParams params) throws Exception{
		using();
		
		if(params==null
				||(params.key==null && params.keyFilter==null && params.valueFilter==null && params.index<0 && JUtilString.isBlank(params.indexType))){
			return null;
		}

		if(params.key!=null) return container.get(params.key);

		if(!JUtilString.isBlank(params.indexType)){
			return container.get(params.indexType, params.matchType, params.indexValue);
		}
		
		if(params.index>-1){
			List values=container.listValues();
			return values.get(params.index);
		}
		
		if(params.keyFilter!=null){//是否有匹配指定key的
			List keys=container.listKeys();
			for(int i=0;i<keys.size();i++){
				if(params.keyFilter.matches(keys.get(i))){
					return container.get(keys.get(i));
				}
			}
		}
		
		if(params.valueFilter!=null){//是否有匹配指定value的
			List values=container.listValues();
			for(int i=0;i<values.size();i++){
				Object value=values.get(i);
				if(params.valueFilter.matches(value)){
					return value;
				}
			}
		}
		
		return null;
	}


	@Override
	public void remove(JCacheParams params) throws Exception{
		using();
		
		if(params==null||(params.key==null&&params.keyFilter==null&&params.valueFilter==null)){
			throw new Exception("no valid operating params.");
		}
		
		if(params.key!=null){
			container.remove(params.key);
			return;
		}
		
		if(params.keyFilter!=null){//是否有匹配指定key的
			List keys=container.listKeys();
			for(int i=0;i<keys.size();i++){
				Object key=keys.get(i);
				if(params.keyFilter.matches(key)) container.remove(key);
			}
		}
		
		if(params.valueFilter!=null){//是否有匹配指定value的
			List keys=container.listKeys();
			for(int i=0;i<keys.size();i++){
				Object key=keys.get(i);
				Object value=container.get(key);
				if(params.valueFilter.matches(value)) container.remove(key);
			}
		}
	}


	@Override
	public void clear() throws Exception{
		container.clear();
	}


	@Override
	public void update(JCacheParams params) throws Exception{
		using();
		
		if(params==null||(params.updater==null)){
			throw new Exception("no valid operating params.");
		}
		
		params.updater.update(container);
	}


	@Override
	public void updateCollection(JCacheParams params) throws Exception{
		using();
		
		if(params==null||(params.collectionUpdater==null)){
			throw new Exception("no valid operating params.");
		}
		
		params.collectionUpdater.updateCollection(container);
	}


	@Override
	public Object sub(JCacheParams params) throws Exception{
		using();
		
		JCacheFilter keyFileter=params==null?null:params.keyFilter;
		JCacheFilter valueFilter=params==null?null:params.valueFilter;
		
		ConcurrentMap mappings=new ConcurrentMap(true, (Map)container.getContainerType().newInstance());

		if(keyFileter!=null || valueFilter!=null){
			List keys=container.listKeys();
			for(int i=0;i<keys.size();i++){
				Object key=keys.get(i);
				Object value=container.get(key);

				boolean matches=true;
				if(keyFileter!=null&&!keyFileter.matches(key)){
					matches=false;
				}else if(valueFilter!=null&&!valueFilter.matches(value)){
					matches=false;
				}
				if(matches) mappings.put(key, value);
			}
		}else{
			mappings.putAll(container);
		}
		
		mappings.setTotal(mappings.size());
		if(params!=null && params.recordsPerPage>0&&params.pageNum>0){//分页
			int start=params.recordsPerPage*(params.pageNum-1);
			int to=params.recordsPerPage*params.pageNum;
			
			if(start>=0){
				if(mappings.size()>start){
					mappings=ConcurrentMap.subConcurrentMap(mappings,start,to>mappings.size()?mappings.size():to);
				}else{
					mappings.clear();
				}
			}
		}
		
		return mappings;
	}


	@Override
	public ConcurrentList keys(JCacheParams params) throws Exception{
		using();
		
		JCacheFilter keyFileter=params==null?null:params.keyFilter;
		JCacheFilter valueFilter=params==null?null:params.valueFilter;
		int recordsPerPage=params==null?0:params.recordsPerPage;
		int pageNum=params==null?0:params.pageNum;

		ConcurrentList keys=container.listKeys();
		if(keyFileter!=null||valueFilter!=null){	
			for(int i=0;i<keys.size();i++){
				Object key=keys.get(i);
				Object value=container.get(key);
				
				boolean remove=false;
				if(keyFileter!=null&&!keyFileter.matches(key)){
					remove=true;
				}else if(valueFilter!=null&&!valueFilter.matches(value)){
					remove=true;
				}
				if(remove){
					keys.remove(i);
				}
			}
		}

		keys.setTotal(keys.size());
		if(recordsPerPage>0&&pageNum>0){//分页
			int start=recordsPerPage*(pageNum-1);
			int to=recordsPerPage*pageNum;
			
			if(start>=0){
				if(keys.size()>start){
					keys=ConcurrentList.subConcurrentList(keys,start,to>keys.size()?keys.size():to);
				}else{
					keys.clear();
				}
			}
		}

		return keys;
	}


	@Override
	public ConcurrentList values(JCacheParams params) throws Exception{
		using();
		
		JCacheFilter keyFileter=params==null?null:params.keyFilter;
		JCacheFilter valueFilter=params==null?null:params.valueFilter;
		JUtilSorter sorter=params==null?null:params.sorter;
		String sortType=params==null?null:params.sortType;
		int recordsPerPage=params==null?0:params.recordsPerPage;
		int pageNum=params==null?0:params.pageNum;

		ConcurrentList values=null;


		if(params!=null && !JUtilString.isBlank(params.indexType)){
			values=container.values(params.indexType, params.matchType, params.indexValue);
		}else if(keyFileter!=null||valueFilter!=null){
			values=new ConcurrentList();
			
			List keys=container.listKeys();
			for(int i=0;i<keys.size();i++){
				Object key=keys.get(i);
				Object value=container.get(key);
				
				boolean remove=false;
				if(keyFileter!=null&&!keyFileter.matches(key)){
					remove=true;
				}else if(valueFilter!=null&&!valueFilter.matches(value)){
					remove=true;
				}
				if(!remove){
					values.add(value);
				}
			}
			keys.clear();
			keys=null;
		}else{
			values=container.listValues();
		}
		
		if(sorter!=null){//排序
			values=(ConcurrentList)sorter.mergeSort(values, sortType);
		}

		int total=values.size();
		if(recordsPerPage>0&&pageNum>0){//分页
			int start=recordsPerPage*(pageNum-1);
			int to=recordsPerPage*pageNum;
			
			if(start>=0){
				if(values.size()>start){
					values=ConcurrentList.subConcurrentList(values,start,to>values.size()?values.size():to);
				}else{
					values.clear();
				}
			}
		}
		values.setTotal(total);

		return values;
	}


	@Override
	public void addOne(Object value) throws Exception {
		throw new Exception("Not Supported.");
	}


	@Override
	public void addOneIfNotContains(Object value) throws Exception{
		throw new Exception("Not Supported.");
	}

	@Override
	public void addAll(Collection values) throws Exception {
		throw new Exception("Not Supported.");
	}
}
