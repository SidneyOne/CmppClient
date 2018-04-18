/**
 * 
 */
package com.chuanglan.protocol.cmpp20.message.body;

import com.chuanglan.protocol.cmpp20.message.CMPPBody;

import io.netty.buffer.ByteBuf;

/**submit_resp消息
 * @author 
 *
 */
public class CMPPSubmitResp extends CMPPBody {

	private long messageId;
	private byte result;

	
	@Override
	public int encode(ByteBuf buffer) {
		int length = 0;
		buffer.writeLong(this.messageId);
		length+=8;
		buffer.writeByte((byte)this.result);
		length+=1;
		return length;
	}

	
	@Override
	public CMPPBody decode(ByteBuf buffer) {
		this.messageId = buffer.readLong();
		
		this.result = buffer.readByte();
		return this;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}


	public void setResult(byte result) {
		this.result = result;
	}

	/**
	 * @return the messageId
	 */
	public long getMessageId() {
		return messageId;
	}

	/**
	 * @return the result
	 */
	public int getResult() {
		return result;
	}
	
	@Override
	public String toString() {
		return "CMPPSubmit [messageId=" + messageId + ", result=" + result + "]";
	}
	
}
