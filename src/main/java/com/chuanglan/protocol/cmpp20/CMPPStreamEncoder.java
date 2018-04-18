package com.chuanglan.protocol.cmpp20;

import java.net.InetSocketAddress;

import com.chuanglan.protocol.cmpp20.message.CMPPMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * CMPP协议 数据流编码
 * @author 
 */
public class CMPPStreamEncoder extends MessageToByteEncoder<CMPPMessage>{
	
	@Override
	protected void encode(ChannelHandlerContext context, CMPPMessage message, ByteBuf buff) throws Exception {
		try{
			//消息长度
			int size=0;
			size=message.encode(buff);
			
			//校正消息体长度
			message.setTotalLength(size);
			buff.setInt(0, size);
//			System.out.println("CMPP消息长度："+ size + " class == " + message.getCommandID());
//			System.out.println(ByteBufUtil.prettyHexDump(buff));
		} catch(Exception ex){
			System.out.println("编码消息:" + message.toString());
			ex.printStackTrace();
			buff.clear();
//			System.out.println("ENCODE: " + ByteBufUtil.prettyHexDump(buff));
		}
		
	}
}
