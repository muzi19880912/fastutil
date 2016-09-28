package org.fastutil.mainland.util.db.annotation;

import java.lang.annotation.*;

/**
 * Created by fastutil on 2016-08-23.<br/>
 * primary key(主键)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface PrimaryKey {
    String alias() default "";

    boolean unique() default true;

    boolean primary() default true;
}
