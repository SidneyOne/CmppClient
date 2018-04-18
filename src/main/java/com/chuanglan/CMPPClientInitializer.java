package com.chuanglan;

import java.util.concurrent.TimeUnit;

import com.chuanglan.handler.CMPPControlHandler;
import com.chuanglan.handler.CMPPReceiverHandler;
import com.chuanglan.protocol.cmpp20.CMPPStreamDecoder;
import com.chuanglan.protocol.cmpp20.CMPPStreamEncoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 客户端pipeline初始化
 * 
 * @author
 *
 */
public class CMPPClientInitializer extends ChannelInitializer<SocketChannel> {

	// gateway
	private CMPPClient gateWay;

	// 构造
	public CMPPClientInitializer(CMPPClient aGateWay) {
		this.gateWay = aGateWay;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {

		ChannelPipeline pipeline = channel.pipeline();

		// 长度解码器
		pipeline.addLast("length field decoder", new LengthFieldBasedFrameDecoder(4 * 1024, 0, 4, -4, 0));

		// 解码器
		pipeline.addLast("decoder", new CMPPStreamDecoder());

		// 编码器
		pipeline.addLast("encoder", new CMPPStreamEncoder());
		
		// 空闲事件将发送心跳包
		pipeline.addLast("idleCheck", new IdleStateHandler(20,0,0, TimeUnit.SECONDS));

		// 接收控制器
		pipeline.addLast("receiver handler", new CMPPReceiverHandler(gateWay));

		// 总控制器
		pipeline.addLast("controler", new CMPPControlHandler(gateWay));
		
	}

}
