package com.chuanglan.protocol.cmpp20.message;

import com.chuanglan.protocol.Message;

import io.netty.buffer.ByteBuf;

/**
 * CMPP
 * @author 
 */
public class CMPPHeader implements Message {
	
	protected int totalLength;
	protected int commandID;
	protected int sequenceId;
	
	public final static int HEADER_LENGTH = 12;
	
	public int getTotalLength() {
		return totalLength;
	}
	public void setTotalLength(int totalLength) {
		this.totalLength = totalLength;
	}

	public int getCommandID() {
		return commandID;
	}
	public void setCommandID(int commandID) {
		this.commandID = commandID;
	}
	
	public int getSequenceId() {
		return sequenceId;
	}
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	
	/**
	 * 
	 */
	@Override
	public int encode(ByteBuf buffer) {
		int length = 0;
		buffer.writeInt(getTotalLength());
		length += 4;
		buffer.writeInt(getCommandID());
		length += 4;
		buffer.writeInt(getSequenceId());
		length += 4;
		return length;
	}

	
	/**
	 * 
	 */
	@Override
	public CMPPHeader decode(ByteBuf buffer) {
		setTotalLength(buffer.readInt());
		setCommandID(buffer.readInt());
		setSequenceId(buffer.readInt());
		return this;
	}
}
