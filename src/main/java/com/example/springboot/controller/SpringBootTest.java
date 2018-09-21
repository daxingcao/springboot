package com.example.springboot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringBootTest {

	@RequestMapping("/test")
	public String helloWorld(String json) {
		System.out.println(json);
		return json;
	}
	
	public enum SystemInfo {
		ADD(1,"增加"),
		DEL(2,"删除"),
		UPDATE(3,"更新"),
		SELECT(4,"查询");
		
		private Integer index;
		private String value;
		
		SystemInfo (Integer index, String value){
			this.index = index;
			this.value = value;
		}

		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
	}
	
	public static void main(String[] args) {
		SystemInfo valueOf = SystemInfo.valueOf(SystemInfo.class, "ADD");
		System.out.println(SystemInfo.class.getEnumConstants()[1]);
		System.out.println(SystemInfo.ADD.getValue());
	}
	
}
