package com.chuanglan.handler;

import java.util.Date;
import java.util.HashMap;

import com.chuanglan.CMPPClient;
import com.chuanglan.protocol.cmpp20.message.CMPPHeader;
import com.chuanglan.protocol.cmpp20.message.CMPPMessage;
import com.chuanglan.protocol.cmpp20.message.body.CMPPActiveTestResp;
import com.chuanglan.protocol.cmpp20.message.body.CMPPConnectionResp;
import com.chuanglan.protocol.cmpp20.message.body.CMPPDeliver;
import com.chuanglan.protocol.cmpp20.message.body.CMPPDeliverResp;
import com.chuanglan.protocol.cmpp20.message.body.CMPPSubmitResp;
import com.chuanglan.utils.CommUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * CMPP接收消息控制器
 * @author 
 */
public class CMPPReceiverHandler extends SimpleChannelInboundHandler<CMPPMessage> {
	
	 private CMPPClient gateway  ;
	 
     public CMPPReceiverHandler( CMPPClient aGateWay ){
    	 this.gateway = aGateWay ;
     }

	 @Override
	 protected void channelRead0(ChannelHandlerContext ctx, CMPPMessage msg) throws Exception {
		 int commandId = msg.getCommandID();
		 switch (commandId) {
		 
		    //如果收到心跳包，不处理
			case CMPPMessage.CMPP_ACTIVE_TEST_RESP:
				System.out.println(" 收到服务器心跳包回复。");
				break;
				
		    //如果收到心跳包，回复网关
			case CMPPMessage.CMPP_ACTIVE_TEST:
				System.out.println(" 收到服务器心跳包，回复心跳。");
				CMPPMessage messageTest = new CMPPMessage();
				messageTest.setSequenceId(msg.getSequenceId());
				messageTest.setCommandID(CMPPMessage.CMPP_ACTIVE_TEST_RESP);
				CMPPActiveTestResp activeTestResp = new CMPPActiveTestResp();
				messageTest.setBody(activeTestResp);
				ctx.writeAndFlush(messageTest);
				break;
		    //连接回应
			case CMPPMessage.CMPP_CONNECT_RESP:
				handleCmppConnected(ctx, (CMPPConnectionResp)msg.getBody());
				break;
				
		    //获得上行短信
			case CMPPMessage.CMPP_DELIVER:
				CMPPDeliver deliver = (CMPPDeliver) msg.getBody();
				String accessNumber = deliver.getDestId();
				if( deliver.getIsDelivery() == 0 ){
					HashMap<String, String> message = new HashMap<String,String>();
					message.put("messageId", CommUtil.generateMessageId());
					message.put("remoteMessageId", Long.toHexString(deliver.getMessageId()));
					message.put("messageContent", deliver.getMessageContent());
					message.put("fmt", deliver.getMessageFormat().getStringCode());
					message.put("receiveTime", CommUtil.date2Str(new Date(), "yyMMddHHmm"));
					message.put("reportTime", System.currentTimeMillis()+"");
					message.put("sendPhone", deliver.getSendPhone());
					message.put("accessNumber", accessNumber);
					gateway.receiveMessage(message);
				}else{
					HashMap<String, String> message = new HashMap<String,String>();
					message.put("remoteMessageId", Long.toHexString(deliver.getReportMessageId()));
					message.put("result", deliver.getStat()+"");
					message.put("reportTime", System.currentTimeMillis()+"");
					message.put("receiveTime", deliver.getDoneTime()+"");
					message.put("submitTime", deliver.getSubmitTime()+"");
					message.put("sendPhone", deliver.getSendPhone());
					message.put("accessNumber", accessNumber);
					gateway.receiveReport( message );
				}
				responseDeliveryMessage(ctx, msg);
				break;
				
			//获得消息发送返回值
			case CMPPMessage.CMPP_SUBMIT_RESP:
				int seqId = msg.getSequenceId();
				CMPPSubmitResp resp = (CMPPSubmitResp)msg.getBody();
				String remoteMsgId = Long.toHexString(resp.getMessageId());
				handleSummitResponse( resp.getResult(), seqId, remoteMsgId);
				
	            break;
			default:
				break;
		}
	 }

	 
	 /**
	  * 发送回应处理
	  */
	private void handleSummitResponse( int result, int seqId, String remoteMsgId ){
		String messageId = gateway.onSubmitResponse(result+"", seqId+"", remoteMsgId);
		System.out.println("从网关收到消息:CMPP20SubmitRespMessage:[msgId="+messageId+",remoteMsgId="+remoteMsgId+",seqId="+seqId+",result="+result+"]");
	 }
	 
	 /**
	  *  处理登录回应
	  */
	 private void handleCmppConnected( ChannelHandlerContext ctx, CMPPConnectionResp response ){
		int status = response.getStatus();
		System.out.println("网关登录，回复状态："+status);
		if( status == 0 ){
			gateway.loginSuccess();
			return;
		}
		if( status > 5 ){
			 status = 5;
		}
		gateway.loginFail(response.getStatus(), CMPPConnectionResp.STATUS.get(status)); 
	 }
	 
	 /**
	  * 处理上行短信
	  */
     private void responseDeliveryMessage( ChannelHandlerContext context, CMPPMessage message ){
    	 
    	 CMPPMessage msg = new CMPPMessage();
    	 CMPPHeader header = new CMPPHeader();
    	 header.setCommandID(CMPPMessage.CMPP_DELIVER_RESP);
    	 header.setSequenceId(message.getSequenceId());
    	 msg.setHeader( header );
    	 CMPPDeliver deliver = (CMPPDeliver) message.getBody();
    	 CMPPDeliverResp resp = new CMPPDeliverResp();
    	 resp.setMessageId(deliver.getMessageId());
    	 resp.setResult((byte)0);
    	 msg.setBody(resp);
    	 
    	 context.writeAndFlush(msg) ;
    	 
     }
}
