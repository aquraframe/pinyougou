package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.gruop.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Reference
	private CartService cartService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		String cartlistString = util.CookieUtil.getCookieValue(request, "cartList","utf-8");
		if (cartlistString == null || cartlistString.equals("")) {
			cartlistString = "[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartlistString,Cart.class);	
		if (username.equals("anonymousUser")) {
				return cartList_cookie;
		}else {
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			if (cartList_cookie.size() > 0) {
				cartList_redis = cartService.mergeCartList(cartList_cookie, cartList_redis);
				util.CookieUtil.deleteCookie(request, response, "cartList");
				cartService.saveCartListToRedis(username, cartList_redis);	
			}
			return cartList_redis;
		}
		
	}
	
	@CrossOrigin("http://localhost:9105")
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num) {
//		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
//		response.setHeader("Access-Control-Allow-Credentials", "true");
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			List<Cart> cartList = findCartList();
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);			
			if (username.equals("anonymousUser")) {
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList),3600*24,"utf-8");				
			}else {
				cartService.saveCartListToRedis(username, cartList);
			}
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
