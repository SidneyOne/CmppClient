package com.chuanglan.protocol.cmpp20.message.body;

import java.util.HashMap;

import com.chuanglan.protocol.cmpp20.message.CMPPBody;

import io.netty.buffer.ByteBuf;

/**
 * 连接回复消息
 * @author 
 */
public class CMPPConnectionResp extends CMPPBody {

	public final static HashMap<Integer,String> STATUS = new HashMap<Integer, String>();

	//登录状态
	private int status;
	
	//网关验证值
	private String authenticatorISMG;
	
	//connection消息提交的验证值
	private String authenticator;
	
	//最大版本号
	private byte maxVersion;
	
	static{
		STATUS.put(0, "正确");
		STATUS.put(1, "消息结构错");
		STATUS.put(2, "非法源地址");
		STATUS.put(3, "认证错");
		STATUS.put(4, "版本太高");
		STATUS.put(5, "其他错误");
	}
	
	@Override
	public int encode(ByteBuf buffer) {
		int length = 0;
		buffer.writeByte(status);
		length += 1;
		
		buffer.writeZero(16);
		length+=16;
		
		buffer.writeByte(0x20);
		length+=1;
		
		return length;
	}

	/**
	 * 消息注册解码
	 */
	@Override
	public CMPPBody decode(ByteBuf buffer) {
		status=buffer.readByte();
		byte []auth = new byte[16];
		buffer.readBytes(auth);
		authenticatorISMG=new String(auth);
		maxVersion=buffer.readByte();
		
		return this;
	}



	/**
	 * @return 登录状态
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return 网关验证值
	 */
	public String getAuthenticatorISMG() {
		return authenticatorISMG;
	}

	/**
	 * @return 最大版本号
	 */
	public byte getMaxVersion() {
		return maxVersion;
	}

	public void setAuthenticator(String authenticator) {
		this.authenticator = authenticator;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getAuthenticator() {
		return authenticator;
	}
}
