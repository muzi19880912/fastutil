package org.fastutil.mainland.util;

import org.apache.log4j.Logger;

/**
 * 打印sql与参数
 * 
 * @author Administrator
 *
 */
public class SqlPrintUtil {
	private static Logger logger = Logger.getLogger("SQL");

	public static final <T> void printSql(String sql, @SuppressWarnings("unchecked") T... params) {
		StringBuilder strBuilder = new StringBuilder();
		try {
//			StackTraceElement[] stack=new Exception().getStackTrace();
//			if(stack.length>1){
//				strBuilder.append(" ClassName ").append(stack[1].getClassName());
//				strBuilder.append(" ").append(stack[1].getMethodName());
//			}
			strBuilder.append("sql:[").append(sql).append("] params:[");
			CommonUtil.objectConvertString(strBuilder, params).append("]");
			logger.info(strBuilder.toString());
		} finally {
			strBuilder.setLength(0);
			strBuilder = null;
		}
	}
}
