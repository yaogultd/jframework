package j.core.dao.partition;

import j.core.annotation.description.ClassDescription;

@ClassDescription(author = "肖炯",
		date = "2022/06/30",
		description = "分表边界")
public class TablePartitionBounds {
	public String column;
	public long start=-1;
	public long end=-1;

	/**
	 *
	 * @param column
	 * @param start
	 * @param end
	 */
	public TablePartitionBounds(String column, long start, long end) {
		this.column=column;
		this.start=start;
		this.end=end;
	}
}
