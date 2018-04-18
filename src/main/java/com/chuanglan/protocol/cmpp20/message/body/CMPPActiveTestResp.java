
package com.chuanglan.protocol.cmpp20.message.body;

import com.chuanglan.protocol.cmpp20.message.CMPPBody;

import io.netty.buffer.ByteBuf;

public class CMPPActiveTestResp extends CMPPBody {

	private byte reserved;
	@Override
	public int encode(ByteBuf buffer) {
			buffer.writeByte(reserved);
			return 1;
	}

	@Override
	public CMPPBody decode(ByteBuf buffer) {
		this.reserved=buffer.readByte();
		return this;
	}

}
