package com.chuanglan.protocol;

import java.io.UnsupportedEncodingException;

import com.chuanglan.utils.CommUtil;

/**
 * 消息编码
 * 
 * @author 
 */
public enum CharsetInfo {

	GBK("GBK", 15),

	UCS2("UnicodeBigUnmarked", 8),
	
	BI("US-ASCII", 4),
	
	ASCII("US-ASCII",0);
	
	private CharsetInfo(String charsetName, int code) {
		this.charsetName = charsetName;
		this.code = code;
	}

	private String charsetName;
	private int code;

	public byte[] encode(String content) {
		try {
			if(this == BI){
				return CommUtil.transHexString2bytes(content);
			}else{
				return content.getBytes(charsetName);
			}
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public String decode(byte[] content) {
		try {
			if(this == BI){
				return CommUtil.transBytes2HexString(content);
			}else{
				return new String(content, charsetName);
			}
		} catch (UnsupportedEncodingException e) {
			//如果内容解码失败，返回空字符串而不返回NULL
			return "";
		}
	}

	public String getCharsetName() {
		return charsetName;
	}

	public int getCode() {
		return code;
	}
	
	public String getStringCode(){
		return String.valueOf(code);
	}

	public static CharsetInfo fromCMPPFormat(int format) {
		switch (format) {
		case 4:
			return BI;
		case 8:
			return UCS2;
		case 15:
			return GBK;
		}
		return ASCII;
	}
}
