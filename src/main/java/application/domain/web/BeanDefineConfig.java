package application.domain.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import platform.cache.CacheManager;

/**
 * Created by @author: ahjun30@hotmail.com
 * Created on @date: 2019/5/8
 */
@Component
public class BeanDefineConfig implements ApplicationListener<ContextRefreshedEvent> {

  @Autowired
  private CacheManager cacheManager;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    cacheManager.initCache();
  }
}
