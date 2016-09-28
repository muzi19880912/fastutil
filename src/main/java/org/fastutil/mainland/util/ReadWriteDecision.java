package org.fastutil.mainland.util;

public class ReadWriteDecision {
    
    public enum DataSourceType {
        write, read
    }
    
    
    private static final ThreadLocal<DataSourceType> holder = new ThreadLocal<DataSourceType>();

    public static final void markWrite() {
        holder.set(DataSourceType.write);
    }
    
    public static final void markRead() {
        holder.set(DataSourceType.read);
    }
    
    public static final void reset() {
//      holder.set(null);
    	holder.remove();
    }
    
    public static final boolean isChoiceNone() {
        return null == holder.get(); 
    }
    
    public static final boolean isChoiceWrite() {
        return DataSourceType.write == holder.get();
    }
    
    public static final boolean isChoiceRead() {
        return DataSourceType.read == holder.get();
    }

}
