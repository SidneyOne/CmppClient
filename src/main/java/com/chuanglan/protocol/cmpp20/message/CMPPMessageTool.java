package com.chuanglan.protocol.cmpp20.message;

/**CMPP
 * @author
 * 
 */
public class CMPPMessageTool {

	/***
	 * 将命令转换为相应的描述
	 * @param commandId
	 * @return
	 */
	public static  String formatCommandId(int commandId)
	{
		switch(commandId)
		{
		case CMPPMessage.CMPP_ACTIVE_TEST:
			return "CMPP_ACTIVE_TEST";
		case CMPPMessage.CMPP_ACTIVE_TEST_RESP:
			return "CMPP_ACTIVE_TEST_RESP";
		case CMPPMessage.CMPP_CONNECT:
			return "CMPP_CONNECT";
		case CMPPMessage.CMPP_CONNECT_RESP:
			return "CMPP_CONNECT_RESP";
		case CMPPMessage.CMPP_DELIVER:
			return "CMPP_DELIVER";
		case CMPPMessage.CMPP_DELIVER_RESP:
			return "CMPP_DELIVER_RESP";
		case CMPPMessage.CMPP_SUBMIT:
			return "CMPP_SUBMIT";
		case CMPPMessage.CMPP_SUBMIT_RESP:
			return "CMPP_SUBMIT_RESP";
		case CMPPMessage.CMPP_TERMINATE:
			return "CMPP_TERMINATE";
		case CMPPMessage.CMPP_TERMINATE_RESP:
			return "CMPP_TERMINATE_RESP";
		}
		return "UNKNOWN";
	}
	
}