package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.gruop.Cart;

@Service
public class CartServiceImpl implements CartService{
	
	@Autowired
	private TbItemMapper itemMapper;
	
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, int num) {
		//1.������ƷSKU ID��ѯSKU��Ʒ��Ϣ
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item == null) {
			throw new RuntimeException("��Ʒ������");
		}
		
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("��Ʒ״̬���Ϸ�");
		}
		//2.��ȡ�̼�ID
		String sellerId = item.getSellerId();
		//3.�����̼�ID�жϹ��ﳵ�б����Ƿ���ڸ��̼ҵĹ��ﳵ
		Cart cart = searchCartBySellerid(cartList, sellerId);
		
		//4.������ﳵ�б��в����ڸ��̼ҵĹ��ﳵ
		if (cart == null) {
			//4.1�½����ﳵ����
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			TbOrderItem orderItem = createOrderItem(item, num);
			List orderItemList = new ArrayList();
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			//4.2���½��Ĺ��ﳵ������ӵ����ﳵ�б�
			cartList.add(cart);
		}else { //5.������ﳵ�б��д��ڸ��̼ҵĹ��ﳵ
			// ��ѯ���ﳵ��ϸ�б����Ƿ���ڸ���Ʒ
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			if (orderItem == null) {
				//5.1. ���û�У��������ﳵ��ϸ
				orderItem = createOrderItem(item, num);
				cart.getOrderItemList().add(orderItem);
			}else {
				//5.2. ����У���ԭ���ﳵ��ϸ��������������Ľ��
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum())));
				if (orderItem.getNum() <= 0) {
					cart.getOrderItemList().remove(orderItem);
				}
				if (cart.getOrderItemList().size() == 0) {
					cartList.remove(cart);
				}
			}
		}
		return cartList;
	}

	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {		
			for (TbOrderItem orderItem : orderItemList) {
				if (orderItem.getItemId().longValue() == itemId.longValue()) {
					return orderItem;
				}
			}
			return null;
		
	}

	private Cart searchCartBySellerid(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	
	private TbOrderItem createOrderItem(TbItem item,Integer num) {
		if (num <= 0) {
			throw new RuntimeException("�����Ƿ�");
		}
		
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(num)));
		return orderItem;
	}

	@Autowired
	private RedisTemplate redisTemplate;
	
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("��redis�ڻ�ȡ���ﳵ����"+username);
		List<Cart> cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
		if (cartList == null) {
			cartList = new ArrayList<Cart>();
		}
		return cartList;
	}

	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("��redis�д��빺�ﳵ��Ϣ"+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);		
	}
	
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2) {
		for (Cart cart : cartList1) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				addGoodsToCartList(cartList2, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList2;
	}
}
