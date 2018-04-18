package com.chuanglan;

import com.chuanglan.utils.CommUtil;

public class TestMain {

	public static void main(String[] args) {
		//设置cmpp连接信息
		CMPPConfig config = new CMPPConfig();
		//cmpp账号
		config.setSpId("326598");//114857
		//密码
		config.setPassword("a.123456");

		//网关IP
		config.setServer("172.16.20.53");//192.168.0.192 106.14.241.15（旧上海）106.14.94.93（新上海） 172.16.20.53（测试）
		//网关端口
		config.setPort(7890);
		//接入号
		config.setSpNumber("10103640");
		//业务代码
		config.setServiceId("HELP");
		
		//建立客户端对象连接网关
		CMPPClient client = new CMPPClient(config);
		//连接网关
		client.doConnect();
		//等待登录完成
		client.waitLoginResp();
		if(!client.isLogin()){
			//登录失败
			System.out.println("登录失败");
			client.close();
			return;
		}
		//发送短信
		String phone = "18670306453";	//下发的电话号码
		String extNo = "520";	//自定义扩展码
		String content = "冬雪霓裳，短信测试";
		String longContent = "冬雪霓裳，验证码测试，冬雪霓裳，验证码测试，冬雪霓裳，验证码测试，冬雪霓裳，验证码测试，冬雪霓裳，验证码测试，冬雪霓裳，验证码测试，冬雪霓裳，验证码测试，冬雪霓裳，验证码测试，冬雪霓裳，验证码测试，冬雪霓裳，验证码测试";
		try {
			//每次发送，生成一个唯一的messageId
			String messageId = CommUtil.generateMessageId();
			//先发一条短的
			client.sendMessage(messageId, phone, extNo, content);
			Thread.sleep(5000);
			//再发送一条长的
			/*messageId = CommUtil.generateMessageId();
			client.sendMessage(messageId, phone, extNo, longContent);
			Thread.sleep(5000);*/
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		client.close();
	}

}
