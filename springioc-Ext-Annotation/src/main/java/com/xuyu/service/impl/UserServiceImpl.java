package com.xuyu.service.impl;

import com.xuyu.service.OrderService;
import com.xuyu.service.UserService;
import com.xuyu.spring.annotation.ExtResource;
import com.xuyu.spring.annotation.ExtService;

//user 服务层
@ExtService
public class UserServiceImpl implements UserService {
	@ExtResource
	private OrderService orderServiceImpl;
	public void add() {
		System.out.println("使用Java反射机制初始化对象");
	}
}
