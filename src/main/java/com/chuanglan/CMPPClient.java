package com.chuanglan;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.chuanglan.protocol.CharsetInfo;
import com.chuanglan.protocol.LongMsgHeader;
import com.chuanglan.protocol.MessageTool;
import com.chuanglan.protocol.cmpp20.message.CMPPHeader;
import com.chuanglan.protocol.cmpp20.message.CMPPMessage;
import com.chuanglan.protocol.cmpp20.message.body.CMPPActiveTest;
import com.chuanglan.protocol.cmpp20.message.body.CMPPConnection;
import com.chuanglan.protocol.cmpp20.message.body.CMPPSubmit;
import com.chuanglan.utils.DefaultSequenceNumberUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class CMPPClient {

	//启动
	public Bootstrap bootstrap;

	//事件组
	private EventLoopGroup group;

	private ChannelFuture channelFuture;

	//是否关闭
	private boolean closed = false;
	
	//是否连接
	private boolean connected = false;

	//连接状态锁
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition needConnect = this.lock.newCondition();
	private final Condition loginResp = this.lock.newCondition();
	
	//网关配置
	private CMPPConfig config;
	//是否登录
	private boolean isLogin;
	//上下文
	private ChannelHandlerContext ctx;
	//sequnce
	private AtomicInteger seqNo;
	//消息发送序号跟消息ID关系
	protected final Map<String, String> S2M;
	//remoutId跟消息ID关系
	protected final Map<String, String> R2M;
	
	
	public CMPPClient(CMPPConfig config){
		this.config = config;
		this.seqNo = new AtomicInteger(0);
		this.S2M = new HashMap<String, String>();
		this.R2M = new HashMap<String, String>();
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		bootstrap.option(ChannelOption.SO_RCVBUF, 2048);
		bootstrap.option(ChannelOption.SO_SNDBUF, 2048);
		bootstrap.handler(new CMPPClientInitializer(this));
	}
	
    public void doConnect(){
    	if(closed){
    		return;
    	}
		System.out.println(" doConnect:"+config.getServer()+":"+config.getPort());
		connected = false;
		isLogin = false;
		if(null != channelFuture){
			channelFuture.channel().close();
			channelFuture = null;
		}
		try{
			bootstrap.remoteAddress( InetAddress.getByName(config.getServer()), config.getPort());
			channelFuture = bootstrap.connect().sync();
			channelFuture.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture f) throws Exception {
					setConnected(f.isSuccess());
					awaitConnected();
				}
			});
			waitConnect();
		}catch( Exception ex ){
			ex.printStackTrace();
		}
	}

	public void close() {
		System.out.println("client close!");
		closed = true;
		connected=false;
		channelFuture.channel().close();
		group.shutdownGracefully();
		bootstrap = null;
	}

	private void waitConnect() {
		lock.lock();
		try{
			needConnect.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}

	private void awaitConnected() {
		lock.lock();
		try{
			needConnect.signalAll();
		}finally{
			lock.unlock();
		}
	}

	public void waitLoginResp() {
		lock.lock();
		try{
			loginResp.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}

	public void awaitLoginFinish() {
		lock.lock();
		try{
			loginResp.signalAll();
		}finally{
			lock.unlock();
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	//发送心跳包
	public void doActiveTest() {
		System.out.println("发送心跳包");
		CMPPMessage message = new CMPPMessage();
		CMPPActiveTest activeTest =new CMPPActiveTest();
		message.setCommandID(CMPPMessage.CMPP_ACTIVE_TEST);
		message.setSequenceId(getSeqNo());
		message.setBody(activeTest);
		//发送
		ctx.writeAndFlush(message);
	}

	//登录操作
	public void doLogin() {
		System.out.println(" 正在登录。。。");
		CMPPMessage loginMessage = new CMPPMessage();
		CMPPConnection cmppConnection=new CMPPConnection(config.getSpId(), config.getPassword());
		loginMessage.setCommandID(CMPPMessage.CMPP_CONNECT);
		loginMessage.setSequenceId(getSeqNo());
		loginMessage.setBody(cmppConnection);
		//发送
		ctx.writeAndFlush(loginMessage);
	}

	/**
	 * 发送短信
	 * 
	 */
	public int sendMessage(String messageId,String phone,String extNo,String content) {
		System.out.println("发送短信, messageId:["+messageId+"] phone:["+phone+"] extNo:["+extNo+"] content:"+content);

		if(!isConnected() || !isLogin()){
			System.out.println("网关状态不正常，无法发送短信。  connect:"+isConnected()+", login:"+isLogin());
			return 0;
		}
		
		//接入号，等于网关配置的接入号+自定义扩展码，最大20位
		String accessNumber = config.getSpNumber() + extNo;
		if(accessNumber.length() > 20){
			accessNumber = accessNumber.substring(0, 20);
		}
		
		//创建submit消息
		CMPPSubmit submit = new CMPPSubmit();
		submit.setSpId(config.getSpId());
		submit.setServiceId(config.getServiceId());
		submit.setSrcId(accessNumber);
		submit.setDestTerminalId(new String[]{phone});
		submit.setMessageContent(content);
		submit.setMessageFormat(CharsetInfo.UCS2);
		
		CMPPMessage message = new CMPPMessage();
		message.setCommandID(CMPPMessage.CMPP_SUBMIT);
		
		//长短信内容分割
		List<String> contentChips = MessageTool.splitLongContente(content);
		int sendNum = contentChips.size() > 1 ? contentChips.size() : 1;
		//长短信设置
		if(sendNum > 1){
			byte total = (byte)sendNum;
			byte serial = DefaultSequenceNumberUtil.getOne();
			
			for(int index=0;index < contentChips.size();index++){
				CMPPMessage message2 = (CMPPMessage) message.clone();
				CMPPSubmit submit2 = (CMPPSubmit) submit.clone();
				LongMsgHeader msgHeader = new LongMsgHeader();
				//设置sequenceId对应的发送信息
				int sequenceId = getSeqNo();
				message2.setSequenceId(sequenceId);
				this.S2M.put(String.valueOf(sequenceId), messageId);
				msgHeader.setSerial(serial);
				msgHeader.setTotal(total);
				msgHeader.setNumber((byte) (index+1));
				String contentChip = contentChips.get(index);
				submit2.setMessageContent(contentChip);
				submit2.setMsgHeader(msgHeader);
				submit2.setTpUdhi((byte)1);
				submit2.setPkTotal(total);
				submit2.setPkNumber((byte) (index+1));
				message2.setBody(submit2);
				//发送
				ctx.writeAndFlush(message2);
			}
		}else{
			//设置sequenceId对应的发送信息
			int sequenceId = getSeqNo();
			message.setSequenceId(sequenceId);
			this.S2M.put(String.valueOf(sequenceId), messageId);
			message.setBody(submit);
			//发送
			ctx.writeAndFlush(message);
		}
		return sendNum;
	}

	//接收到sumbit_resp
	public String onSubmitResponse(String result, String seqno, String remoteMessageId) {
		String messageId = S2M.get(seqno);
		if(null == messageId){
			System.out.println("S2M not find messagId! seqno:"+seqno);
			return null;
		}
		S2M.remove(seqno);
		if("0".equals(result)){
			R2M.put(remoteMessageId, messageId);
			System.out.println("messegId:"+messageId+" submit success, remoteId:"+remoteMessageId);
		}else{
			System.out.println("messegId:"+messageId+" submit fail, result:"+result);
		}
		return messageId;
	}

	//接收到状态报告
	public void receiveReport(HashMap<String, String> deliver) {
		//短信发送结果
		String result = deliver.get("result");
		//电话号码
		String phone = deliver.get("sendPhone");
		//remoteId
		String remoteId = deliver.get("remoteMessageId");
		String messageId = R2M.get(remoteId);
		if(null == messageId){
			System.out.println("R2M not find messagId! remoteId:"+remoteId);
			return;
		}
		S2M.remove(remoteId);
		System.out.println("messegId:"+messageId+" receive report, phone:"+phone+" result:"+result);
	}

	//接收到上行短信
	public void receiveMessage(HashMap<String, String> deliver) {
		String messageId = deliver.get("messageId");
		//电话号码
		String phone = deliver.get("sendPhone");
		//接入号
		String accessNumber = deliver.get("accessNumber");
		//上行内容
		String content = deliver.get("messageContent");
		System.out.println("收到上行短信，messegId:"+messageId+", phone:"+phone+", accessNumber:"+accessNumber+", content:"+content);
	}

	private int getSeqNo() {
		return seqNo.getAndIncrement();
	}

	public boolean isLogin() {
		return isLogin;
	}

	public void loginSuccess() {
		isLogin = true;
		awaitLoginFinish();
	}

	public void loginFail(int status, String message) {
		isLogin = false;
		awaitLoginFinish();
	}

	public void setContext(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

}
