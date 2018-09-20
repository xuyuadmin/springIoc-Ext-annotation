package com.xuyu.spring.ext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.xuyu.spring.annotation.ExtService;
import com.xuyu.utils.ClassUtils;

/**
 * ��дSpringר�� ע��汾ע��bean
 * ˼�룺����ע��ԭ��
 * 1.ʹ��Java������ƻ�ȡ��ǰ���µ�������
 * 2.�ж������Ƿ����ע��bean��ע��
 * 3.ʹ��Java������Ƴ�ʼ�����󣬻�ø�������
 * 4.ʹ��beanId���Ҷ�Ӧ��bean����
 * 5.����bean����Ϳ���ʹ�÷�����ƻ�ȡ������ԣ���ֵ��Ϣ
 * 		��ͨ�������Ϣȥ��ȡ�����������
 * 		��ʹ���������Ʋ���bean�������и�ֵ
 */
public class ExtClassPathXmlApplicationContext {
	
	//��ʼ��bean����
	ConcurrentHashMap<String, Object> initBean = null;
	private String packageName;
	public ExtClassPathXmlApplicationContext(String packageName) {
		this.packageName=packageName;
	}
	//��һ��.ʹ��java������ƻ�ȡ��ǰ���µ�������
	public List<Class> findClassExistService() throws Exception {
		//1.�жϰ���ַ�Ƿ�Ϊ��
		if(StringUtils.isEmpty(packageName)) {
			throw new Exception("ɨ����ַ����Ϊ�գ�");
		}
		//2.��ַ��Ϊ�գ���ʹ��Java������ƻ�ȡ��ǰ���µ�������
		List<Class<?>> classByPackageName = ClassUtils.getClasses(packageName);
		//���������ж������Ƿ����bean��ע��
		//1.����һ������������ע�������Ϣ
		List<Class> extServiceArrayList = new ArrayList<Class>();
		for (Class<?> classInfo : classByPackageName) {
			ExtService extService = classInfo.getDeclaredAnnotation(ExtService.class);
			if(extService!=null) {
				//2.�Ѵ���beanע�����ӵ�������
				extServiceArrayList.add(classInfo);
				continue;
			}
		}
		//3.���ش���beanע�������Ϣ����
		return extServiceArrayList;
	}
	//��������ʹ��Java������Ƴ�ʼ������beanע��������Ϣ���϶���
	public ConcurrentHashMap<String, Object> initBean(List<Class>extServiceArrayList) throws Exception {
		ConcurrentHashMap<String, Object> cp = new ConcurrentHashMap<String,Object>();
		for (Class classInfo : extServiceArrayList) {
			//1.��ʼ������
			Object parentBeanObject = classInfo.newInstance();
			//2.��ø�������,���á�toLowerCaseFirstOne������ĸתСд���� �õ�����beanId
			String parentBeanId = toLowerCaseFirstOne(classInfo.getSimpleName());
			//3.����ȡ���ĸ���beanId�Ͷ�Ӧ�ĸ����ʼ���������map������
			cp.put(parentBeanId, parentBeanObject);
		}
		//4.���ش�����beanId����Ķ�����Ϣ��map������Ϣ
		return cp;
	}
	
	//���ġ���ʹ��parentBeanId����parentBeanObject����
	public Object getBean(String beanId) throws Exception {
		//1.���á�1��������ȡ��ǰ���µ�������
		List<Class> findClassExistService = findClassExistService();
		//2.�ж������Ƿ����beanע��
		if(findClassExistService==null || findClassExistService.isEmpty()) {
			throw new Exception("û�����Ŵ���beanע�����");
		}
		//3.����beanע��͵��á�3,initBean��������ʼ��bean����
		initBean = initBean(findClassExistService);
		if(initBean==null || initBean.isEmpty()) {
			throw new  Exception("��ʼ��beanΪ�գ�");
		}
		//4.��ʼ������ɹ���ʹ��beanId���Ҷ�Ӧbean����
		Object beanObject = initBean.get(beanId);
		//5.���÷�����attriAssign��ʹ��Java������ƶ�ȡ�����Ϣ����ֵ��Ϣ
		attriAssign(beanObject);
		return beanObject;
	}
	//ʹ�÷�����ƶ�ȡ������ԣ���ֵ��Ϣ
	public void attriAssign(Object object) throws Exception {
		//1.��ȡ�����Ϣ
		Class<? extends Object> classInfo = object.getClass();
		//2.��ȡ���Ӧ������
		Field[] declaredFields = classInfo.getDeclaredFields();
		//3.ѭ��������
		for (Field field : declaredFields) {
			//4.�����������
			String fieldName = field.getName();
			//5.ʹ���������Ʋ���bean������ֵ
			Object beanObject = initBean.get(fieldName);
			if(beanObject!=null) {
				//��������
				field.setAccessible(true);
				//�����Ը�ֵ
				field.set(object, beanObject);
				continue;
			}
		}
	}
	//������������ĸתСд
	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}
	
	
}
