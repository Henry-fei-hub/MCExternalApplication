package message.util;

import com.pomplatform.db.bean.BaseSystemConfig;

import java.util.HashMap;
import java.util.Map;



public class StaticUtils {
	// 设置系统配置参数
	public static Map<Integer, BaseSystemConfig> configMap = new HashMap<>();

	/**
	 * 系统配置参数：企业微信配置信息
	 */
	public static final Integer CONFIG_TYPE_1 = 1;
	/**
	 * 系统配置参数：企业邮箱配置信息
	 */
	public static final Integer CONFIG_TYPE_2 = 2;
	/**
	 * 系统配置参数：发送邮件配置信息
	 */
	public static final Integer CONFIG_TYPE_3 = 3;
	/**
	 * 系统配置参数：手机短息配置信息
	 */
	public static final Integer CONFIG_TYPE_5 = 5;

	/**
	 * 系统配置参数：企业微信打卡配置信息
	 */
	public static final Integer CONFIG_TYPE_13 = 13;
}
