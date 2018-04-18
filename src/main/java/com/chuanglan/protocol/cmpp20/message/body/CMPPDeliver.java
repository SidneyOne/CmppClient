package com.chuanglan.protocol.cmpp20.message.body;

import org.apache.commons.lang.ArrayUtils;
import com.chuanglan.protocol.CharsetInfo;
import com.chuanglan.protocol.cmpp20.message.CMPPBody;
import com.chuanglan.protocol.cmpp20.message.CMPPMessage;
import com.chuanglan.utils.CommUtil;
import io.netty.buffer.ByteBuf;

/**
 * 上行短信消息体
 * @author 
 *
 */
public class CMPPDeliver extends CMPPBody{
	private long messageId;
	private String destId;
	private String serviceId = "HELP";   //业务类型，是数字、字母和符号的组合。
	private byte tpPid = (byte)0; //GSM协议类型。详细是解释请参考GSM03.40中的9.2.3.9
	private byte tpUdhi = (byte)0; //GSM协议类型。详细是解释请参考GSM03.40中的9.2.3.23,仅使用1位，右对齐
	private CharsetInfo messageFormat;
	private String srcTerminalId;
	private byte isDelivery = 0;	//是否为状态报告	0：非状态报告	1：状态报告

	private int messageLength;
	private String messageContent;
	private String reserved = new String(new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});    //保留位
	
	private String stat;
	private String submitTime;
	private String doneTime;
	private String destTerminalId;
	private int SMSCSequence=0;
	
	private long reportMessageId;
	private int deliveryCount = 1;

	static{
		registerBodyType(CMPPMessage.CMPP_DELIVER, CMPPDeliver.class);
	}
	
	@Override
	public int encode(ByteBuf buffer) {
		int length=0;
		//消息ID
		buffer.writeLong(this.messageId);
		length+=8;
		
		//21字节SP代码
		buffer.writeBytes(destId.getBytes());
		buffer.writeZero(21-destId.getBytes().length);
		length += 21;
		
		//业务类型
		buffer.writeBytes(serviceId.getBytes());
		buffer.writeZero(10-serviceId.getBytes().length);
		length += 10;

		//TP_pid
		buffer.writeByte(this.tpPid);
		length += 1;
		
		//TP_udhi
		buffer.writeByte(this.tpUdhi);
		length += 1;
		
		//格式
		buffer.writeByte(this.messageFormat.getCode());
		length += 1;
		
		//Src_terminal_Id
		buffer.writeBytes(srcTerminalId.getBytes());
		buffer.writeZero(21-srcTerminalId.getBytes().length);
		length += 21;
		
		//是否为状态报告
		buffer.writeByte(this.isDelivery);
		length += 1;

		//如果是状态报告，那么只有60个字符
		if( isDelivery == 1 ){
			//消息长度
			buffer.writeByte((byte)60);
			length += 1;
			//报告消息ID
			buffer.writeLong(reportMessageId);
			length += 8;
			//报告内容
			buffer.writeBytes(stat.getBytes());
			buffer.writeZero(7-stat.getBytes().length);
			length += 7;
			//提交时间
			buffer.writeBytes(submitTime.getBytes());
			buffer.writeZero(10-submitTime.getBytes().length);
			length += 10;
			//完成时间
			buffer.writeBytes(doneTime.getBytes());
			buffer.writeZero(10-doneTime.getBytes().length);
			length += 10;
			//原号码
			buffer.writeBytes(destTerminalId.getBytes());
			buffer.writeZero(21-destTerminalId.getBytes().length);
			length += 21;
			//序号
			buffer.writeInt(SMSCSequence);
			length+=4;
		}else{
			byte[] contentBytes = messageFormat.encode(messageContent);
			//消息长度
			messageLength = contentBytes.length;
			buffer.writeByte((byte)messageLength);
			length += 1;
			//消息内容
			buffer.writeBytes(contentBytes);
			length += messageLength;
		}
		
		//保留项
		buffer.writeBytes(reserved.getBytes());
		buffer.writeZero(8-reserved.getBytes().length);
		length += 8;

//		logger.debug("CMPPDeliver encode :"+ByteBufUtil.prettyHexDump(buffer));
		return length;
	}

	/**
     * 编码
     */
	@Override
	public CMPPBody decode(ByteBuf buffer) {
//		logger.debug("CMPPDeliver decode :"+ByteBufUtil.prettyHexDump(buffer));
		
		//消息ID
		this.messageId = buffer.readLong();
		
		//21字节SP代码
		byte []temp = new byte[21];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.destId=new String(temp).intern();
		
		//业务类型
		temp=new byte[10];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.serviceId=new String(temp).intern();
		
		//TP_pid
		this.tpPid=buffer.readByte();
		
		//TP_udhi
		this.tpUdhi=buffer.readByte();
		
		//格式
		this.messageFormat = CharsetInfo.fromCMPPFormat(buffer.readByte());
		
		//Src_terminal_Id
		temp=new byte[21];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.srcTerminalId=new String(temp);
		
		//是否为状态报告
		this.isDelivery=buffer.readByte();
		
		//消息长度
		this.messageLength=buffer.readByte() & 0xff;
 
		//如果是状态报告，那么只有60个字符
		if( isDelivery == 1 ){
			
			//报告消息ID
			setReportMessageId(buffer.readLong());
			
			//报告内容
			temp=new byte[7];
			buffer.readBytes(temp);
			temp=CommUtil.trimStringBufferRightZeros(temp);
			this.stat = new String(temp);
			
			//提交时间
			temp=new byte[10];
			buffer.readBytes(temp);
			temp=CommUtil.trimStringBufferRightZeros(temp);
			this.submitTime = new String(temp);
			
			//完成时间
			temp=new byte[10];
			buffer.readBytes(temp);
			temp=CommUtil.trimStringBufferRightZeros(temp);
			this.doneTime = new String(temp);
			
			//原号码
			temp=new byte[21];
			buffer.readBytes(temp);
			temp=CommUtil.trimStringBufferRightZeros(temp);
			this.destTerminalId = new String(temp);
			//序号
			this.SMSCSequence = buffer.readInt();
			
		}else{
			
			temp = new byte[messageLength];
			buffer.readBytes(temp);
			
			if(tpUdhi > 0 ){
				int headerlen = temp[0] + 1;
				if( headerlen == 6 ){
					deliveryCount = temp[4];
				}else{
					deliveryCount = temp[5];
				}
				byte[] temp2 = ArrayUtils.subarray(temp, 6, temp.length);
				this.messageContent = messageFormat.decode(temp2);
			}else{
				this.messageContent = messageFormat.decode(temp);
			}
		}
		
		//保留项
		temp = new byte[8];
		buffer.readBytes(temp);
		reserved = new String(temp);
		
		return this;
	}


    public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat;
	}

	public String getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}

	public String getDoneTime() {
		return doneTime;
	}

	public void setDoneTime(String doneTime) {
		this.doneTime = doneTime;
	}

	public String getDestTerminalId() {
		return destTerminalId;
	}

	public void setDestTerminalId(String destTerminalId) {
		this.destTerminalId = destTerminalId;
	}

	public int getSMSCSequence() {
		return SMSCSequence;
	}

	public void setSMSCSequence(int sMSCSequence) {
		SMSCSequence = sMSCSequence;
	}

	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}
	
	public long getReportMessageId() {
		return reportMessageId;
	}

	public void setReportMessageId(long reportMessageId) {
		this.reportMessageId = reportMessageId;
	}

	/**
	 * @return the messageId
	 */
	public long getMessageId() {
		return messageId;
	}


	/**
	 * @param messageId the messageId to set
	 */
	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}


	/**
	 * @return the destId
	 */
	public String getDestId() {
		return destId;
	}


	/**
	 * @param destId the destId to set
	 */
	public void setDestId(String destId) {
		this.destId = destId;
	}


	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}


	/**
	 * @param serviceId the serviceId to set
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}


	/**
	 * @return the tpPid
	 */
	public byte getTpPid() {
		return tpPid;
	}


	/**
	 * @param tpPid the tpPid to set
	 */
	public void setTpPid(byte tpPid) {
		this.tpPid = tpPid;
	}


	/**
	 * @return the tpUdhi
	 */
	public byte getTpUdhi() {
		return tpUdhi;
	}


	/**
	 * @param tpUdhi the tpUdhi to set
	 */
	public void setTpUdhi(byte tpUdhi) {
		this.tpUdhi = tpUdhi;
	}


	/**
	 * @return the messageFormat
	 */
	public CharsetInfo getMessageFormat() {
		return messageFormat;
	}


	/**
	 * @param messageFormat the messageFormat to set
	 */
	public void setMessageFormat(CharsetInfo messageFormat) {
		this.messageFormat = messageFormat;
	}


	/**
	 * @return the srcTerminalId
	 */
	public String getSrcTerminalId() {
		return srcTerminalId;
	}


	/**
	 * @param srcTerminalId the srcTerminalId to set
	 */
	public void setSrcTerminalId(String srcTerminalId) {
		this.srcTerminalId = srcTerminalId;
	}

	/**
	 * @return the isDelivery
	 */
	public byte getIsDelivery() {
		return isDelivery;
	}


	/**
	 * @param isDelivery the isDelivery to set
	 */
	public void setIsDelivery(byte isDelivery) {
		this.isDelivery = isDelivery;
	}


	/**
	 * @return the messageLength
	 */
	public int getMessageLength() {
		return messageLength;
	}


	/**
	 * @param messageLength the messageLength to set
	 */
	public void setMessageLength(byte messageLength) {
		this.messageLength = messageLength;
	}
	
	public int getDeliveryCount() {
		return deliveryCount;
	}

	public void setDeliveryCount(int deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	/**
	 * @param messageContent the messageContent to set
	 */
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	
	public String getMessageContent() {
		return messageContent;
	}

	public String getSendPhone() {
		if( getIsDelivery() == 0 ){
			if(srcTerminalId != null && srcTerminalId.startsWith("86")){
				srcTerminalId = srcTerminalId.substring(2);
			}
			return srcTerminalId;
		}else{
			if(destTerminalId != null && destTerminalId.startsWith("86")){
				destTerminalId = destTerminalId.substring(2);
			}
			return destTerminalId;
		}
	}

	@Override
	public String toString() {
		return "CMPPDeliver [messageId=" + messageId + ", destId=" + destId + ", tpUdhi=" + tpUdhi + ", messageFormat=" + messageFormat
				+ ", srcTerminalId=" + srcTerminalId + ", isDelivery=" + isDelivery + ", messageLength=" + messageLength
				+ ", messageContent=" + messageContent + ", stat=" + stat + ", submitTime="
				+ submitTime + ", doneTime=" + doneTime + ", destTerminalId=" + destTerminalId  + "]";
	}
}
