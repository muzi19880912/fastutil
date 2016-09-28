package org.fastutil.mainland.util.db;

import org.fastutil.mainland.util.db.annotation.SelectDataBase;

/**
 * 选择器<br/>
 * ThreadLocal<br/>
 * 线程安全
 * 
 * @author fastutil
 *
 */
public final class Decision {

	/**
	 * 只读选择
	 * 
	 * @return
	 */
	protected static final ThreadLocal<Boolean> getReadOnlyDecision() {
		return EnumDecision.READONLY_HOLDER;
	}

	/**
	 * 数据库选择
	 * 
	 * @return
	 */
	protected static final ThreadLocal<SelectDataBase> getDataBaseDecision() {
		return EnumDecision.DATABASE_HOLDER;
	}

	protected static final class EnumDecision {
		protected static final ThreadLocal<Boolean> READONLY_HOLDER = new ThreadLocal<Boolean>() {
			protected Boolean initialValue() {
				return false;
			}
        };
		protected static final ThreadLocal<SelectDataBase> DATABASE_HOLDER = new ThreadLocal<SelectDataBase>();
	}
}
