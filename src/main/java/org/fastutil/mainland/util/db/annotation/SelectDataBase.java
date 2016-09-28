package org.fastutil.mainland.util.db.annotation;

import java.lang.annotation.*;

/**
 * 获取数据库
 * 
 * @author Administrator
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface SelectDataBase {

	/**
	 * 当前数据库，默认为DB_0
	 * 
	 * @see DataBase
	 * 
	 * @return
	 */
	DataBase value() default DataBase.DB_0;

	/**
	 * 默认数据库，默认为DB_0，当“当前数据库”不存在时使用
	 * 
	 * @see DataBase
	 * 
	 * @return
	 */
	DataBase defaultValue() default DataBase.DB_0;

	/**
	 * SQL操作
	 * 
	 * @see Operation
	 * 
	 * @return
	 */
	Operation operate() default Operation.SELECT;

	/**
	 * 数据库装配策略，默认值AUTO(自动选择)
	 * 
	 * @see Type
	 * 
	 * @return
	 */
	Type autowire() default Type.AUTO;

	/**
	 * DataBase 定义
	 * 
	 * @author fastutil
	 *
	 */
	enum DataBase {
		DB_0, DB_1, DB_2, DB_3, DB_4, DB_5, DB_6, DB_7, DB_8, DB_9, DB_10, DB_11, DB_12, DB_13, DB_14, DB_15, DB_16, DB_17, DB_18, DB_19, DB_20, DB_21, DB_22, DB_23, DB_24, DB_25, DB_26, DB_27, DB_28, DB_29, DB_30, DB_31
    }

	/**
	 * SQL操作 定义
	 * 
	 * @author fastutil
	 *
	 */
	enum Operation {
		SELECT, UPDATE, INSERT, DELETE
	}

	/**
	 * 数据库装配类型 定义
	 * 
	 * @author fastutil
	 *
	 */
	enum Type {
		READ(true), WRITE(false), AUTO;

		private boolean readOnly;

		Type() {
		}

		Type(boolean readOnly) {
			this.readOnly = readOnly;
		}

		public boolean isReadOnly() {
			return this.readOnly;
		}
	}
}
