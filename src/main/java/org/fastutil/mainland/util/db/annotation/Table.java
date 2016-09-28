package org.fastutil.mainland.util.db.annotation;

import java.lang.annotation.*;

/**
 * Created by fastutil on 2016-08-23.<br/>
 * table(表)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Table {
    String alias() default "";
}
