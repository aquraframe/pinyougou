package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * @author Administrator
 *
 */
public interface BrandService {

	public List<TbBrand> findAll();
	
	public PageResult findPage(int pageNum,int pageSize);
	
	public Integer add(TbBrand brand);
	
	public TbBrand findOne(Long id);
	
	public Integer update(TbBrand brand);
	
	public void delete(long[] ids);
	
	public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
	
	public List<Map> selectOptionlist();
}
