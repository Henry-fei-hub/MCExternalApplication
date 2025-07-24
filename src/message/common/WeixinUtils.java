package message.common;

import delicacy.common.BaseHelpUtils;
import delicacy.common.MapUtils;
import message.util.HttpClientUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @Title: WeixinUtils.java
 * @Package delicacy.wx
 * @Description: API_DOC: https://work.weixin.qq.com/api/doc
 * @author CL
 * @date 2017年5月17日
 */
public class WeixinUtils {

    private static final Logger log = Logger.getLogger(WeixinUtils.class);

    public static String CORP_ID = "wwdfc157b24bebba67";
    //
    public static String TXL_CORP_SECRET = "3Nop4cUnFPmNgw612XtWygVQdn9ymb9XeHJEERKzv8k";//通讯录编辑权限的 secret
    //
    public static String TEST_USERID = "qy01e08ae36810ac0028e026d171";

//    public static String CORP_ID = "";

//    public static String TXL_CORP_SECRET = "";//通讯录编辑权限的 secret

//    public static String TEST_USERID = "";

    public static String TXL_ACCESS_TOKEN;//通讯录的 ACCESS_TOKEN

    public final static Long TEST_DEPARTMENT_ID = 1090012687L;

    public static String getTXLAccessToken() {
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?" + "corpid=" + CORP_ID + "&corpsecret="
                    + TXL_CORP_SECRET;

            if (!BaseHelpUtils.isNullOrEmpty(TXL_ACCESS_TOKEN)) {
                // 检验是否有效
                boolean b = checkTXLAccessToken(TXL_ACCESS_TOKEN);
                if (b) {
                    return TXL_ACCESS_TOKEN;
                } else {
                    String result = HttpClientUtils.get(url, "utf-8");
                    JSONObject json = JSONObject.fromString(result);
                    TXL_ACCESS_TOKEN = (String) json.get("access_token");
                    // int expires_in = json.getInt("expires_in");
                }
            } else {// AccessToken已过期
                String result = HttpClientUtils.get(url, "utf-8");
                JSONObject json = JSONObject.fromString(result);
                TXL_ACCESS_TOKEN = (String) json.get("access_token");
                // int expires_in = json.getInt("expires_in");
            }
        } catch (Exception e) {
            log.error("getAccessToken Exception", e);
        }
        return TXL_ACCESS_TOKEN;
    }

    public static String getUserIdByCode(String accessToken, String code) throws Exception {
        if (!BaseHelpUtils.isNullOrEmpty(code)) {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=" + accessToken + "&code=" + code;
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                if (json.has("UserId") && !BaseHelpUtils.isNullOrEmpty(json.get("UserId"))) {
                    return json.getString("UserId");
                }

            }
        }
        return null;
    }

    public static boolean checkAccessToken(Integer accessToken, String agentId) {
        boolean b = true;
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/agent/get?access_token=" + accessToken + "&agentid="
                    + agentId;
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {// 获取access_token时AppSecret错误，或者access_token无效
                b = false;
            }
        } catch (Exception e) {
            log.error("checkAccessToken Exception", e);
        }
        return b;
    }

    public static boolean checkTXLAccessToken(String access_token) {
        boolean b = true;
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=" + access_token + "&userid="
                    + TEST_USERID;
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {// 获取access_token时AppSecret错误，或者access_token无效
                b = false;
            }
        } catch (Exception e) {
            log.error("checkAccessToken Exception", e);
        }
        return b;
    }


    /**
     * 给用户发送文字微信消息
     * @param accessToken
     * @param agentId
     * @param touser
     * @param content
     * @return
     */
    public static boolean sendTextMsgToUser(String accessToken, Integer agentId, String touser, String content) {
        return sendTextMsg(accessToken, agentId, touser, null, null, content);
    }

    public static boolean sendTextMsg(String accessToken, Integer agentId, String touser, String toparty, String totag, String content) {
        try {
            boolean flag = true;
            if ((BaseHelpUtils.isNullOrEmpty(touser) && BaseHelpUtils.isNullOrEmpty(toparty)
                    && BaseHelpUtils.isNullOrEmpty(totag)) || BaseHelpUtils.isNullOrEmpty(content)) {
                return false;
            }
            Map<String, Object> params = new HashMap<>();
            if (!BaseHelpUtils.isNullOrEmpty(touser)) {
                params.put("touser", touser);
            }
            if (!BaseHelpUtils.isNullOrEmpty(toparty)) {
                params.put("toparty", toparty);
            }
            if (!BaseHelpUtils.isNullOrEmpty(totag)) {
                params.put("totag", totag);
            }
            params.put("msgtype", "text");
            params.put("agentid", agentId);
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("content", content);
            params.put("text", contentMap);
            String result = HttpClientUtils.postParameters(
                    "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {
                flag = false;
            }
            return flag;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 发送图文消息
     * @param accessToken
     * @param mapOne
     * @return
     */
    public static Boolean sendGraphicMsg(String accessToken, Map<String, Object> mapOne) {
        try {
            boolean flag = true;
            if ((BaseHelpUtils.isNullOrEmpty(accessToken) && BaseHelpUtils.isNullOrEmpty(mapOne))) {
                return false;
            }
            String s = MapUtils.toJSON(mapOne);
            String result = HttpClientUtils.postParameters(
                    "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken, s);
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {
                flag = false;
            }
            return flag;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) throws SQLException {
    }




    /**
     * **** 微信部门数据处理模块 开始 ****
     */
    /**
     *
     * @Title: createDepartment
     * @Description: 创建微信部门 API_DOC: https://work.weixin.qq.com/api/doc#10076
     * @param @param params
     * @param @return
     * @return Integer
     * @throws
     */
    public static Long createDepartment(Map<String, Object> params) {
        Long departmentId = null;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token=" + access_token;
            String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                departmentId = json.getLong("id");
            }
        } catch (Exception e) {
            log.error("创建微信部门失败", e);
        }
        return departmentId;
    }

    /**
     *
     * @Title: deleteDepartmentById
     * @Description: 删除部门 API_DOC: https://work.weixin.qq.com/api/doc#10079
     * @param @param departmentId
     * @param @return
     * @return boolean
     * @throws
     */
    public static boolean deleteDepartmentById(Long departmentId) {
        boolean b = true;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/department/delete?access_token=" + access_token + "&id="
                    + departmentId;
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {//0删除成功
                b = false;
            }
        } catch (Exception e) {
            log.error("删除微信部门信息失败", e);
        }
        return b;
    }

    /**
     *
     * @Title: updateDepartment
     * @Description: 更新微信部门信息 API_DOC: https://work.weixin.qq.com/api/doc#10077
     * @param @param params
     * @param @return
     * @return boolean
     * @throws
     */
    public static boolean updateDepartment(Map<String, Object> params) {
        boolean b = true;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/department/update?access_token=" + access_token;
            String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {
                b = false;
            }
        } catch (Exception e) {
            log.error("更新微信部门失败", e);
        }
        return b;
    }

    /**
     *
     * @Title: getDepartmentList
     * @Description: 获取部门列表信息 API_DOC: https://work.weixin.qq.com/api/doc#10093
     * @param @param departmentId
     * @param @return
     * @return JSONObject
     * @throws
     */
    public static JSONObject getDepartmentList(Long departmentId) {
        JSONObject dataJson = null;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + access_token;
            if (!BaseHelpUtils.isNullOrEmpty(departmentId)) {
                url += "&id=" + departmentId;
            }
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                dataJson = json;
            }
        } catch (Exception e) {
            log.error("获取微信部门信息列表失败", e);
        }

        return dataJson;
    }

    /**
     * **** 微信部门数据处理模块 结束 ****
     */
    /**
     * **** 微信用户数据处理模块 开始 ****
     */
    /**
     *
     * @Title: createUser
     * @Description: 创建微信用户 API_DOC: https://work.weixin.qq.com/api/doc#10018
     * @param @param params
     * @param @return
     * @param @throws Exception
     * @return boolean
     * @throws
     */
    public static boolean createUser(Map<String, Object> params) {
        boolean b = true;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token=" + access_token;
            String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {
                b = false;
            }
        } catch (Exception e) {
            log.error("创建微信用户失败", e);
        }
        return b;
    }

    /**
     *
     * @Title: updateUser
     * @Description: 更新微信账户 API_DOC: https://work.weixin.qq.com/api/doc#10020
     * @param @param params
     * @param @return
     * @return boolean
     * @throws
     */
    public static boolean updateUser(Map<String, Object> params) {
        boolean b = true;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/update?access_token=" + access_token;
            String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {
                b = false;
            }
        } catch (Exception e) {
            log.error("更新微信用户失败", e);
        }
        return b;
    }

    /**
     *
     * @Title: getUserByUserId
     * @Description: 根据UserId读取微信账号信息
     * @param @param userId
     * @param @return
     * @param @throws Exception
     * @return JSONObject
     * @throws
     */
    public static JSONObject getUserByUserId(String userId) {
        JSONObject dataJson = null;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=" + access_token + "&userid=" + userId;
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                dataJson = json;
            }
        } catch (Exception e) {
            log.error("获取微信用户信息失败", e);
        }

        return dataJson;
    }

    /**
     *
     * @Title: deleteUser
     * @Description: 删除微信成员
     * @param @param touser
     * @param @return
     * @param @throws Exception
     * @return boolean
     * @throws
     */
    public static boolean deleteUserById(String touser) {
        boolean b = true;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/delete?access_token=" + access_token + "&userid="
                    + touser;
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {//0删除成功
                b = false;
            }
        } catch (Exception e) {
            log.error("删除微信用户信息失败", e);
        }

        return b;
    }

    public static JSONObject getDpeartmentUsers(Long departmentId, boolean fetchChild) {
        JSONObject dataJson = null;
        try {
            String access_token = getTXLAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=" + access_token + "&department_id="
                    + departmentId + "&fetch_child=" + (fetchChild ? 1 : 0);
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                dataJson = json;
            }
        } catch (Exception e) {
            log.error("查询部门下用户信息失败", e);
        }

        return dataJson;
    }

    /****************************************** 群聊接口处理     开始 ************************/

    /**
     * 创建群聊会话
     * API_DOC : https://work.weixin.qq.com/api/doc#13288
     * @param accessToken
     * @param params
     * @return
     */
    public static String createGroupChat(String accessToken, Map<String, Object> params) {
        String chatid = null;
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/appchat/create?access_token=" + accessToken;
            String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                chatid = json.getString("chatid");
            }
        } catch (Exception e) {
            log.error("创建群聊会话失败", e);
        }
        return chatid;
    }

    /**
     * 修改群聊
     * API_DOC : https://work.weixin.qq.com/api/doc#13292
     * @param accessToken
     * @param params
     * @return
     */
    public static boolean updateGroupChat(String accessToken, Map<String, Object> params) {
        boolean b = false;
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/appchat/update?access_token=" + accessToken;
            String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                b = true;
            }
        } catch (Exception e) {
            log.error("修改群聊失败", e);
        }
        return b;
    }

    /**
     * 发送非保密群聊消息
     * @param accessToken
     * @param chatid
     * @param content
     * @return
     */
    public static boolean sendGroupChatTxtMsg(String accessToken, String chatid, String content) {
        return sendGroupChatTxtMsg(accessToken, chatid, content, 0);
    }

    /**
     * 发送群聊消息
     * @param accessToken
     * @param chatid
     * @param chatid
     * @param content
     * @param safe
     * @return
     */
    public static boolean sendGroupChatTxtMsg(String accessToken, String chatid, String content, int safe) {
        boolean flag = false;
        if(!BaseHelpUtils.isNullOrEmpty(chatid) && !BaseHelpUtils.isNullOrEmpty(content)) {
            Map<String, Object> params = new HashMap<>();
            params.put("chatid", chatid);
            params.put("msgtype", "text");
            params.put("safe", safe);
            Map<String, Object> contentParams = new HashMap<>();
            contentParams.put("content", content);
            params.put("text", contentParams);
            return sendGroupChatMsg(accessToken, params);
        }
        return flag;
    }

    /**
     * 发送群聊消息
     * @param accessToken
     * @param params
     * @return
     */
    public static boolean sendGroupChatMsg(String accessToken, Map<String, Object> params) {
        boolean b = false;
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/appchat/send?access_token=" + accessToken;
            String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                b = true;
            }
        } catch (Exception e) {
            log.error("发送群聊消息失败", e);
        }
        return b;
    }

    /**
     * 获取群聊信息
     * @param accessToken
     * @param chatid
     * @return
     */
    public static JSONObject getGroupChatInfo(String accessToken, String chatid) {
        JSONObject dataJson = null;
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/appchat/get?access_token=" + accessToken + "&chatid=" + chatid;
            String result = HttpClientUtils.get(url, "utf-8");
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if ("0".equals(errcode)) {
                dataJson = json;
            }
        } catch (Exception e) {
            log.error("获取群聊会话失败", e);
        }

        return dataJson;
    }
    /****************************************** 群聊接口处理     结束 ************************/


    /**
     * 企业微信读取打卡数据请求
     * @param accessToken
     * @param agentId
     * @param useridlist 需要获取打卡记录的用户列表
     * @param opencheckindatatype 打卡类型。1：上下班打卡；2：外出打卡；3：全部打卡
     * @param starttime 获取打卡记录的开始时间。Unix时间戳
     * @param endtime 获取打卡记录的结束时间。Unix时间戳
     *
     * 请求示例：
     * {
     * 	"opencheckindatatype": 3,
     * 	"starttime": 1492617600,
     * 	"endtime": 1492790400,
     * 	"useridlist": ["james","paul"]
     * }
     * @return
     */
    public static JSONArray sendCheckInMsg(String accessToken, Integer agentId, String[] useridlist, int opencheckindatatype, Long starttime, Long endtime) {
        try {
            if (BaseHelpUtils.isNullOrEmpty(useridlist) || BaseHelpUtils.isNullOrEmpty(starttime) || BaseHelpUtils.isNullOrEmpty(endtime)) {
                return null;
            }
            Map<String, Object> params = new HashMap<>();
            if(BaseHelpUtils.isNullOrEmpty(opencheckindatatype)) {
                opencheckindatatype = 3;
            }
            params.put("opencheckindatatype", opencheckindatatype);
            params.put("starttime", starttime);
            params.put("endtime", endtime);
            params.put("useridlist", useridlist);
            String result = HttpClientUtils.postParameters(
                    "https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckindata?access_token=" + accessToken, MapUtils.toJSON(params));
            JSONObject json = JSONObject.fromString(result);
            String errcode = json.getString("errcode");
            if (!"0".equals(errcode)) {
                return null;
            }
            JSONArray checkindataArr = JSONArray.fromString(json.getString("checkindata"));
            return checkindataArr;
        } catch (Exception e) {
            return null;
        }

    }

}
