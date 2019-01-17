package entity;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbBrand;

public class PageResult implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long total;
	private List rows;
	
	public PageResult(long total, List rows) {
		super();
		this.total = total;
		this.rows = rows;
	}
	
	
	
	public PageResult() {
		super();
	}



	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	public List getRows() {
		return rows;
	}
	public void setRows(List rows) {
		this.rows = rows;
	}
	
	
	
	
	
	

}
