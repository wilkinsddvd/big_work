package com.uxsino.shiro.config;

import com.uxsino.common.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description yaml文件修改操作实体类
 * @Author wyn
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2021/4/16
 */
@Slf4j
@Component
public class YamlConfig {

    public static final String FILE_SPLIT = ";";

    public static final String FILE_SPLIT_DOT = ".";

    private final static DumperOptions OPTIONS = new DumperOptions();

    static {
        // 设置yaml读取方式为块读取
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        OPTIONS.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        OPTIONS.setPrettyFlow(false);
    }

    /**
     * 将 yaml 转成 Map。
     * <p>
     * 读取顺序：若 {@code fileName} 指向的外置文件存在（如 ./config/application.yml），则读外置；
     * 否则读 classpath 根目录的 {@code application.yml}（与 Spring Boot 主配置一致，便于 java -jar 与 IDE 启动）。
     * </p>
     *
     * @param fileName 通常为 {@link FileConstant#SYSTEM_CONFIG_FILE_PATH}
     */
    public Map<String, Object> getYamlToMap(String fileName) {
        LinkedHashMap<String, Object> yamlMap = new LinkedHashMap<>();
        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            File f = new File(fileName);
            if (f.isFile()) {
                in = new FileInputStream(f);
            } else {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.yml");
                if (in != null) {
                    log.debug("外置配置文件不存在，已使用 classpath:application.yml");
                }
            }
            if (in == null) {
                log.error("YAML 未找到: 外置 {} 不存在，且 classpath 中无 application.yml", fileName);
                return yamlMap;
            }
            yamlMap = yaml.loadAs(in, LinkedHashMap.class);
        } catch (Exception e) {
            log.error("{} load failed !!!", fileName, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            }
        }
        return yamlMap;
    }

    /**
     * 通过key在map里面寻找
     * key格式：aaa.bbb.ccc
     *
     * @param key
     * @param yamlMap
     * @return
     */
    public Object getValue(String key, Map<String, Object> yamlMap) {
        String[] keys = key.split("[.]");
        Object objValue = yamlMap.get(keys[0]);
        if (StringUtils.contains(key, FILE_SPLIT_DOT)) {
            return objValue instanceof Map
                    ? getValue(key.substring(key.indexOf(".") + 1), (Map<String, Object>) objValue) : null;
        } else {
            return objValue;
        }
    }

    /**
     * 使用递归的方式设置map中的值，仅适合单一属性;key的格式: "server.port" server.port=111
     *
     * @param key
     * @param value
     * @return
     */
    public Map<String, Object> setValue(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        String[] keys = key.split("[.]");
        int i = keys.length - 1;
        result.put(keys[i], value);
        if (i > 0) {
            return setValue(key.substring(0, key.lastIndexOf(".")), result);
        }
        return result;
    }

    /**
     * 从获取的map中重新设置值
     *
     * @param map
     * @param key
     * @param value
     * @return
     */
    public Map<String, Object> setValue(Map<String, Object> map, String key, Object value) {
        String[] keys = StringUtils.split(key, "\\.");
        int len = keys.length;
        Map temp = map;
        for (int i = 0; i < len - 1; i++) {
            if (temp.containsKey(keys[i])) {
                temp = (Map) temp.get(keys[i]);
            } else {
                return null;
            }
            if (i == len - 2) {
                temp.put(keys[i + 1], value);
            }
        }
        for (int j = 0; j < len - 1; j++) {
            if (j == len - 1) {
                map.put(keys[j], temp);
            }
        }
        return map;
    }

    /**
     * 修改yaml中属性的值
     *
     * @param key key是properties的方式： aaa.bbb.ccc (key不存在不修改)
     * @param value 新的属性值 （新属性值和旧属性值一样，不修改）
     * @param yamlToMap
     * @return true 修改成功，false 修改失败。
     */
    public boolean updateYaml(String key, @Nullable Object value, Map<String, Object> yamlToMap) {
        // 1.排除不需要进行修改的情况
        // 1.1 返回map是空
        if (Objects.isNull(yamlToMap)) {
            return false;
        }
        Object oldValue = this.getValue(key, yamlToMap);
        // 1.2 通过key没有找到不修改
        if (Objects.isNull(oldValue)) {
            log.error("{} key is not found", key);
            return false;
        }
        // 1.2 不是最小节点值，不修改，包含类类型的判断
        if (oldValue instanceof Map) {
            log.error("input key is not last node {}", key);
            return false;
        }
        // 1.3 新旧值一样 不修改
        if (Objects.equals(oldValue, value)) {
            log.info("newVal equals oldVal, newVal: {} , oldVal: {}", value, oldValue);
            return false;
        }
        // 1.4 如果新值是null,不修改
        if (Objects.isNull(value)) {
            log.info("newVal: {} is empty or null", value);
            return false;
        }
        // 1.5 不是数字类型，不修改
        /* BEGIN: Added by wyn for #113556, 2021/9/24 reviewer: shenbiru */
        if (value instanceof String) {
            String strNewValue = (String) value;
            strNewValue = StringUtils.trim(strNewValue);
            if (StringUtils.endsWith(strNewValue, "ms")) {
                String splitNum = StringUtils.substringBefore(strNewValue, "ms");
                if (!NumberUtils.isNumber(splitNum)) {
                    return false;
                }
            }
        }
        /* END: Added by wyn for #113556, 2021/9/24  reviewer: shenbiru */
        Yaml yaml = new Yaml(OPTIONS);
        String path = new File(FileConstant.SYSTEM_CONFIG_FILE_PATH).getPath();
        // 2. 利用yaml的dump属性加载
        try {
            Map<String, Object> resultMap = this.setValue(yamlToMap, key, value);
            if (Objects.nonNull(resultMap)) {
                yaml.dump(this.setValue(yamlToMap, key, value), new FileWriter(path));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("yaml file update failed !");
            log.error("更新配置文件异常.", e);
        }
        return false;
    }

}
