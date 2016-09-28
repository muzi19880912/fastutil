package org.fastutil.mainland.util;

import com.efun.general.GeneralHelper;
import org.apache.commons.lang3.StringUtils;

/**
 * 易幻登陆 字符串工具类
 * @author HUYUNCHONG
 * @date 2013-01-04
 */
public class StringUtil{
	
	/**
	 * 去除字符串中的换行、回车、制表符
	 * @param inputValue 输入字符串
	 * @return String
	 */
	public static final String removeEnter(String inputValue){
		if(StringUtils.isEmpty(inputValue)){
			return "";
		}
		return inputValue.replaceAll("\t|\r|\n", "");
	}
	
	/**
	 * 获取字符串的字符数(英文 1个字符，中文 2个字符)
	 * @param inputValue 
	 * @return 字符长度
	 */
	public static final int  lengthOfChar(String inputValue){
		if(StringUtils.isEmpty(inputValue))
			return 0;
		if(inputValue.matches("[a-zA-Z0-9\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\+\\`\\;\\'\\,\\.\\/\\|\"\\:\\<\\>\\?\\-\\=\\_\\+\\\\]*"))
			return inputValue.length();
		return inputValue.replaceAll("[\u4E00-\u9FFF\\（\\）\\“\\”\\：\\—]", "**").length();
	}
	
	/**
	 * 验证字符长度是否合法
	 * @param inputValue 输入字符
	 * @param len 合法长度
	 * @return boolean
	 */
	public static final boolean checkParamLen(String inputValue,int len) {
        return lengthOfChar(inputValue) <= len;
    }
	
	/**
	 * 比较版本号是否是最新
	 * @param oldVersion 原始本版号
	 * @param newVersion 当前数据库最新版本号
	 * @return 是否需要更新（boolean）
	 */
	public static final boolean versionCompareTo(String oldVersion,String newVersion) throws Exception{
		//将字符串分含.字符串转换为数值型  1.1.5 -> 001001005
		StringBuffer oldValue=new StringBuffer();
		String[] olds=oldVersion.split("\\.");
		for(String str:olds){
			oldValue.append(StringUtils.leftPad(str, 3, "0"));
		}
		StringBuffer newValue= new StringBuffer();
		String[] anothers=newVersion.split("\\.");
		for(String str:anothers){
			newValue.append(StringUtils.leftPad(str, 3, "0"));
		}
		//将版本转换为左对齐的Long类型
		int oldLen = oldValue.length();
		int newLen = newValue.length();
		if(oldLen<newLen){
			oldValue=new StringBuffer(StringUtils.rightPad(String.valueOf(oldValue), newLen, "0"));
		}
		if(oldLen>newLen){
			newValue=new StringBuffer(StringUtils.rightPad(String.valueOf(newValue), oldLen, "0"));
		}
		//比较Long类型的版本号
        return Long.valueOf(String.valueOf(newValue)) > Long.valueOf(String.valueOf(oldValue));
    }
	
	/**
	 * 邮箱验证
	 * @param email
	 * @return boolean
	 */
	public static final boolean email(String email){
		if(StringUtils.isEmpty(email))return false;
		return GeneralHelper.isStrEmailAddress(email);
	}
	
	/**
	 * 帐号验证
	 * @param loginName
	 * @return boolean
	 */
	public static final boolean loginName1(String loginName){
		if(StringUtils.isEmpty(loginName))return false;
		return loginName.matches("[\\w_]{1,50}");
	}
	
	/**
	 * 将空字符串(null)转换为 ""
	 * @param inputValue 输入参数
	 * @return 输出 String
	 */
	public static final String valueOf(String inputValue){
		if(inputValue==null)
			return "";
		return String.valueOf(inputValue);
	}
	/**
	 * 将空字符串(null)转换为 ""
	 * @param inputValue 输入参数
	 * @return 输出 String
	 */
	public static final String valueOf(Long inputValue){
		if(inputValue==null)
			return "";
		return String.valueOf(inputValue);
	}
	
	/**
	 * MD5加密算法<p/>
	 * 推荐使用MD5Util.getMD5String
	 * @param inputValue 要加密的字符
	 * @return MD5串
	 */
	@Deprecated
	public static final String toMd5(String inputValue){
		if(inputValue==null)return "";
//        try {
//        	MessageDigest m=MessageDigest.getInstance("MD5");
//			m.update(inputValue.getBytes("UTF8"));
//			byte s[ ]=m.digest( );
//		    String result="";
//		    for(int i=0;i<s.length;i++){
//		        result+=Integer.toHexString((0x000000ff & s[i]) | 0xffffff00).substring(6);
//		    }
//		    return result.toUpperCase();
//		} catch (UnsupportedEncodingException e) {
//			System.out.println(inputValue+ "toMd5 error ,error message:"+e.getMessage());
//			return "";
//		} catch (NoSuchAlgorithmException e) {
//			System.out.println(inputValue+ "toMd5 error ,error message:"+e.getMessage());
//			return "";
//		}
		return MD5Util.getMD5String(inputValue);
	}
}
