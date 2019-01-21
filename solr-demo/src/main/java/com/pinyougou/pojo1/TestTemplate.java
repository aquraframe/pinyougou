package com.pinyougou.pojo1;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-solr.xml")
public class TestTemplate {

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Test
	public void testAdd() {
		TbItem item = new TbItem();
		item.setId(1L);
		item.setBrand("华为");
		item.setCategory("手机");
		item.setGoodsId(1L);
		item.setSeller("华为手机旗舰店");
		item.setTitle("华为9");
		item.setPrice(new BigDecimal(2000));
		solrTemplate.saveBean(item);
		solrTemplate.commit();
	}
	
	@Test
	public void testFindOne() {
		TbItem item = solrTemplate.getById(1,TbItem.class);
		System.out.println(item.getTitle()+" "+item.getCategory() );
		
	}
	
	@Test
	public void dele() {
		solrTemplate.deleteById("1");
		solrTemplate.commit();
	}
	
	@Test
	public void addList() {
		List<TbItem> list = new ArrayList<TbItem>();
		for (int i = 0; i < 100; i++) {
			TbItem item = new TbItem();
			item.setId(1L+i);
			item.setBrand("华为");
			item.setCategory("手机");
			item.setGoodsId(1L);
			item.setSeller("华为手机旗舰店");
			item.setTitle("华为"+i);
			item.setPrice(new BigDecimal(2000+100*i));
			list.add(item);
		}
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}
	
	@Test
	public void testPageQuery() {
		Query query = new SimpleQuery("*:*");
		query.setOffset(20);
		query.setRows(15);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		System.out.println(page.getTotalElements());
		System.out.println(page.getTotalPages());
		for (TbItem tbItem : page.getContent()) {
			System.out.println(tbItem.getTitle()+" "+tbItem.getPrice());
		}
	}
	
	@Test
	public void testPageQuerymutil() {
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_title").contains("2");
		criteria=criteria.and("item_title").contains("5");
		query.addCriteria(criteria);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		System.out.println(page.getTotalElements());
		System.out.println(page.getTotalPages());
		for (TbItem tbItem : page.getContent()) {
			System.out.println(tbItem.getTitle()+" "+tbItem.getPrice());
		}
	}
	
	@Test
	public void testDeleteAll() {
		Query query = new SimpleQuery("*:*");
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
