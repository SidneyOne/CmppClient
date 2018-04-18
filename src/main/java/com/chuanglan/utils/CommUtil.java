package com.chuanglan.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;

public class CommUtil {
	
	public static final char[] BYTE_CHAR = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	
	//获取十进制的日期时间 mmddhhmiss
	public static int getMMDDHHMMSS(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int timestamp = (calendar.get(Calendar.MONTH) + 1) * 0x5f5e100
				+ calendar.get(Calendar.DAY_OF_MONTH) * 0xf4240
				+ calendar.get(Calendar.HOUR_OF_DAY) * 10000
				+ calendar.get(Calendar.MINUTE) * 100
				+ calendar.get(Calendar.SECOND);
		return timestamp;
	}

	//获取十进制的日期时间 YYmmddhhmi
	public static int getYYMMDDHHMM(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		int timestamp = (calendar.get(Calendar.YEAR)%100) * 0x5f5e100
				+ (calendar.get(Calendar.MONTH) + 1) * 0xf4240
				+ calendar.get(Calendar.DAY_OF_MONTH) * 10000
				+ calendar.get(Calendar.HOUR_OF_DAY) * 100
				+ calendar.get(Calendar.MINUTE);
		return timestamp;
	}

	//获取十进制的日期时间 YYmmdd
	public static int getYYMMDD(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		int timestamp = (calendar.get(Calendar.YEAR)%100) * 10000
				+ (calendar.get(Calendar.MONTH) + 1) * 100
				+ calendar.get(Calendar.DAY_OF_MONTH);
		return timestamp;
	}
	
	/**
	 * 按照参数format的格式，日期转字符串
	 * @param date
	 * @param format
	 * @return
	 */
	public static String date2Str(Date date,String format){
		if(date!=null){
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(date);
		}else{
			return "";
		}
	}

    //MD5加密
	public static byte[] Md5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(str.getBytes());
		return md5.digest();
	}

	/***
	 * 去除buffer末尾的0x0
	 */
	public static byte[] trimStringBufferRightZeros(byte[] buffer){
		int offset = buffer.length-1;
		while(offset > 0){
			if(buffer[offset] != 0x0){
				break;
			}
			offset--;
		}
		return Arrays.copyOf(buffer, offset+1);
	}

	/***
	 * BCD码转成long
	 */
	public static String transBCD2String(byte[] bcd) {
		StringBuffer retString = new StringBuffer();
		int lengh = bcd.length;
		for(int i=0;i<lengh;i++){
			int high = (bcd[i] & 0xf0) >>> 4;
			int low = (bcd[i] & 0x0f);
			retString.append(String.valueOf(high));
			retString.append(String.valueOf(low));
		}
		return retString.toString();
	}

	/**
	 * 验证客户端IP
	 * @param ip
	 * @return
	 */
	public static boolean validClientIp( String ip, String clientIp ){
		 String[] ips  = ip.split(",");
		 for(String iip : ips){
			 if( iip.equals(clientIp) ){
				 return true;
			 }
		 }
		 return false;
	}

	/**
	 * 获取buffer中以0x0结尾的字符串内容，最大截取max长度,max必须大于0
	 * 注：主要用于smpp协议对字符串的解析
	 */
	public static byte[] readByteBufString(ByteBuf buffer, int max) {
		if(max <= 0 || buffer.readableBytes() <=0){
			return new byte[0];
		}
		byte[] retByte = new byte[max];
		int offset = 0;
		for(;offset < max;offset++){
			byte temp = buffer.readByte();
			if(temp == 0x0){
				break; 
			}
			retByte[offset] = temp;
		}
		return Arrays.copyOf(retByte, offset);
	}

	/**
	 * HEX字符串转换成byte数组
	 */
	public static byte[] transHexString2bytes(String content) throws NumberFormatException {
		int length = content.length();
		if(length == 0){
			return new byte[0];
		}
		byte[] bytes = new byte[length/2];
		for(int i=0;i<bytes.length;i++){
			String temp = content.substring(i*2, i*2+2);
			int hex = 0;
			try{
				hex = Integer.parseInt(temp, 16);
			}catch(NumberFormatException e){
				throw e;
			}
			bytes[i]=(byte)hex;
		}
		return bytes;
	}

	/**
	 * byte数组转换成HEX字符串
	 */
	public static String transBytes2HexString(byte[] bytes) throws NumberFormatException {
		if(bytes.length == 0){
			return null;
		}
		StringBuffer strBuf = new StringBuffer();
		int bit;
		for(int i=0;i<bytes.length;i++){
			bit = (bytes[i]&0xf0)>>4;
			strBuf.append(BYTE_CHAR[bit]);
			bit = bytes[i]&0x0f;
			strBuf.append(BYTE_CHAR[bit]);
		}
		return strBuf.toString();
	}

	/**
	 * buffer中写入定长byte数组，数组长度大于length则截断，小于length则补0x00
	 * @param buffer
	 * @param bytes
	 * @param length
	 */
	public static void writeBytes2Buffer(ByteBuf buffer, byte[] bytes, int length) {
		if(null == buffer || null == bytes ||  length <= 0){
			return;
		}
		int byteLength = bytes.length;
		if(byteLength >= length){
			buffer.writeBytes(bytes, 0, length);
		}else{
			buffer.writeBytes(bytes);
			buffer.writeZero(length-byteLength);
		}
	}

	//时间格式
	public final static SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmm");
	public final static AtomicInteger seed = new AtomicInteger();

	public static String generateMessageId() {
		int messageIdSeed = seed.addAndGet(1);
		if(messageIdSeed < 0){
			messageIdSeed += Integer.MAX_VALUE;
		}
		if( messageIdSeed > 900000 ){
			messageIdSeed = messageIdSeed%900000;
 		}
		messageIdSeed += 100000;
		return df.format(new Date()) + messageIdSeed;
	}
	
}
