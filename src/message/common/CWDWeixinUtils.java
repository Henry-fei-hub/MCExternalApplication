package message.common;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delicacy.common.BaseHelpUtils;
import delicacy.system.bean.BaseEmployee;
import delicacy.system.dao.Employee;
import message.util.HttpClientUtils;
import net.sf.json.JSONObject;

public class CWDWeixinUtils {

    private static final Logger log = Logger.getLogger(CWDWeixinUtils.class);

    public static Integer AGENTID = 1000022;

    public static String CORP_SECRET = "W9KmTY5z1ryCc7EB2FkTZnoos8cc6mRB1iQglGKcgco";

    private static String ACCESS_TOKEN;

    /**
     * 获取AccessToken
     *
     * @return
     */
    public static String getAccessToken() {
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?" + "corpid=" + WeixinUtils.CORP_ID
                    + "&corpsecret=" + CORP_SECRET;

            if (!BaseHelpUtils.isNullOrEmpty(ACCESS_TOKEN)) {
                // 检验是否有效
                boolean b = WeixinUtils.checkAccessToken(AGENTID, ACCESS_TOKEN);
                if (b) {
                    return ACCESS_TOKEN;
                } else {
                    String result = HttpClientUtils.get(url, "utf-8");
                    JSONObject json = JSONObject.fromString(result);
                    ACCESS_TOKEN = (String) json.get("access_token");
                    // int expires_in = json.getInt("expires_in");
                }
            } else {// AccessToken已过期
                String result = HttpClientUtils.get(url, "utf-8");
                JSONObject json = JSONObject.fromString(result);
                ACCESS_TOKEN = (String) json.get("access_token");
                // int expires_in = json.getInt("expires_in");
            }
        } catch (Exception e) {
            log.error("getAccessToken Exception", e);
        }
        return ACCESS_TOKEN;
    }

    public static void sendWXMsgToUser(String content, java.lang.Object... args) {
        if (!BaseHelpUtils.isNullOrEmpty(content) && null != args && args.length > 0) {
            try {
                Object[] employeeIds = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    employeeIds[i] = BaseHelpUtils.getIntValue(args[i]);
                }
                Employee dao;
                dao = new Employee();
                dao.setConditionStatus("=", 0);
                dao.addCondition(BaseEmployee.CS_EMPLOYEE_ID, "in", employeeIds);
                List<BaseEmployee> list = dao.conditionalLoad();
                String touser = "";
                int index = 0;
                for (BaseEmployee baseEmployee : list) {
                    if (!BaseHelpUtils.isNullOrEmpty(baseEmployee.getCompanyWeixin())) {
                        if (index != 0) {
                            touser += "|";
                        }
                        touser += baseEmployee.getCompanyWeixin();
                        index++;
                    }
                }
                if (touser.length() > 0) {
                    WeixinUtils.sendTextMsgToUser(getAccessToken(), AGENTID, touser, content);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean sendTextMsgToUser(String touser, String content) {
        return WeixinUtils.sendTextMsg(getAccessToken(), AGENTID, touser, null, null, content);
    }

    public static String getUserIdByCode(String code) throws Exception {
        return WeixinUtils.getUserIdByCode(getAccessToken(), code);
    }

    public static String createGroupChat(Map<String, Object> params) {
        return WeixinUtils.createGroupChat(getAccessToken(), params);
    }

    public static boolean updateGroupChat(Map<String, Object> params) {
        return WeixinUtils.updateGroupChat(getAccessToken(), params);
    }

    public static JSONObject getGroupChatInfo(String chatId) {
        return  WeixinUtils.getGroupChatInfo(getAccessToken(), chatId);
    }

    public static boolean sendGroupChatTxtMsg(String chatid, String content) {
        return WeixinUtils.sendGroupChatTxtMsg(getAccessToken(), chatid, content, 0);
    }
}
