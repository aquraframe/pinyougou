package com.pinyougou.search.service.impl;

import java.util.Arrays;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemDeleteListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;
	
	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = (ObjectMessage)message;
			Long[] goodsids = (Long[]) objectMessage.getObject();
			System.out.println("ItemDeleteLinstener监听接受到消息");
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodsids));
			System.out.println("成功删除索引库中的记录");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
