package j.core.cache;

import j.util.ConcurrentList;
import j.util.ConcurrentMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class JCacheUnitList extends JCacheUnit{
	private ConcurrentList container=null;

	/**
	 *
	 * @param lifeCircle
	 * @param timeout
	 * @param initializer
	 * @throws Exception
	 */
	public JCacheUnitList(int lifeCircle, long timeout, JCacheInitializer initializer) throws Exception{
		super(lifeCircle, timeout, initializer);
		this.container=new ConcurrentList();
		this.init();
	}

	@Override
	public int getUnitType(){
		return JCache.UNIT_LIST;
	}

	@Override
	public void addOne(Object value) throws Exception{
		using();
		container.add(value);
	}

	@Override
	public void addOneIfNotContains(Object value) throws Exception{
		using();
		if(!container.contains(value)) container.add(value);
	}

	@Override
	public void addAll(Collection values) throws Exception{
		using();
		this.container.addAll(values);
	}

	@Override
	public boolean contains(JCacheParams params) throws Exception{
		using();
		
		if(params==null||(params.value==null&&params.valueFilter==null)){
			throw new Exception("no valid operating params.");
		}
		
		if(params.value!=null) return container.contains(params.value);
		
		if(params.valueFilter!=null){
			for(int i=0;i<container.size();i++){
				if(params.valueFilter.matches(container.get(i))) return true;
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
		ConcurrentList values=(ConcurrentList)this.sub(params);
		return values==null?0:values.size();
	}

	@Override
	public Object get(JCacheParams params) throws Exception{
		using();
		
		if(params==null||(params.index<0&&params.valueFilter==null)){
			throw new Exception("no valid operating params.");
		}
		
		if(params.index>=0) return container.get(params.index);
		
		if(params.valueFilter!=null){
			for(int i=0;i<container.size();i++){
				if(params.valueFilter.matches(container.get(i))) return container.remove(i);
			}
		}
		
		return null;
	}


	@Override
	public void remove(JCacheParams params) throws Exception{
		using();
		
		if(params==null||(params.index<0&&params.value==null&&params.valueFilter==null)){
			throw new Exception("no valid operating params.");
		}
		
		if(params.index>=0){
			container.remove(params.index);
			return;
		}
		
		if(params.value!=null){
			container.remove(params.value);
			return;
		}
		
		if(params.valueFilter!=null){
			for(int i=0;i<container.size();i++){
				if(params.valueFilter.matches(container.get(i))){
					container.remove(i);
					i--;
				}
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
		
		if(params!=null){
			ConcurrentList values=new ConcurrentList(true, (List)container.getContainerType().newInstance());

			for(int i=0;i<values.size();i++){
				Object value=values.get(i);

				if(params.valueFilter==null || params.valueFilter.matches(value)){
					values.add(value);
				}
			}
			
			if(params.sorter!=null){//排序
				values=(ConcurrentList) params.sorter.mergeSort(values,params.sortType);
			}

			if(params.fromIndex>=0 && params.toIndex > params.fromIndex){//指定起始位置
				int start=params.fromIndex;
				int to=params.toIndex;
				if(start>=0){
					if(values.size()>start){
						List temp=values.subList(start,to>values.size()?values.size():to);
						values.clear();
						values.addAll(temp);
					}else{
						values.clear();
					}
				}
			}

			int total=values.size();
			if(params!=null && params.recordsPerPage>0 && params.pageNum>0){//分页
				int start=params.recordsPerPage*(params.pageNum-1);
				int to=params.recordsPerPage*params.pageNum;
				
				if(start>=0){
					if(values.size()>start){
						List temp=values.subList(start,to>values.size()?values.size():to);
						values.clear();
						values.addAll(temp);
					}else{
						values.clear();
					}
				}
			}
			values.setTotal(total);

			return values;
		}

		return container.snapshot();
	}

	@Override
	public void addOne(Object key, Object value) throws Exception {
		throw new Exception("Not Supported.");
	}

	@Override
	public void addAll(Map mappings) throws Exception {
		throw new Exception("Not Supported.");
	}

	@Override
	public ConcurrentList keys(JCacheParams params) throws Exception {
		throw new Exception("Not Supported.");
	}

	@Override
	public ConcurrentList values(JCacheParams params) throws Exception {
		throw new Exception("Not Supported.");
	}
}
