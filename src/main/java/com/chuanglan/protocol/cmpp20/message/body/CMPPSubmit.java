package com.chuanglan.protocol.cmpp20.message.body;

import java.util.Arrays;

import com.chuanglan.protocol.CharsetInfo;
import com.chuanglan.protocol.LongMsgHeader;
import com.chuanglan.protocol.cmpp20.message.CMPPBody;
import com.chuanglan.utils.CommUtil;

import io.netty.buffer.ByteBuf;

/**短信发送消息
 * @author 
 *
 */
public class CMPPSubmit extends CMPPBody  implements Cloneable{
	//日志
//	private final static Logger logger = LogManager.getLogger(CMPPSubmit.class);

	private long messageId = 0L;     //信息标识，由SP接入的短信网关本身产生，本处填空。
	private byte pkTotal = (byte)1;       //相同Msg_Id的信息总条数，从1开始
	private byte pkNumber = (byte)1;      //相同Msg_Id的信息序号，从1开始
	private byte delivery = (byte)1;  //是否要求返回状态确认报告 1：需要
	private byte level = (byte)1;         //信息级别
	private String serviceId = "HELP";   //业务类型，是数字、字母和符号的组合。
	private byte feeUserType = 2; //计费用户类型字段
	private String feeTerminalId = ""; //被计费用户的号码（如本字节填空，则表示本字段无效，对谁计费参见Fee_UserType字段，本字段与Fee_UserType字段互斥）
	private byte feeTerminalType = (byte)0; //
	private byte tpPid = (byte)0; //GSM协议类型。详细是解释请参考GSM03.40中的9.2.3.9
	private byte tpUdhi = (byte)0; //GSM协议类型。详细是解释请参考GSM03.40中的9.2.3.23,仅使用1位，右对齐
	private CharsetInfo messageFormat = CharsetInfo.GBK; //信息格式
	private String spId; //信息内容来源(SP_Id)
	private String feeType = "01"; //资费类别 01：对“计费用户号码”免费
	private String feeCode = ""; //资费代码（以分为单位）
	private String validTime = ""; //存活有效期，格式遵循SMPP3.3协议
	private String atTime = ""; //定时发送时间，格式遵循SMPP3.3协议
	private String srcId; //源号码,SP的服务代码或前缀为服务代码的长号码，该号码最终在用户手机上显示为短消息的主叫号码
	private String[] destTerminalId; //接收短信的MSISDN号码
	private byte destTerminalType; //协议未定义
	private String messageContent; //信息内容
//	private byte[] contentAttach;
	private String reserve = new String(new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});    //保留位
	private byte[] tpUdhiBytes;
	private LongMsgHeader msgHeader = null;
	
	@Override
	public int encode(ByteBuf buffer) {
		
		int size=0;
		
		buffer.writeLong(messageId);
		size += 8;
		
		buffer.writeByte(pkTotal);
		size += 1;
		
		buffer.writeByte(pkNumber);
		size += 1;
		
		buffer.writeByte(delivery);
		size += 1;
		
		buffer.writeByte(level);
		size += 1;

		CommUtil.writeBytes2Buffer(buffer,serviceId.getBytes(),10);
		size += 10;
		
		buffer.writeByte(feeUserType);
		size += 1;

		CommUtil.writeBytes2Buffer(buffer,feeTerminalId.getBytes(),21);
		size += 21;
		
		buffer.writeByte(tpPid);
		size += 1;
		
		buffer.writeByte(tpUdhi);
		size += 1;
		
		buffer.writeByte(messageFormat.getCode());
		size += 1;

		CommUtil.writeBytes2Buffer(buffer,spId.getBytes(),6);
 		size += 6;

		CommUtil.writeBytes2Buffer(buffer,feeType.getBytes(),2);
		size += 2;

		CommUtil.writeBytes2Buffer(buffer,feeCode.getBytes(),6);
		size += 6;

		CommUtil.writeBytes2Buffer(buffer,validTime.getBytes(),17);
		size += 17;

		CommUtil.writeBytes2Buffer(buffer,atTime.getBytes(),17);
		size += 17;
		
		CommUtil.writeBytes2Buffer(buffer,srcId.getBytes(),21);
		size += 21;
		
		buffer.writeByte(destTerminalId.length);
		size += 1;
		
		for( String phoneNumber : destTerminalId ){
			CommUtil.writeBytes2Buffer(buffer,phoneNumber.getBytes(),21);
			size += 21;
		}

		byte[] byte_content = messageFormat.encode(messageContent);
		int contentLen = byte_content.length;
		if(msgHeader != null){
			contentLen += 6;
		}
		buffer.writeByte( contentLen );
	    size += 1;
	    
	    if(msgHeader != null){
			buffer.writeBytes(msgHeader.getBytes());
		}
	    
		buffer.writeBytes( byte_content );
		size += contentLen;

		CommUtil.writeBytes2Buffer(buffer,reserve.getBytes(),8);
		size += 8;

//		logger.debug("CMPPSubmit encode :"+ByteBufUtil.prettyHexDump(buffer));
		return size;
	}


	@Override
	public CMPPBody decode(ByteBuf buffer) {
//		logger.debug("CMPPSubmit decode :"+ByteBufUtil.prettyHexDump(buffer));
		
		//消息ID
		this.messageId = buffer.readLong();

		this.pkTotal=buffer.readByte();
		
		this.pkNumber=buffer.readByte();
		
		this.delivery=buffer.readByte();
		
		this.level=buffer.readByte();
		
		byte[] temp=new byte[10];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.serviceId=new String(temp);

		this.feeUserType=buffer.readByte();
		
		temp=new byte[21];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.feeTerminalId=new String(temp);

		this.tpPid=buffer.readByte();

		this.tpUdhi=buffer.readByte();
		
		this.messageFormat = CharsetInfo.fromCMPPFormat(buffer.readByte());

		temp=new byte[6];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.spId=new String(temp);

		temp=new byte[2];
		buffer.readBytes(temp);
		this.feeType=new String(temp);

		temp=new byte[6];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.feeCode=new String(temp);

		temp=new byte[17];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.validTime=new String(temp);

		temp=new byte[17];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.atTime=new String(temp);

		temp=new byte[21];
		buffer.readBytes(temp);
		temp=CommUtil.trimStringBufferRightZeros(temp);
		this.srcId=new String(temp);

		int length = buffer.readByte() & 0xff;

		this.destTerminalId = new String[length];
		for( int i=0;i<length;i++ ){
			temp=new byte[21];
			buffer.readBytes(temp);
			temp=CommUtil.trimStringBufferRightZeros(temp);
			this.destTerminalId[i] = new String(temp);
		}
		
		length = buffer.readByte() & 0xff;
		if(this.tpUdhi == 1){
			this.msgHeader = LongMsgHeader.decode(buffer);
			length -= 6;
		}
		temp=new byte[length];
		buffer.readBytes(temp);
		this.messageContent = messageFormat.decode(temp);

		temp=new byte[8];
		buffer.readBytes(temp);
		this.reserve=new String(temp);

		return this;
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
	 * @return the pkTotal
	 */
	public byte getPkTotal() {
		return pkTotal;
	}

	/**
	 * @param pkTotal the pkTotal to set
	 */
	public void setPkTotal(byte pkTotal) {
		if(pkTotal <=0)
			throw new IllegalArgumentException("pkTotal must than 0");
		this.pkTotal = pkTotal;
	}

	/**
	 * @return the pkNumber
	 */
	public byte getPkNumber() {
		
		return pkNumber;
	}

	/**
	 * @param pkNumber the pkNumber to set
	 */
	public void setPkNumber(byte pkNumber) {
		if(pkNumber <=0)
			throw new IllegalArgumentException("pkNumber must than 0");
		this.pkNumber = pkNumber;
	}

	/**
	 * @return the delivery
	 */
	public byte getDelivery() {
		return delivery;
	}

    public void setDelivery( byte register_delivery ){
    	delivery = register_delivery;
    }

	/**
	 * @return the level
	 */
	public byte getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(byte level) {
		this.level = level;
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
		this.serviceId = serviceId.intern();
	}

	/**
	 * @return the feeUserType
	 */
	public byte getFeeUserType() {
		return feeUserType;
	}

	/**
	 * @param feeUserType the feeUserType to set
	 */
	public void setFeeUserType(byte feeUserType) {
		this.feeUserType = feeUserType;
	}

	/**
	 * @return the feeTerminalId
	 */
	public String getFeeTerminalId() {
		return feeTerminalId;
	}

	/**
	 * @param feeTerminalId the feeTerminalId to set
	 */
	public void setFeeTerminalId(String feeTerminalId) {
		
		this.feeTerminalId = feeTerminalId;
	}

	/**
	 * @return the feeTerminalType
	 */
	public byte getFeeTerminalType() {
		return feeTerminalType;
	}

	/**
	 * @param feeTerminalType the feeTerminalType to set
	 */
	public void setFeeTerminalType(byte feeTerminalType) {
		this.feeTerminalType = feeTerminalType;
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
	 * @return the spId
	 */
	public String getSpId() {
		return spId;
	}

	/**
	 * @param spId the spId to set
	 */
	public void setSpId(String spId) {
		this.spId = spId.intern();
	}

	/**
	 * @return the feeType
	 */
	public String getFeeType() {
		return feeType;
	}

	/**
	 * @param feeType the feeType to set
	 */
	public void setFeeType(String feeType) {
		this.feeType = feeType;
	}

	/**
	 * @return the feeCode
	 */
	public String getFeeCode() {
		return feeCode;
	}

	/**
	 * @param feeCode the feeCode to set
	 */
	public void setFeeCode(String feeCode) {
		this.feeCode = feeCode;
	}

	/**
	 * @return the validTime
	 */
	public String getValidTime() {
		return validTime;
	}

	/**
	 * @param validTime the validTime to set
	 */
	public void setValidTime(String validTime) {
		this.validTime = validTime;
	}

	/**
	 * @return the atTime
	 */
	public String getAtTime() {
		return atTime;
	}

	/**
	 * @param atTime the atTime to set
	 */
	public void setAtTime(String atTime) {
		this.atTime = atTime;
	}

	/**
	 * @return the srcId
	 */
	public String getSrcId() {
		return srcId;
	}

	/**
	 * @param srcId the srcId to set
	 */
	public void setSrcId(String srcId) {
		this.srcId = srcId.intern();
	}

	/**
	 * @return the destTerminalId
	 */
	public String[] getDestTerminalId() {
		return destTerminalId;
	}

	/**
	 * @param destTerminalId the destTerminalId to set
	 */
	public void setDestTerminalId(String[] destTerminalId) {
		this.destTerminalId = destTerminalId;
	}

	/**
	 * @return the destTerminalType
	 */
	public byte getDestTerminalType() {
		return destTerminalType;
	}

	/**
	 * @param destTerminalType the destTerminalType to set
	 */
	public void setDestTerminalType(byte destTerminalType) {
		this.destTerminalType = destTerminalType;
	}

	/**
	 * @return the messageContent
	 */
	public String getMessageContent() {
		return messageContent;
	}

	/**
	 * @param messageContent the messageContent to set
	 */
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	
	public CharsetInfo getMessageFormat(){
		return messageFormat;
	}
	
	public byte[] getTpUdhiBytes() {
		return tpUdhiBytes;
	}


	public void setTpUdhiBytes(byte[] tpUdhiBytes) {
		this.tpUdhiBytes = tpUdhiBytes;
	}


	public void setMessageFormat(CharsetInfo messageFormat) {
		this.messageFormat = messageFormat;
	}
	
	public void setReserve(String reserve) {
		this.reserve = reserve;
	}
	
    public String getReserve() {
		return reserve;
		
	}
    
	public LongMsgHeader getMsgHeader() {
		return msgHeader;
	}

	public void setMsgHeader(LongMsgHeader msgHeader) {
		this.msgHeader = msgHeader;
	}


	@Override
	public Object clone()  {
		
		try {
			return   super.clone();
		} catch (CloneNotSupportedException e) {
			
		}
		return null;
	}


	@Override
	public String toString() {
		String header = "";
		if(msgHeader != null){
			header = "msgHeader["+msgHeader.toString()+"]";
		}
		return "CMPPSubmit [messageId=" + messageId + ", pkTotal=" + pkTotal + ", pkNumber=" + pkNumber
				+ ", tpPid=" + tpPid + ", tpUdhi=" + tpUdhi + ", messageFormat=" + messageFormat + ", spId=" + spId
				+ ", srcId=" + srcId + ", destTerminalId=" + Arrays.toString(destTerminalId)
				+ ", messageContent=" + messageContent + "]"+header;
	}

}
