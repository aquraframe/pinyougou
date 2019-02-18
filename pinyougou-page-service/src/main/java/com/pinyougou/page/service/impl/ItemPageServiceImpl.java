package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class ItemPageServiceImpl implements ItemPageService{
	
	@Value("${pagedir}")
	private String pagedir;
	
	@Autowired
	private FreeMarkerConfigurer freemarkerConfig;
	
	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Override
	public boolean genItemHtml(Long goodsId) {
		try {
			 Configuration configuration = freemarkerConfig.getConfiguration();
			 Template template =configuration.getTemplate("item.ftl");
			 Map dataModel = new HashMap<>();
			 TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			 dataModel.put("goods", goods);
			 
			 TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			 dataModel.put("goodsDesc", goodsDesc);
			 
			 String category1Id = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			 String category2Id = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			 String category3Id = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			 dataModel.put("category1Id", category1Id);
			 dataModel.put("category2Id", category2Id);
			 dataModel.put("category3Id", category3Id);
			 
			 TbItemExample example  = new TbItemExample();
			 Criteria criteria = example.createCriteria();
			 criteria.andGoodsIdEqualTo(goodsId);
			 criteria.andStatusEqualTo("1");
			 example.setOrderByClause("is_default desc");
			 List<TbItem> itemList = itemMapper.selectByExample(example);
			 dataModel.put("itemList", itemList);	
			 Writer out = new FileWriterWithEncoding(pagedir+goodsId+".html","utf-8");		
			 template.process(dataModel, out);
			 out.close();
			 return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteItemHtml(Long[] goodsIds) {
		try {
			for (Long goodsId : goodsIds) {
				new File(pagedir+goodsId+".html").delete();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
