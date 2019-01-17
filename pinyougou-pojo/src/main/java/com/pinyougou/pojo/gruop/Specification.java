package com.pinyougou.pojo.gruop;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

public class Specification implements Serializable{

	private TbSpecification tbSpecification;
	private List<TbSpecificationOption> list;
	
	public TbSpecification getTbSpecification() {
		return tbSpecification;
	}
	public void setTbSpecification(TbSpecification tbSpecification) {
		this.tbSpecification = tbSpecification;
	}
	public List<TbSpecificationOption> getList() {
		return list;
	}
	public void setList(List<TbSpecificationOption> list) {
		this.list = list;
	}
	
	
}
