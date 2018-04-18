package com.chuanglan.protocol.cmpp20.message.body;

import java.util.Arrays;

import com.chuanglan.protocol.cmpp20.message.CMPPBody;
import com.chuanglan.utils.CommUtil;
import io.netty.buffer.ByteBuf;

/**
 * 请求连接消息体
 * @author 
 */
public class CMPPConnection extends CMPPBody {

	//企业代码
	private String spId;
	
	//SP的密码
	private String password;

	//网关验证值
	private byte[] authenticator;
	
	//CMPP版本号 CMPP2.0
	private static byte version = 0x20; 
	
	//当前时间戳
	private int timestamp = 0;
	
	/**
	 * 构造函数
	 * @param spId
	 * @param password
	 * @param version
	 */
	public CMPPConnection(String spId, String password) {
		super();
		if( spId == null || spId.length() > 6 ){
			throw new IllegalArgumentException("企业代码不能超过6位");
		}
		if(password==null ){
			throw new IllegalArgumentException("密码不能为空");
		}
		this.spId = spId.intern();
		this.password = password.intern();
	}

	public CMPPConnection(){
		super();
	}
    
	/**
	 * 注册消息体编码
	 */
	@Override
	public int encode( ByteBuf buffer ){

        if( timestamp == 0  ) {
        	timestamp = CommUtil.getMMDDHHMMSS();
        }

		int length = 0;
		
		try {
			
			String sTimestamp = String.format("%010d", timestamp);
			
			//用于鉴别源地址
			byte[] zeros = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
			byte[] AuthenticatorSource = CommUtil.Md5(spId+new String(zeros)+password+sTimestamp);

			CommUtil.writeBytes2Buffer(buffer,spId.getBytes(),6);
			length += 6;
			
			buffer.writeBytes(AuthenticatorSource);
			length += AuthenticatorSource.length;
			
			buffer.writeByte(version);
			length += 1;
			
			buffer.writeInt(timestamp);
			length += 4;

		}catch (Exception ex) {
			ex.printStackTrace();
		}
		return length;
	}

	
	@Override
	public CMPPBody decode(ByteBuf buffer) {
		if( buffer.readableBytes() < 27 )
			throw new IllegalAccessError("可读字节过小,不足以支持解码");
		
		byte []temp = new byte[6];
		buffer.readBytes(temp);
		spId=new String(temp);
		
		authenticator = new byte[16];
		buffer.readBytes(authenticator);
		
		version=buffer.readByte();
		
		timestamp=buffer.readInt();
		
		return this;
	}

	@Override
	public String toString() {
		return "CMPPConnection [spId=" + spId + ", password=" + password
				+ ", authenticator=" + Arrays.toString(authenticator)
				+ ", timestamp=" + String.format("%010d", timestamp) + "]";
	}
	
	public String getSpId() {
		return spId;
	}

	public byte[] getAuthenticator() {
		return authenticator;
	}

	public int getTimestamp() {
		return timestamp;
	}

}
