package com.chuanglan.protocol.cmpp20;

import java.util.List;

import com.chuanglan.protocol.cmpp20.message.CMPPBody;
import com.chuanglan.protocol.cmpp20.message.CMPPHeader;
import com.chuanglan.protocol.cmpp20.message.CMPPMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * CMPP消息解码
 * @author 
 */
public class CMPPStreamDecoder extends ByteToMessageDecoder{
	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buff, List<Object> out) throws Exception {
		if(buff.readableBytes() <= 0){
			return;
		}
//		System.out.println("cmpp decoder :"+ByteBufUtil.prettyHexDump(buff));
		try{
			CMPPMessage message = new CMPPMessage();
			
			CMPPHeader header = new CMPPHeader();
			header.decode(buff);
			message.setHeader(header);
			try{
				CMPPBody body = CMPPBody.decodeBody(message.getCommandID(), buff);
				message.setBody(body);
				message.setDecodeSuccess(true);
			}catch(Exception e){
				e.printStackTrace();
				message.setDecodeSuccess(false);
			}
			out.add(message);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(buff.readableBytes() > 0){
			System.out.println("cmpp decode have remain bytes:"+ByteBufUtil.prettyHexDump(buff));
			byte[] temp = new byte[buff.readableBytes()];
			buff.readBytes(temp);
		}
	}
}
