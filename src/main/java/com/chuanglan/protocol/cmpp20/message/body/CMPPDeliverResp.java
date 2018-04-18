
package com.chuanglan.protocol.cmpp20.message.body;

import com.chuanglan.protocol.cmpp20.message.CMPPBody;

import io.netty.buffer.ByteBuf;

/**
 * 
 * 回收
 * @author 
 *
 */
public class CMPPDeliverResp extends CMPPBody {

	private long messageId;
	private byte result;
	
	@Override
	public int encode(ByteBuf buffer) {
		buffer.writeLong(messageId);
		buffer.writeByte(result);
		return 9;
	}
	
	@Override
	public CMPPBody decode(ByteBuf buffer) {
		this.messageId = buffer.readLong();
		this.result = buffer.readByte();
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
	 * @return the result
	 */
	public byte getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(byte result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "CMPPSubmit [messageId=" + messageId + ", result=" + result + "]";
	}
	
}
