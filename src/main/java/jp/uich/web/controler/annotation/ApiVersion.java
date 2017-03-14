package jp.uich.web.controler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {

  @AliasFor("supported")
  String[] value() default {};

  @AliasFor("value")
  String[] supported() default {};

  String atLeast() default "";

  String atMost() default "";

  String lessThan() default "";

  String greaterThan() default "";

}
