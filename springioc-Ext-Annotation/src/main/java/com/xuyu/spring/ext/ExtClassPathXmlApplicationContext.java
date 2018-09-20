package com.xuyu.spring.ext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.xuyu.spring.annotation.ExtService;
import com.xuyu.utils.ClassUtils;

/**
 * 手写Spring专题 注解版本注入bean
 * 思想：依赖注入原理
 * 1.使用Java反射机制获取当前包下的所有类
 * 2.判断类上是否存在注入bean的注解
 * 3.使用Java反射机制初始化对象，获得父类名字
 * 4.使用beanId查找对应的bean对象
 * 5.存在bean对象就可以使用反射机制获取类的属性，赋值信息
 * 		①通过类的信息去获取类的属性名称
 * 		②使用属性名称查找bean容器进行赋值
 */
public class ExtClassPathXmlApplicationContext {
	
	//初始化bean容器
	ConcurrentHashMap<String, Object> initBean = null;
	private String packageName;
	public ExtClassPathXmlApplicationContext(String packageName) {
		this.packageName=packageName;
	}
	//【一】.使用java反射机制获取当前包下的所有类
	public List<Class> findClassExistService() throws Exception {
		//1.判断包地址是否为空
		if(StringUtils.isEmpty(packageName)) {
			throw new Exception("扫包地址不能为空！");
		}
		//2.地址不为空，则使用Java反射机制获取当前包下的所有类
		List<Class<?>> classByPackageName = ClassUtils.getClasses(packageName);
		//【二】：判断类上是否存在bean的注解
		//1.定义一个集合来存有注解的类信息
		List<Class> extServiceArrayList = new ArrayList<Class>();
		for (Class<?> classInfo : classByPackageName) {
			ExtService extService = classInfo.getDeclaredAnnotation(ExtService.class);
			if(extService!=null) {
				//2.把存在bean注解的类加到集合中
				extServiceArrayList.add(classInfo);
				continue;
			}
		}
		//3.返回存在bean注解的类信息集合
		return extServiceArrayList;
	}
	//【三】：使用Java反射机制初始化存在bean注解的类的信息集合对象
	public ConcurrentHashMap<String, Object> initBean(List<Class>extServiceArrayList) throws Exception {
		ConcurrentHashMap<String, Object> cp = new ConcurrentHashMap<String,Object>();
		for (Class classInfo : extServiceArrayList) {
			//1.初始化对象
			Object parentBeanObject = classInfo.newInstance();
			//2.获得父类名称,调用【toLowerCaseFirstOne】首字母转小写方法 得到父类beanId
			String parentBeanId = toLowerCaseFirstOne(classInfo.getSimpleName());
			//3.将获取到的父类beanId和对应的父类初始化对象存在map集合中
			cp.put(parentBeanId, parentBeanObject);
		}
		//4.返回存放类的beanId和类的对象信息的map集合信息
		return cp;
	}
	
	//【四】：使用parentBeanId查找parentBeanObject对象
	public Object getBean(String beanId) throws Exception {
		//1.调用【1】方法获取当前包下的所有类
		List<Class> findClassExistService = findClassExistService();
		//2.判断类上是否存在bean注解
		if(findClassExistService==null || findClassExistService.isEmpty()) {
			throw new Exception("没有找着存在bean注解的类");
		}
		//3.存在bean注解就调用【3,initBean】方法初始化bean对象
		initBean = initBean(findClassExistService);
		if(initBean==null || initBean.isEmpty()) {
			throw new  Exception("初始化bean为空！");
		}
		//4.初始化对象成功，使用beanId查找对应bean对象
		Object beanObject = initBean.get(beanId);
		//5.调用方法【attriAssign】使用Java反射机制读取类的信息，赋值信息
		attriAssign(beanObject);
		return beanObject;
	}
	//使用反射机制读取类的属性，赋值信息
	public void attriAssign(Object object) throws Exception {
		//1.获取类的信息
		Class<? extends Object> classInfo = object.getClass();
		//2.获取类对应的属性
		Field[] declaredFields = classInfo.getDeclaredFields();
		//3.循环类属性
		for (Field field : declaredFields) {
			//4.获得属性名称
			String fieldName = field.getName();
			//5.使用属性名称查找bean容器赋值
			Object beanObject = initBean.get(fieldName);
			if(beanObject!=null) {
				//暴力反射
				field.setAccessible(true);
				//给属性赋值
				field.set(object, beanObject);
				continue;
			}
		}
	}
	//父类名称首字母转小写
	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}
	
	
}
