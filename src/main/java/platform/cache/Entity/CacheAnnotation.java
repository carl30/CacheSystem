package platform.cache.Entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by @author: ahjun30@hotmail.com
 * Created on @date: 2019/5/7
 */
@Target( {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheAnnotation {
  // 缓存名字
  String name();
  // 缓存有效时间
  int timeout() default 30;
  // 缓存有效时间
  CacheTimeEnum timeoutType() default CacheTimeEnum.MINUTE;
  // 是否自动刷新数据
  boolean autoRefresh() default true;
  // 前置依赖
  String[] dependenceReference() default {};
  // 缓存说明
  String description() default "这个人很懒，没有填缓存说明";
  // 是否必要项
  boolean essential() default false;
  // 重试次数
  int retryTimes() default 0;
}
