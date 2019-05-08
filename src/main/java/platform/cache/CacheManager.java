package platform.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import platform.cache.Entity.CacheAnnotation;
import platform.cache.Entity.CacheBean;
import platform.cache.Entity.CacheEntity;
import platform.cache.Entity.CacheTimeEnum;
import platform.util.CollectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by @author: ahjun30@hotmail.com
 * Created on @date: 2019/5/7
 */
public class CacheManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);
  private Map<String, CacheEntity> cacheMap = new ConcurrentHashMap<>();
  private ApplicationContext applicationContext;
  private ScheduledThreadPoolExecutor executor;
  private Object lock = new Object();
  private List<Runnable> executeRunnableList = new ArrayList<>();
  private boolean isInit = true;

  public synchronized void initCache() {
    try {
      if (null == executor) {
        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);
      }
      cacheMap.clear();
      executeRunnableList.clear();
      Map<String, CacheBean> beanMap = applicationContext.getBeansOfType(CacheBean.class);
      Collection<CacheBean> beans = beanMap.values();
      for (CacheBean bean : beans) {
        List<Method> methods = CollectionUtils.findAll(Arrays.asList(bean.getClass().getMethods()),
            x -> x.getAnnotation(CacheAnnotation.class) != null);
        for (Method method : methods) {
          CacheEntity cache = new CacheEntity();
          CacheAnnotation cacheAnnotation = method.getAnnotation(CacheAnnotation.class);
          Parameter[] parameters = method.getParameters();
          cache.setContainParam(null != parameters && parameters.length > 0);
          cache.setAutoRefresh(cacheAnnotation.autoRefresh());
          cache.setCacheBean(bean);
          cache.setCacheName(cacheAnnotation.name());
          cache.setTimeout(getTimeout(cacheAnnotation.timeout(), cacheAnnotation.timeoutType()));
          cache.setData(new ConcurrentHashMap<>());
          cache.setParams(new ConcurrentHashMap<>());
          cache.setDescription(cacheAnnotation.description());
          cache.setHandler(convertHandler(method, bean));
          cacheAnnotation.dependenceReference();
          cache.setDependenceReference(cacheAnnotation.dependenceReference().length > 0 ?
              cacheAnnotation.dependenceReference() : null);
          cache.setEssential(cacheAnnotation.essential());
          cache.setRetryTimes(cacheAnnotation.retryTimes());
          cacheMap.put(cacheAnnotation.name(), cache);
        }
      }
      List<String> keys = sortKey();
      for (String key : keys) {
        CacheEntity cache = cacheMap.get(key);
        executeSaveCache(cache);
        if (cache.isAutoRefresh()) {
          Runnable runnable = () -> executeSaveCache(cache);
          executor.scheduleAtFixedRate(runnable, cache.getTimeout(), cache.getTimeout(), TimeUnit.MILLISECONDS);
          executeRunnableList.add(runnable);
        }
      }
    } catch (Throwable e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  private void executeSaveCache(CacheEntity cache) {
    try {
      long executeBegin = System.currentTimeMillis();
      MethodHandle method = cache.getHandler();
      if (!CollectionUtils.isNullOrEmpty(cache.getData())) {
        isInit = false;
      }
      Object result = null;
      if (!cache.isContainParam()) {
        try {
          result = method.invoke();
        } catch (Throwable throwable) {
          LOGGER.error("获取缓存失败. CacheName: " + cache.getCacheName(), throwable);
        }
        if (isInit && null == result && cache.isEssential() && cache.getRetryTimes() > 0) {
          for (int i = 0; i < cache.getRetryTimes(); i++) {
            try {
              result = method.invoke();
            } catch (Throwable throwable) {
              LOGGER.error("重试获取缓存失败. CacheName: " + cache.getCacheName()
                  + ", 重试次数: " + i + 1, throwable);
            }
            if (null != result) {
              break;
            }
          }
          if (null == result) {
            LOGGER.error("无法获取强依赖缓存数据，系统主动退出，点火失败。", "CacheName: " + cache.getCacheName());
            ((ConfigurableApplicationContext) applicationContext).close();
            throw new Throwable("无法获取强依赖缓存数据，系统主动退出，点火失败。CacheName: " + cache.getCacheName());
          }
        }
        if (null != result) {
          cache.getData().put(cache.getCacheName(), result);
          long nowTime = System.currentTimeMillis();
          if (cache.getFirstExecuteTime() <= 0) {
            cache.setFirstExecuteTime(nowTime);
          }
          cache.setLastExecuteTime(nowTime);
        } else {
          LOGGER.error("获取缓存数据失败", "缓存名: " + cache.getCacheName());
        }
      } else {
        if (!cache.getParams().isEmpty()) {
          try {
            for (String paramKey : cache.getParams().keySet()) {
              result = method.invokeWithArguments(cache.getParams().get(paramKey));
              cache.getData().put(paramKey, result);
              cache.setFirstExecuteTime(System.currentTimeMillis());
              long nowTime = System.currentTimeMillis();
              if (cache.getFirstExecuteTime() <= 0) {
                cache.setFirstExecuteTime(nowTime);
              }
              cache.setLastExecuteTime(nowTime);
            }
          } catch (Throwable throwable) {
            LOGGER.error("带参数获取缓存失败. CacheName: " + cache.getCacheName(), throwable);
          }
        }
      }
      long executeEnd = System.currentTimeMillis();
      LOGGER.info("本地缓存：" + cache.getCacheName()
          + "刷新，耗时：" + (executeEnd - executeBegin) + "毫秒");
    } catch (Throwable throwable) {
      LOGGER.error(throwable.getMessage(), throwable);
    }
  }

  /**
   * 将被依赖的缓存key移前面
   *
   * @return
   */
  private List<String> sortKey() {
    LinkedList<String> linkedList = new LinkedList<>(cacheMap.keySet());
    Collection<CacheEntity> caches = cacheMap.values();
    if (!CollectionUtils.isNullOrEmpty(linkedList)) {
      for (CacheEntity cache : caches) {
        if (cache.getDependenceReference() != null) {
          int cIndex = linkedList.indexOf(cache.getCacheName());
          int refIndex = 0;
          for (String s : cache.getDependenceReference()) {
            int index = linkedList.indexOf(s);
            if (index > refIndex) {
              refIndex = index;
            }
          }
          if (cIndex < refIndex) {
            linkedList.add(refIndex + 1, cache.getCacheName());
            linkedList.remove(cIndex);
          }
        }
      }
    }
    return linkedList;
  }

  private MethodHandle convertHandler(Method method, CacheBean bean) {
    try {
      return MethodHandles.lookup().unreflect(method).bindTo(bean);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
    return null;
  }

  private long getTimeout(int timeout, CacheTimeEnum timeoutType) {
    int timeOut;
    switch (timeoutType) {
      case DAY:
        timeOut = timeout * 1000 * 60 * 60 * 24;
        break;
      case HOUR:
        timeOut = timeout * 1000 * 60 * 60;
        break;
      case MINUTE:
        timeOut = timeout * 1000 * 60;
        break;
      case SECOND:
        timeOut = timeout * 1000;
        break;
      default:
        timeOut = timeout;
    }
    return timeOut;
  }
}
