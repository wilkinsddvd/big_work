package com.uxsino.common.utils;

import com.uxsino.entity.User;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.SimpleHash;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 加密处理
 */
public class PasswordEncoder {

    public final static String SALT = "salt";

    public final static String PASSWORD = "passWord";

    public final static int HASH_ITERATIONS = 2;

    public final static String ALGORITHM_NAME = "md5";

    /**
     * 创建盐值
     *
     * @param length 盐值位数
     * @return
     */
    public static String createSalt(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 密码加密
     *
     * @param userName 用户名
     * @param passWord 原密码
     * @return 盐值、加密密码
     */
    public static Map<String, String> enCodePassWord(String userName, String passWord) {
        Map<String, String> map = new HashMap<>();
        // 盐值
        String salt = createSalt(32);
        // 散列次数
        // int hashIterations = 2;
        // 第1个参数：散列算法
        // 第2个参数：明文，原始密码
        // 第3个参数：盐，通过使用随机数
        // 第4个参数：散列的次数，比如散列两次
        SimpleHash simpleHash = new SimpleHash(ALGORITHM_NAME, passWord, userName + salt, HASH_ITERATIONS);
        map.put(SALT, salt);
        map.put(PASSWORD, simpleHash.toString());
        return map;
    }

    /**
     * 通过盐加密密码
     *
     * @param credentialsSalt 用户名+salt
     * @param passWord        原密码
     * @return 加密密码
     */
    public static String enCodePassWordBySalt(String credentialsSalt, String passWord) {
        SimpleHash simpleHash = new SimpleHash(ALGORITHM_NAME, passWord, credentialsSalt, HASH_ITERATIONS);
        return simpleHash.toString();
    }

    /**
     * 验证用户密码
     *
     * @param user
     * @param password
     * @return
     */
    public static boolean checkPassword(User user, String password) {
        if (user == null || StringUtils.isEmpty(user.getPassword()) || StringUtils.isEmpty(user.getCredentialsSalt())) {
            return false;
        }
        String enCodePassWordBySalt = enCodePassWordBySalt(user.getCredentialsSalt(), password);
        if (!user.getPassword().equals(enCodePassWordBySalt)) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        // 获取用户名和密码明文计算出的密码密文
        /*String[] userNameList = {"zhaomiaomiao", "wangjuan", "wangyahui", "lifan", "liyong"};
        String[] passWordList = {"zhao_miaomiao", "wang_juan", "wang_yahui", "li_fan", "li_yong"};*/

        String[] userNameList = {"aaa"};
        String[] passWordList = {"bbb"};
        for (int i = 0;i<userNameList.length;i++) {
            String salt = createSalt(32);
            SimpleHash simpleHash = new SimpleHash(ALGORITHM_NAME, passWordList[i], userNameList[i] + salt, HASH_ITERATIONS);
            System.out.println(userNameList[i]);
            System.out.println(salt);
            System.out.println(simpleHash.toString());
        }
    }
}
