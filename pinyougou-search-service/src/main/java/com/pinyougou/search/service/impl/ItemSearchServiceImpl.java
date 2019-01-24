package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	
	@Override
	public Map<String, Object> search(Map searchMap) {
		String keywords = (String)searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		Map<String, Object> map = new HashMap<>();
//		Query query = new SimpleQuery("*:*");
//		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//		query.addCriteria(criteria);
//		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//		System.out.println(page);
//		map.put("rows", page.getContent());
		map.putAll(searchList(searchMap));
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		String categoryName = (String)searchMap.get("category");
		if (!"".equals(categoryName)) {
			map.putAll(searchBrandAndSpeclist(categoryName));
		}else {
			if (categoryList.size() >0) {
				map.putAll(searchBrandAndSpeclist(categoryList.get(0)));
			}
		}
		return map;
	}
	
	private Map searchList(Map searchMap) {
		Map map = new HashMap();
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		
		if (!"".equals(searchMap.get("category"))) {
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		if (!"".equals(searchMap.get("brand"))) {
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
	
		if (searchMap.get("spec")!=null) {
			Map<String, String> specMap = (Map)searchMap.get("spec");
			for (String key : specMap.keySet()) {
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			
		}
		
		if (!"".equals(searchMap.get("price"))) {
			String[] price = ((String)searchMap.get("price")).split("-");
		/*	if (!"0".equals(price[0])) {
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if ("*".equals(price[1])) {
				Criteria filterCriteria = new Criteria("item_price").lessThan(price[1]);				
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}*/
			FilterQuery filterQuery = new SimpleQuery("item_price:["+price[0]+" TO "+price[1]+"]");
			query.addFilterQuery(filterQuery);
		}
		
		Integer pageNo = (Integer)searchMap.get("pageNo");
		if (pageNo==null) {
			pageNo = 1;
		}
		Integer pageSize = (Integer)searchMap.get("pageSize");
		if (pageSize==null) {
			pageSize = 20;
		}
		query.setOffset((pageNo-1)*pageSize);
		query.setRows(pageSize);
		
		String sortValue =  (String) searchMap.get("sort");
		String sortField = (String) searchMap.get("sortField");
		if (sortValue!=null && sortField != null && !"".equals(sortField)) {
			if (sortValue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
				query.addSort(sort);
			}
			if (sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
			}
		}
		
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		for (HighlightEntry<TbItem>	entry : page.getHighlighted()) {
			TbItem item = entry.getEntity();
			if (entry.getHighlights().size()>0 && entry.getHighlights().get(0).getSnipplets().size()>0) {
				item.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPage", page.getTotalPages());
		map.put("total", page.getTotalElements());
		return map;
	}
	
	private List<String> searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList<>();
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		GroupPage<TbItem> page =solrTemplate.queryForGroupPage(query, TbItem.class);
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());
		}
		return list;
	}
	
	private Map searchBrandAndSpeclist(String category) {
		Map map = new HashMap();
		Long typeId = (Long)redisTemplate.boundHashOps("itemCat").get(category);
		if (typeId!=null) {
			List brandList = (List)redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);
			List specList = (List)redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);	
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List list) {
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(list);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}










