package platform.cache.Entity;

import java.lang.invoke.MethodHandle;
import java.util.Map;

/**
 * Created by @author: ahjun30@hotmail.com
 * Created on @date: 2019/5/7
 */
public class CacheEntity {
  private CacheBean cacheBean;
  private Map<String, Object[]> params;
  private Map<String, Object> data;
  private long firstExecuteTime;
  private long lastExecuteTime;
  private boolean autoRefresh;
  private String cacheName;
  private long timeout;
  private boolean containParam;
  private String[] dependenceReference;
  private String description;
  private MethodHandle handler;
  private boolean essential;
  private int retryTimes;

  public CacheBean getCacheBean() {
    return cacheBean;
  }

  public void setCacheBean(CacheBean cacheBean) {
    this.cacheBean = cacheBean;
  }

  public Map<String, Object[]> getParams() {
    return params;
  }

  public void setParams(Map<String, Object[]> params) {
    this.params = params;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public long getFirstExecuteTime() {
    return firstExecuteTime;
  }

  public void setFirstExecuteTime(long firstExecuteTime) {
    this.firstExecuteTime = firstExecuteTime;
  }

  public long getLastExecuteTime() {
    return lastExecuteTime;
  }

  public void setLastExecuteTime(long lastExecuteTime) {
    this.lastExecuteTime = lastExecuteTime;
  }

  public boolean isAutoRefresh() {
    return autoRefresh;
  }

  public void setAutoRefresh(boolean autoRefresh) {
    this.autoRefresh = autoRefresh;
  }

  public String getCacheName() {
    return cacheName;
  }

  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public boolean isContainParam() {
    return containParam;
  }

  public void setContainParam(boolean containParam) {
    this.containParam = containParam;
  }

  public String[] getDependenceReference() {
    return dependenceReference;
  }

  public void setDependenceReference(String[] dependenceReference) {
    this.dependenceReference = dependenceReference;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public MethodHandle getHandler() {
    return handler;
  }

  public void setHandler(MethodHandle handler) {
    this.handler = handler;
  }

  public boolean isEssential() {
    return essential;
  }

  public void setEssential(boolean essential) {
    this.essential = essential;
  }

  public int getRetryTimes() {
    return retryTimes;
  }

  public void setRetryTimes(int retryTimes) {
    this.retryTimes = retryTimes;
  }
}
