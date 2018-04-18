package com.chuanglan.protocol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息处理工具
 * @author 
 */
public class MessageTool {
	
	public static int CONTENT_SPLIT_SIZE = 67;
	public static int CONTENT_MAX_SIZE = 70;
	
	/***
	 * 解码
	 * @param content
	 * @return
	 */
	public static String decodeUCS2String(byte[]content)
	{
		try {
			return new String(content,"UnicodeBigUnmarked");
		} catch (UnsupportedEncodingException e) {
			return  null;
		}
	}
	
	/***
	 * 格式化内容为UCS2格式
	 * @param content
	 * @return
	 */
	public static byte[] encodeUCS2String(String content)
	{
		try {
			return content.getBytes("UnicodeBigUnmarked");
		} catch (UnsupportedEncodingException e) {
			return  new byte[0];
		}
	}

	/***
	 * 长短信内容拆分
	 * @param content
	 * 单条短信最大长度默认70
	 * 长短信每条长度默认67
	 * @return
	 */
	public static List<String> splitLongContente(String messageContent) {
		return splitLongContente(messageContent,0);
	}

	/***
	 * 长短信内容拆分，带签名长度
	 * @param content
	 * @param signLenth
	 * 单条短信最大长度默认70
	 * 长短信每条长度默认67
	 * @return
	 */
	public static List<String> splitLongContente(String messageContent,int signLenth) {
		if(signLenth < 0){
			signLenth = 0;
		}
		List<String> strLst = new ArrayList<String>();
		StringBuffer strBuf = new StringBuffer(messageContent);
		if(strBuf.length()+signLenth > CONTENT_MAX_SIZE){
			int firstLenth = CONTENT_SPLIT_SIZE-signLenth;
			String first = strBuf.substring(0, firstLenth);
			strBuf.delete(0, firstLenth);
			strLst.add(first);
			while (strBuf.length() > CONTENT_SPLIT_SIZE){
				String str = strBuf.substring(0, CONTENT_SPLIT_SIZE);
				strBuf.delete(0, CONTENT_SPLIT_SIZE);
				strLst.add(str);
			}
		}
		if(strBuf.length() > 0){
			strLst.add(strBuf.toString());
		}
		
		return strLst;
	}

	/***
	 * 长短信内容拆分
	 * @param content
	 * @param singleMaxSize 单条短信最大长度
	 * @param longMsgLength 长短信每条长度
	 * @return
	 */
	public static List<String> splitLongContente(String messageContent,
									int singleMaxSize, int longMsgLength) {
		List<String> strLst = new ArrayList<String>();
		StringBuffer strBuf = new StringBuffer(messageContent);
		if(strBuf.length() > singleMaxSize){
			while (strBuf.length() > longMsgLength){
				String str = strBuf.substring(0, longMsgLength);
				strBuf.delete(0, longMsgLength);
				strLst.add(str);
			}
		}
		if(strBuf.length() > 0){
			strLst.add(strBuf.toString());
		}
		
		return strLst;
	}

}