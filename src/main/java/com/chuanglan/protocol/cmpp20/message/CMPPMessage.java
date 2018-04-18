package com.chuanglan.protocol.cmpp20.message;

import com.chuanglan.protocol.Message;

import io.netty.buffer.ByteBuf;

/**
 * 
 * CMPP
 * @author 
 *
 */
public class CMPPMessage extends CMPPHeader implements Message,Cloneable{
	
	public static final int CMPP_CONNECT = 0x00000001; // 

	public static final int CMPP_CONNECT_RESP = 0x80000001; // 
	
	public static final int CMPP_TERMINATE = 0x00000002; // 
	
	public static final int CMPP_TERMINATE_RESP = 0x80000002; // 
	
	public static final int CMPP_SUBMIT = 0x00000004; // 
	
	public static final int CMPP_SUBMIT_RESP = 0x80000004; // 
	
	public static final int CMPP_DELIVER = 0x00000005; // 
	
	public static final int CMPP_DELIVER_RESP = 0x80000005; // 
	
	public static final int CMPP_QUERY = 0x00000006; // 
	
	public static final int CMPP_QUERY_RESP = 0x80000006; // 
	
	public static final int CMPP_CANCEL = 0x00000007; // 
	
	public static final int CMPP_CANCEL_RESP = 0x80000007; // 
	
	public static final int CMPP_ACTIVE_TEST = 0x00000008; // 
	
	public static final int CMPP_ACTIVE_TEST_RESP = 0x80000008; // 
	
	
	//
	protected CMPPBody body =CMPPBody.NOOP_BODY;
	private boolean decodeSuccess = true;
	
	public boolean isDecodeSuccess() {
		return decodeSuccess;
	}

	public void setDecodeSuccess(boolean decodeSuccess) {
		this.decodeSuccess = decodeSuccess;
	}

	/**
	 * @return 消息体
	 */
	public CMPPBody getBody() {
		return body;
	}

	/**
	 * @param 消息体
	 */
	public void setBody(CMPPBody body) {
		this.body = body;
	}
	
	/***
	 * 设置消息头
	 * @param header
	 */
	public void setHeader(CMPPHeader header)
	{
		this.totalLength=header.getTotalLength();
		this.commandID=header.getCommandID();
		this.sequenceId=header.getSequenceId();
	}

	/***
	 * 消息编码
	 */
	@Override
	public int encode(ByteBuf buffer) {
		
		int index = buffer.writerIndex();
		
		//消息头编码
		int size  = super.encode(buffer);
		
		//消息体编码
		size += getBody().encode(buffer);
		
		//消息总长度
		setTotalLength(size);
		
		buffer.setInt(index, size);
		
		return size;
		
	}

	/**
	 * 消息解码
	 */
	@Override
	public CMPPMessage decode(ByteBuf buffer){
		
		//消息头
		super.decode(buffer);
		
		//消息体
		CMPPBody body = CMPPBody.createBody(super.getCommandID());
		body.decode(buffer);
		this.body = body;
		
		return this;
	}

	/**
	 * 鍏嬮殕
	 */
	@Override
	public Object clone()  {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		    
		}
		
		CMPPMessage message=new CMPPMessage();
		message.setHeader(this);
		message.setBody(getBody());
		return message;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return getBody() + "COMMANID:"+Integer.toHexString(commandID)+"("+CMPPMessageTool.formatCommandId(commandID)+")" +" SEQ:"+sequenceId;
	}
}
