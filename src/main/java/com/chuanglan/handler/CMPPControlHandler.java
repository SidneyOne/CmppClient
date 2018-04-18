package com.chuanglan.handler;

import java.net.SocketAddress;

import com.chuanglan.CMPPClient;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * CMPP协议总控制器
 * @author 
 */
public class CMPPControlHandler extends ChannelDuplexHandler {

	private CMPPClient gateway;
	public CMPPControlHandler( CMPPClient aGateWay ){
		this.gateway = aGateWay;
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future){
		try{
			super.connect(ctx, remoteAddress, localAddress, future);
		}catch( Exception ex ){
			ex.printStackTrace();
		}
	}
	
	public void close(ChannelHandlerContext ctx, ChannelPromise future)throws Exception {
		super.close(ctx, future);
	}
	
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		super.write(ctx, msg, promise);
	}
	
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		gateway.setContext(ctx);
		gateway.doLogin();
		super.channelActive(ctx);
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		gateway.setConnected(false);
		super.channelInactive(ctx);
	}
	
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)throws Exception {
		cause.printStackTrace();
	}
	
	//心跳
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		System.out.println(" 触发心跳测试。");
		gateway.doActiveTest();
	}

}
