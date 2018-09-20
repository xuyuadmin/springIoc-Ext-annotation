package com.xuyu.service;

import com.xuyu.spring.ext.ExtClassPathXmlApplicationContext;

public class Test001 {

	public static void main(String[] args) throws Exception {
		ExtClassPathXmlApplicationContext app = new ExtClassPathXmlApplicationContext("com.xuyu.service.impl");
		UserService userService = (UserService) app.getBean("userServiceImpl");
		userService.add();
		System.out.println(userService);
	}
}
