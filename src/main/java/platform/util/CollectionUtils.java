package platform.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by @author: ahjun30@hotmail.com
 * Created on @date: 2019/5/7
 */
public class CollectionUtils {
  public static <K, V> Map<K, V> sortByValue(Map<K, V> map, Comparator<Entry<K, V>>
      comparator) {
    return map.entrySet()
        .stream()
        .sorted(comparator)
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (e1, e2) -> e1,
            LinkedHashMap::new
        ));
  }

  public static <T> boolean isNullOrEmpty(Collection<T> collection) {
    boolean result = false;
    if (collection == null || collection.isEmpty()) {
      result = true;
    }
    return result;
  }

  public static <Key, Value> boolean isNullOrEmpty(Map<Key, Value> map) {
    boolean result = false;
    if (map == null || map.isEmpty()) {
      result = true;
    }
    return result;
  }

  public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
    T t = null;
    if (!isNullOrEmpty(collection)) {
      Optional<T> optional = collection.stream().filter(predicate).findFirst();
      t = optional.orElse(null);
    }
    return t;
  }

  public static <T> List<T> findAll(List<T> list, Predicate<? super T> predicate) {
    List<T> result = null;
    if (!isNullOrEmpty(list) && predicate != null) {
      result = list.stream().filter(predicate).collect(Collectors.toList());
    }
    return result;
  }

  public static <T> boolean uniquePush(T item, List<T> items) {
    boolean result = false;
    if (null != items && null != item && !items.contains(item)) {
      result = items.add(item);
    }
    return result;
  }

}
