package com.example.addressbook.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.addressbook.entity.Contact;
import com.example.addressbook.entity.ContactMethod;
import com.example.addressbook.entity.MethodType;
import com.example.addressbook.repository.ContactRepository;

/**
 * Seeds a handful of demo contacts the first time the database is created, so the UI and the
 * Excel export are never empty when the application is launched for a demo.
 */
@Configuration
public class DemoDataSeeder {

	@Bean
	ApplicationRunner seedDemoContacts(ContactRepository repository) {
		return args -> {
			if (repository.count() > 0) {
				return;
			}
			repository.save(contact("林晓", "福大科技有限公司", "产品经理", true,
					"福建省福州市鼓楼区工业路 523 号", "项目对接人，偏好微信沟通",
					method(MethodType.PHONE, "手机", "138 0013 8000"),
					method(MethodType.EMAIL, "工作邮箱", "linxiao@fzu-tech.com"),
					method(MethodType.WECHAT, "微信", "linxiao_pm")));

			repository.save(contact("陈子墨", "闽江数据服务", "后端工程师", true,
					"福建省厦门市思明区软件园二期", "接口联调负责人",
					method(MethodType.PHONE, "手机", "139 5920 1122"),
					method(MethodType.PHONE, "座机", "0592-8888 666"),
					method(MethodType.EMAIL, "邮箱", "chenzimo@mjdata.cn"),
					method(MethodType.QQ, "QQ", "88442113")));

			repository.save(contact("王雅琪", "福建师范大学", "实验室助理", false,
					"福建省福州市闽侯县上街镇", "毕设资料整理",
					method(MethodType.PHONE, "手机", "150 6001 2233"),
					method(MethodType.EMAIL, "校园邮箱", "wangyq@fjnu.edu.cn")));

			repository.save(contact("赵一帆", "星海贸易", "销售总监", false,
					"广东省深圳市南山区科技园", "周一上午例会",
					method(MethodType.PHONE, "手机", "186 8888 5566"),
					method(MethodType.EMAIL, "邮箱", "zhaoyf@xinghai.com"),
					method(MethodType.OTHER, "企业微信", "zhaoyifan-xh")));

			repository.save(contact("周宁", "个人", null, false,
					"福建省福州市台江区", "大学室友",
					method(MethodType.PHONE, "手机", "137 0500 9988"),
					method(MethodType.WECHAT, "微信", "zhouning_1998")));
		};
	}

	private Contact contact(String name, String company, String position, boolean favorite,
			String address, String notes, ContactMethod... methods) {
		Contact contact = new Contact();
		contact.setName(name);
		contact.setCompany(company);
		contact.setPosition(position);
		contact.setFavorite(favorite);
		contact.setAddress(address);
		contact.setNotes(notes);
		for (ContactMethod method : methods) {
			contact.addMethod(method);
		}
		return contact;
	}

	private ContactMethod method(MethodType type, String label, String value) {
		return new ContactMethod(type, label, value);
	}
}
