package com.uxsino.common.constant;

import java.io.File;

/**
 * @Description 上传文件常量类
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2021/4/17
 */
public class FileConstant {

    public static final String FILE_SPLIT = ";";

    public static final String FILE_SPLIT_DOT = ".";

    public static final String FILE_SPLIT_DOUBLE_DOT = "..";

    public static final String REGIX_DIGITAL = "[^0-9]";

    public static final String FILE_UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "public"
            + File.separator + "files" + File.separator;

    public static final String FILE_SCRIPTS_PATH = System.getProperty("user.dir") + File.separator + "public"
            + File.separator + "scripts";

    public static final String FILE_DECOMPRESS_PATH = System.getProperty("user.dir") + File.separator + "public"
            + File.separator + "decompress";

    public static final String FILE_TEMP_UPLOAD_PATH = System.getProperty("user.dir") + "/public/files/temp";

    public static final String FILECONFIG_NAME = "fileConfig.xml";

    public static final String FILECONFIG_ROOT_LABEL = "fileList";

    /**
     * Shiro/Yaml 工具读取的配置路径（外置，可选）。
     * 若该文件不存在，{@link com.uxsino.shiro.config.YamlConfig#getYamlToMap} 会回退到 classpath 下的 {@code application.yml}（与 Spring Boot 主配置一致）。
     * 热更新写入仍指向此路径（若需写入请保证目录存在）。
     */
    public static final String SYSTEM_CONFIG_FILE_PATH = "./config/application.yml";

    public static final String YAML_NODE_SPRING_WEBAGENT_HEARTBEAT_PORT = "spring.webagent.heaerbeat_port";

    public static final String YAML_NODE_SPRING_WEBAGENT_HEART_DELAYTIME = "spring.webagent.heart_delaytime";

    public static final String YAML_NODE_SERVER_PORT = "server.port";

    public static final String YAML_NODE_SERVER_CONNECTION_TIMEOUT = "server.connection-timeout";

    public static final String YAML_NODE_SERVER_TOKEN_EXPIRE_TIME = "server.token.expire-time";

    public static final String YAML_NODE_UXWEBCONFIG_TIMESERVER_DETECTIONTIME = "uxwebconfig.timeserver.detectionTime";

    public static final String FILE_TYPE_TOOL = "tools";

    public static final String FILE_TYPE_TOOL_EN = "工具";

    public static final String FILE_TYPE_DRIVER = "drivers";

    public static final String FILE_TYPE_DRIVER_EN = "驱动";

    public static final String FILE_TYPE_DOCUMENT = "documents";

    public static final String FILE_TYPE_DOCUMENT_EN = "文档";

    public static final int INDEX_0 = 0;

    public static final int UNIT_SIZE = 1024;

    public static final String ATTRNAME_INDEX_0 = "fileName";

    public static final int INDEX_1 = 1;

    public static final String ATTRNAME_INDEX_1 = "fileType";

    public static final int INDEX_2 = 2;

    public static final String ATTRNAME_INDEX_2 = "script";

    public static final int INDEX_3 = 3;

    public static final String ATTRNAME_INDEX_3 = "packageType";

    public static final int INDEX_4 = 4;

    public static final String ATTRNAME_INDEX_4 = "description";

    public static final int INDEX_5 = 5;

    public static final String ATTRNAME_INDEX_5 = "size";

    public static final int INDEX_6 = 6;

    public static final String ATTRNAME_INDEX_6 = "md5";

    public static final int INDEX_7 = 7;

    public static final String ATTRNAME_INDEX_7 = "path";
}
