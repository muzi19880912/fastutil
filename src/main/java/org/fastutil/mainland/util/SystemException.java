package org.fastutil.mainland.util;

/**
 * 
 * @author tink
 *
 */
public class SystemException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5947616520127105328L;

	public SystemException(String frdMessage) {
		super(createFriendlyErrMsg(frdMessage));
	}

	public SystemException(Throwable throwable) {
		super(throwable);
	}

	public SystemException(Throwable throwable, String frdMessage) {
		super(throwable);
	}

	private static final String createFriendlyErrMsg(String msgBody) {
		String prefixStr = "抱歉,";
		String suffixStr = "请稍后试与管理员联系！";
		StringBuffer friendlyErrMsg = new StringBuffer();
		friendlyErrMsg.append(prefixStr);
		friendlyErrMsg.append(msgBody);
		friendlyErrMsg.append(suffixStr);
		return friendlyErrMsg.toString();
	}
}
