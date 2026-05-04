package j.core.dao.intervener;

/**
 * 从数据库查询到某行后，根据业务逻辑对该行数据进行干预（修改）
 * @author Genie
 *
 */
public interface RowIntervener {
	/**
	 * 根据业务逻辑对该行数据进行干预（修改）
	 * @param original 原始行数据
	 * @param datas 业务自定义参数
	 * @return
	 */
	public Object update(Object original, Object[] datas);
}
