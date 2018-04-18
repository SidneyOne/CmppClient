package com.chuanglan.protocol.cmpp20.message;

import java.util.HashMap;
import java.util.Map;

import com.chuanglan.protocol.Message;
import com.chuanglan.protocol.cmpp20.message.body.CMPPActiveTest;
import com.chuanglan.protocol.cmpp20.message.body.CMPPActiveTestResp;
import com.chuanglan.protocol.cmpp20.message.body.CMPPConnection;
import com.chuanglan.protocol.cmpp20.message.body.CMPPConnectionResp;
import com.chuanglan.protocol.cmpp20.message.body.CMPPDeliver;
import com.chuanglan.protocol.cmpp20.message.body.CMPPDeliverResp;
import com.chuanglan.protocol.cmpp20.message.body.CMPPSubmit;
import com.chuanglan.protocol.cmpp20.message.body.CMPPSubmitResp;
import com.chuanglan.protocol.cmpp20.message.body.CMPPTerminate;
import com.chuanglan.protocol.cmpp20.message.body.CMPPTerminateResp;
import io.netty.buffer.ByteBuf;


/**
 * CMPP2.0协议消息体基类
 * @author 
 * 
 */
public abstract class CMPPBody implements Message {
	
	//消息ID与消息类型对应关系
	private static Map<Integer, Class<?>> BODY_TYPE = new HashMap<Integer, Class<?>>();
	
	//消息注册
	static{
		registerBodyType(CMPPMessage.CMPP_ACTIVE_TEST, CMPPActiveTest.class);
		registerBodyType(CMPPMessage.CMPP_ACTIVE_TEST_RESP, CMPPActiveTestResp.class);
		registerBodyType(CMPPMessage.CMPP_CONNECT, CMPPConnection.class);
		registerBodyType(CMPPMessage.CMPP_CONNECT_RESP, CMPPConnectionResp.class);
		registerBodyType(CMPPMessage.CMPP_DELIVER, CMPPDeliver.class);
		registerBodyType(CMPPMessage.CMPP_DELIVER_RESP, CMPPDeliverResp.class);
		registerBodyType(CMPPMessage.CMPP_SUBMIT, CMPPSubmit.class);
		registerBodyType(CMPPMessage.CMPP_SUBMIT_RESP, CMPPSubmitResp.class);
		registerBodyType(CMPPMessage.CMPP_TERMINATE, CMPPTerminate.class);
		registerBodyType(CMPPMessage.CMPP_TERMINATE_RESP, CMPPTerminateResp.class);
	}
	protected final static void registerBodyType(int commandId,Class<?> bodyType){
		BODY_TYPE.put(commandId, bodyType);
	}
	
	
	/***
	 * 空消息体
	 */
	public static final CMPPBody NOOP_BODY = new NOOPBody();
		
	
	/***
	 * 消息编码到buffer
	 * @param buffer
	 * @return buffer长度
	 */
	public abstract int encode(ByteBuf buffer);
	
	
	/***
	 *buffer解码为消息体
	 * @param buffer
	 * @return 消息体
	 */
	public abstract CMPPBody decode(ByteBuf buffer);
	
	
	/**
	 * 消息体解码
	 */
	public static CMPPBody decodeBody( int commandId, ByteBuf buffer ){
		CMPPBody body = createBody(commandId);
		body.decode(buffer);
		return body;
	}
	
	/**
	 * 创建消息对象
	 * @param commandId
	 * @return CMPPBody
	 */
	public static CMPPBody createBody( int commandId ){
		CMPPBody body=null;
		try {
			body = CMPPBody.newInstance(commandId);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return body;
	}
	
	
	/**
	 * 获取命令ID对应消息类型
	 * @param commandId
	 * @return
	 */
	public final static Class<?> getBodyType(int commandId)
	{
		return BODY_TYPE.get(commandId);
	}
	
	/***
	 * 根据命令ID生成对应的消息体
	 * @param commandId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static CMPPBody newInstance(int commandId ) throws InstantiationException, IllegalAccessException
	{
		Class<?> type = getBodyType(commandId);
		if(type == null){
		   return new NOOPBody();
		}
		return (CMPPBody) type.newInstance();
	}
	
	
	/**
	 * 空消息体
	 * @author zhu_tek
	 */
	public static class NOOPBody extends CMPPBody{
		@Override
		public int encode(ByteBuf buffer) {
			return 0;
		}
		
		@Override
		public CMPPBody decode(ByteBuf buffer) {
			return this;
		}
	}
}
