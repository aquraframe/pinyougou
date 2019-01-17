package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	
	@RequestMapping("/findAll.do")
	public List<TbBrand> findAll(){
		return brandService.findAll();		
	}
	
	@RequestMapping("/findPage.do")
	public PageResult findPage(int page,int rows) {
		return brandService.findPage(page, rows);
	}
	
	@RequestMapping("/add.do")
	public Integer add(@RequestBody TbBrand brand) {
		return brandService.add(brand);
	}
	
	@RequestMapping("/findOne.do")
	public TbBrand findOne(Long id){
		return brandService.findOne(id);
	}
	
	@RequestMapping("/update.do")
	public Integer update(@RequestBody TbBrand brand) {
		return brandService.update(brand);
	}
	
	@RequestMapping("/delete.do")
	public void delete(long[] ids) {
		brandService.delete(ids);
	}
	
	@RequestMapping("/search.do")
	public PageResult search(@RequestBody TbBrand brand,int page,int rows){
		return brandService.findPage(brand, page, rows);		
	}
	@RequestMapping("/selectOptionlist")
	public List<Map> selectOptionlist(){
		return brandService.selectOptionlist();
	};
}
