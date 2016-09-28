package org.fastutil.mainland.util.db;

import com.efun.mainland.util.CommonUtil;
import com.efun.mainland.util.db.annotation.SelectDataBase;
import com.efun.mainland.util.db.annotation.SelectDataBase.Type;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.NestedRuntimeException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * DB select and excute<br/>
 * log name is "DBLOG"
 * 
 * @author Efun
 *
 */
public class DBProcessor implements BeanPostProcessor {
	private static final Logger log = LoggerFactory.getLogger("DBLOG");

	private boolean forceChoiceReadWhenWrite = false;

	private Map<String, Boolean> readMethodMap = new HashMap<String, Boolean>();

	public void setForceChoiceReadWhenWrite(boolean forceChoiceReadWhenWrite) {

		this.forceChoiceReadWhenWrite = forceChoiceReadWhenWrite;
	}

	@SuppressWarnings("unchecked")
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if (!(bean instanceof NameMatchTransactionAttributeSource)) {
			return bean;
		}

		try {
			NameMatchTransactionAttributeSource transactionAttributeSource = (NameMatchTransactionAttributeSource) bean;
			Field nameMapField = ReflectionUtils.findField(NameMatchTransactionAttributeSource.class, "nameMap");
			nameMapField.setAccessible(true);
			Map<String, TransactionAttribute> nameMap = (Map<String, TransactionAttribute>) nameMapField
					.get(transactionAttributeSource);

			for (Entry<String, TransactionAttribute> entry : nameMap.entrySet()) {
				RuleBasedTransactionAttribute attr = (RuleBasedTransactionAttribute) entry.getValue();

				if (!attr.isReadOnly()) {
					continue;
				}

				String methodName = entry.getKey();
				Boolean isForceChoiceRead = Boolean.FALSE;
				if (forceChoiceReadWhenWrite) {
					attr.setPropagationBehavior(Propagation.NOT_SUPPORTED.value());
					isForceChoiceRead = Boolean.TRUE;
				} else {
					attr.setPropagationBehavior(Propagation.SUPPORTS.value());
				}
				log.debug("read/write transaction process  method:{} force read:{}", methodName, isForceChoiceRead);
				readMethodMap.put(methodName, isForceChoiceRead);
			}

		} catch (Exception e) {
			throw new ReadWriteDataSourceTransactionException("process read/write transaction error", e);
		}

		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	private class ReadWriteDataSourceTransactionException extends NestedRuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8440271125464118921L;

		public ReadWriteDataSourceTransactionException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public Object determineReadOrWriteDB(ProceedingJoinPoint pjp) throws Throwable {
		String source = null;
		final Object[] args = pjp.getArgs();
		Signature signature = pjp.getSignature();
		Object target = pjp.getTarget();

		SelectDataBase selectDataBase = null;

		if (args != null && args.length > 0 && args[0] != null) {

			if (args[0] instanceof SelectDataBase.DataBase) {
				selectDataBase = new SelectDataBase() {

					@Override
					public Class<? extends Annotation> annotationType() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public DataBase value() {
						// TODO Auto-generated method stub
						return (SelectDataBase.DataBase) args[0];
					}

					@Override
					public Operation operate() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public DataBase defaultValue() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Type autowire() {
						// TODO Auto-generated method stub
						return null;
					}

				};
			} else if (args[0] instanceof SelectDataBase) {
				selectDataBase = (SelectDataBase) args[0];
			}

		}

		if (selectDataBase != null) {
			source = "Pass Argument";
			log.debug("@SelectDataBase:special select {},default {}", selectDataBase.value(),
					selectDataBase.defaultValue());
		} else {
			Method method = ((MethodSignature) signature).getMethod();
			selectDataBase = method.getAnnotation(SelectDataBase.class);
			if (selectDataBase != null) {
				source = "Method Annotation";
				log.debug("@SelectDataBase:method select {},default {}", selectDataBase.value(),
						selectDataBase.defaultValue());
			} else {
				selectDataBase = target.getClass().getAnnotation(SelectDataBase.class);
				if (selectDataBase != null) {
					source = "Class Annotation";
					log.debug("@SelectDataBase:target select {},default {}", selectDataBase.value(),
							selectDataBase.defaultValue());
				} else {
					selectDataBase = new SelectDataBase() {

						@Override
						public Class<? extends Annotation> annotationType() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public DataBase value() {
							// TODO Auto-generated method stub
							return DBDataSource.getDefaultDB();
						}

						@Override
						public Operation operate() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public DataBase defaultValue() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Type autowire() {
							// TODO Auto-generated method stub
							return null;
						}
					};
					source = "Select Dedault";
					log.debug("@SelectDataBase is null,select {}(dedault),default {}", selectDataBase.value(),
							selectDataBase.defaultValue());
				}
			}
		}

		boolean readonly = isChoiceReadDB(signature.getName());

		Type type = selectDataBase.autowire();
		if (type == null) {
			type = Type.AUTO;
		}

		if (type != Type.AUTO) {
			readonly = type.isReadOnly();
			log.debug("force select {}", type);
		}

		String argsStr = new StringBuilder().append("{").append(CommonUtil.objectConvertString(args)).append("}")
				.toString();
		long startTime = System.currentTimeMillis();
		try {
			Decision.getReadOnlyDecision().set(readonly);
			Decision.getDataBaseDecision().set(selectDataBase);

			Object result = pjp.proceed();

			log.info(
					"excute message:@SelectDataBase={},DB={},autowire={},readonly={},target={},method={},args={},time(ms)={}",
					source, DBDataSource.toString(selectDataBase), type, readonly, target, signature.getName(), argsStr,
					System.currentTimeMillis() - startTime);
			return result;
		} catch (Throwable th) {
			log.error(
					"SQL excute exception:@SelectDataBase={},DB={},autowire={},readonly={},target={},method={},args={},time(ms)={},message={}",
					source, DBDataSource.toString(selectDataBase), type, readonly, target, signature.getName(), argsStr,
					System.currentTimeMillis() - startTime, th.getMessage());
			throw th;
		} finally {
			Decision.getDataBaseDecision().remove();
			Decision.getReadOnlyDecision().remove();
		}
	}

	private boolean isChoiceReadDB(String methodName) {

		String bestNameMatch = null;
		for (String mappedName : this.readMethodMap.keySet()) {
			if (isMatch(methodName, mappedName)) {
				bestNameMatch = mappedName;
				break;
			}
		}
		if (bestNameMatch != null) {
			Boolean isForceChoiceRead = readMethodMap.get(bestNameMatch);
			if (isForceChoiceRead == Boolean.TRUE) {
				return true;
			}
		}

		return false;
	}

	protected boolean isMatch(String methodName, String mappedName) {
		return PatternMatchUtils.simpleMatch(mappedName, methodName);
	}

}
