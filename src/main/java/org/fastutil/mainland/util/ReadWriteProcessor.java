package org.fastutil.mainland.util;

import org.aspectj.lang.ProceedingJoinPoint;
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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ReadWriteProcessor implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(ReadWriteProcessor.class);
    
    private boolean forceChoiceReadWhenWrite = false;
    
    private Map<String, Boolean> readMethodMap = new HashMap<String, Boolean>();

    public void setForceChoiceReadWhenWrite(boolean forceChoiceReadWhenWrite) {
        
        this.forceChoiceReadWhenWrite = forceChoiceReadWhenWrite;
    }
    

    @SuppressWarnings("unchecked")
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if(!(bean instanceof NameMatchTransactionAttributeSource)) {
            return bean;
        }
        
        try {
            NameMatchTransactionAttributeSource transactionAttributeSource = (NameMatchTransactionAttributeSource)bean;
            Field nameMapField = ReflectionUtils.findField(NameMatchTransactionAttributeSource.class, "nameMap");
            nameMapField.setAccessible(true);
            Map<String, TransactionAttribute> nameMap = (Map<String, TransactionAttribute>) nameMapField.get(transactionAttributeSource);
            
            for(Entry<String, TransactionAttribute> entry : nameMap.entrySet()) {
                RuleBasedTransactionAttribute attr = (RuleBasedTransactionAttribute)entry.getValue();

                if(!attr.isReadOnly()) {
                    continue;
                }
                
                String methodName = entry.getKey();
                Boolean isForceChoiceRead = Boolean.FALSE;
                if(forceChoiceReadWhenWrite) {
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
        
        if (isChoiceReadDB(pjp.getSignature().getName())) {
        	ReadWriteDecision.markRead();
        } else {
        	ReadWriteDecision.markWrite();
        }
            
        try {
            return pjp.proceed();
        } finally {
        	ReadWriteDecision.reset();
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

        Boolean isForceChoiceRead = readMethodMap.get(bestNameMatch);
        if(isForceChoiceRead == Boolean.TRUE) {
            return true;
        }
        
        if(ReadWriteDecision.isChoiceWrite()) {
            return false;
        }

        return isForceChoiceRead != null;
    }


    protected boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

}

