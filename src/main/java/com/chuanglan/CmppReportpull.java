package com.chuanglan;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//状态重推
public class CmppReportpull {//implements Runnable
	
	public String url; 
	
	public static void main(String[] args) {
//		System.out.println(" content:CMPPDeliver [messageId=1250000462161	 srcTerminalId=18829965880	 doneTime=1801250000	 stat=".length());
//		System.out.println(" content:CMPPDeliver [messageId=1250000462161	 srcTerminalId=18829965880	 doneTime=1801250000	 stat=DELIVRD".length());
		getmsgid();
	}
	
	public static void getmsgid() {
		String fmine ="C://Users//XS-021//Desktop//" + "N7126266_cmpp_0126.txt";
		String fuser = "C://Users//XS-021//Desktop//"+ "N7126266_clientmsgid_0126.txt";
		String result = "C://Users//XS-021//Desktop//" + "result_msgid_0126.txt";
		
		try {
			InputStream inputStreamDiff = new FileInputStream(fmine);
			List<String> diffNumbers = new ArrayList<String>();
			BufferedReader diffReader = new BufferedReader(new InputStreamReader(inputStreamDiff));
			String diffNumber;
			while((diffNumber = diffReader.readLine()) != null){
				diffNumbers.add(diffNumber);
			}
			InputStream inputStreamTotal = new FileInputStream(fuser);
			BufferedReader totalReader = new BufferedReader(new InputStreamReader(inputStreamTotal));
			List<String> reaultList = new ArrayList<String>();
			String numberAndName;
			while((numberAndName = totalReader.readLine()) != null){
				String clientMsgId = numberAndName.substring(12, 29);
				String phone = numberAndName.substring(36, 47);
				String messageId = numberAndName.substring(59, 72);
				
				for (String diff : diffNumbers) {
					if(diff.substring(32, 45).equals(messageId)){
						String stat = diff.substring(79, 86);
						String doneTime = diff.substring(97, 107);
						String submitTime = diff.substring(120, diff.length());
						String str = clientMsgId + ","+phone + ","+ stat + ",doneTime:" + doneTime+",submitTime:" + submitTime;
						System.out.println("str:"+str);
						reaultList.add(str.toString());
					}
				}
			}
			//最后写入结果文件
			FileWriter fileWriter=new FileWriter(result, false);
			for(String str : reaultList){
				fileWriter.write(str);
				fileWriter.write("\r");
			}
			inputStreamDiff.close();
			inputStreamTotal.close();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
	}
	
	
	/**
	 * 构建状态报告消息体
	 */
	public static String buildReportDelivery(String messageId,String accessNumber,String phone,
							String result,String submitTime,String receiveTime) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("1").append(getToken());
		strBuf.append(accessNumber).append(getToken());
		strBuf.append(phone).append(getToken());
		strBuf.append("8").append(getToken());
		strBuf.append(messageId).append(getToken());
		strBuf.append(result).append(getToken());
		strBuf.append(submitTime).append(getToken());
		strBuf.append(receiveTime);
		return strBuf.toString();
	}
	
	//字段连接标记
	private static String TOKEN;
	public static final byte[] TOKEN_TYPES = {0x01,0x02,0x03};
	
	public static String getToken(){
		if(null == TOKEN){
			try {
				TOKEN = new String(TOKEN_TYPES,"UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		return TOKEN;
	}
	
//	GBK("GBK", 15),
//	UCS2("UnicodeBigUnmarked", 8),
//	BI("US-ASCII", 4),
//	ASCII("iso-8859-1",0),
//	SPECIAL("UnicodeBigUnmarked", 25);
	
	
}
