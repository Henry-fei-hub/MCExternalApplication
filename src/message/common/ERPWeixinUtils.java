package message.common;

import delicacy.common.BaseHelpUtils;
import delicacy.system.bean.BaseEmployee;
import delicacy.system.dao.Department;
import delicacy.system.dao.Employee;
import message.util.HttpClientUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ERPWeixinUtils {

	private static final Logger log = Logger.getLogger(ERPWeixinUtils.class);

	public static Integer AGENTID = 1000003;
//
	public static String CORP_SECRET = "IRQOM2xJfSPRHiLz7WyZRj2KVyambo44EqroLj_aaq4";

//	public static Integer AGENTID;

//	public static String CORP_SECRET = "";

	private static String ACCESS_TOKEN;
	
	/**
	 * 获取AccessToken
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
	
	public static void sendWXMsgToUser(int employeeId, String content) throws Exception {
		if (employeeId > 0 && !BaseHelpUtils.isNullOrEmpty(content)) {
            Employee dao = new Employee();
            dao.setEmployeeId(employeeId);
            if (dao.load()) {
                String touser = dao.getCompanyWeixin();
                log.info(" touser : "+touser);
                if (!BaseHelpUtils.isNullOrEmpty(touser)) {
                	WeixinUtils.sendTextMsgToUser(getAccessToken(), AGENTID, touser, content);
                }
            }
        }
	}

	public static void sendGraphicInformation(Map params) throws SQLException {
		if (BaseHelpUtils.isNullOrEmpty(params)) {
			throw new SQLException("数据异常：没有获取到需要发送的消息");
		}
		//获取发送的成员ID
		String touser = BaseHelpUtils.getString(params.get("touser"));
		if (BaseHelpUtils.isNullOrEmpty(touser)) {
			throw new SQLException("数据异常：没有获取到需要发送消息的人员");
		}
		//获取标题
		String title = BaseHelpUtils.getString(params.get("title"));
		if (BaseHelpUtils.isNullOrEmpty(title)) {
			throw new SQLException("数据异常：没有获取到需要发送消息的人员");
		}
		//获取描述
		String description = BaseHelpUtils.getString(params.get("description"));
		//获取跳转的url
		String url = BaseHelpUtils.getString(params.get("url"));
		if (BaseHelpUtils.isNullOrEmpty(url)) {
			throw new SQLException("数据异常：没有获取到url");
		}
		//获取图文连接
		String picurl = BaseHelpUtils.getString(params.get("picurl"));
		Map<String, Object> mapOne = new HashMap<>();
		mapOne.put("touser", touser);
		mapOne.put("msgtype", "news");
		mapOne.put("agentid", AGENTID);
		Map<String, Object> mapTwo = new HashMap<>();
		mapTwo.put("title", title);
		mapTwo.put("url", url);
		mapTwo.put("description", description);
		if (!BaseHelpUtils.isNullOrEmpty(picurl)) {
			mapTwo.put("picurl", picurl);
		}
		JSONObject jsonObject = JSONObject.fromMap(mapTwo);
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(0, jsonObject);
		Map<String, Object> paramB = new HashMap<>();
		paramB.put("articles", jsonArray);
		mapOne.put("news", paramB);
		WeixinUtils.sendGraphicMsg(getAccessToken(), mapOne);
	}
	
	
	/***    测试代码 开始     **/
	static String test1 = "10363345694992994956";
	
	static String  testChatid =  "14218426976022139539";
	public static boolean testUpdateGroup(String groupName) {
		Map<String, Object> params = new HashMap<>();
		params.put("chatid", testChatid);
		params.put("name", groupName);
		return WeixinUtils.updateGroupChat(ERPWeixinUtils.getAccessToken(), params);
	}

	public static String testCreateGroup() {
    	List<String> userList = new ArrayList<>();
    	userList.add(WeixinUtils.TEST_USERID);
    	userList.add("qy01138aa06869ac00282f45fdd2");
//    	userList.add("2135");
//    	userList.add("1848");
//    	userList.add("2134");
//    	userList.add("qy01418a1f6859ac0028add36466");
//    	userList.add("qy01698a4b684bac0028b63721e7");
//    	userList.add("qy015b8aac6803ac002800a52906");
    	Map<String, Object> params = new HashMap<>();
    	params.put("name", "企业微信群聊测试");
    	params.put("owner", WeixinUtils.TEST_USERID);
    	params.put("userlist", userList.toArray(new String[userList.size()] ));
    	return WeixinUtils.createGroupChat(ERPWeixinUtils.getAccessToken(), params);
	}
	
    public static void main(String[] args) throws Exception {
		ERPWeixinUtils weixinUtils =new ERPWeixinUtils();


//    	System.out.println(testCreateGroup());
//    	System.out.println(testUpdateGroup("测试微信群聊接口"));
//    	System.out.println(sendGroupChatTxtMsg(testChatid, "CWD项目组内消息通知是否可以改成这种方式?"));
//    	System.out.println(getGroupChatInfo(testChatid));
//		JSONObject json = getDpeartmentUsers(1L, true);
//		System.out.println(json);
//		JSONArray userList = json.getJSONArray("userlist");
//		Employee dao = new Employee();
//		for (int i = 0; i < userList.length(); i++) {
//			JSONObject userBean = userList.getJSONObject(i);
//			String wxUserId = userBean.getString("userid");
//			String headImg = userBean.getString("avatar");
//			String imgName = downloadPicture(headImg, wxUserId);
//			dao.setConditionCompanyWeixin("=", wxUserId);
//			dao.setPhoto("/images/photos/headimg/" + imgName);
//			dao.conditionalUpdate();
//		}
//		System.out.println(userList.length());
//		sendTextMsgToUser(test_userid, "haha ");
//		System.out.println(getUserIdByCode("L8wqRyIQzMAuADAyTHWNziiM_gtxJEozz9rW6qLpsho"));
//		System.out.println(getUserByUserId(test_userid + ""));
//		JSONObject job = getDepartmentList(null);
//		JSONArray jsa = job.getJSONArray("department");
//		for (int i = 0; i < jsa.length(); i++) {
//			System.out.println(jsa.get(i).toString());
//		}
//		System.out.println(jsa.length());

//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("userid", "1831");
//		params.put("name", "周涵");
//		params.put("email", "zhouhan@jaid.cn");
//		Object [] arr = new Object[]{1090012698};
//		params.put("department", arr);
//		params.put("mobile", "18702781778");
//		params.put("gender",  "2");
//		System.out.println(createUser(params));
//		System.out.println(getUserByUserId(TEST_USERID));
//		String aa = "yrS9DheulIXSsi_Fi_QrM25Psjl3HY89pGSSjul22mEc_WLB4zcNV4MvJ98b-QZvISIg2EhrVRZuRTJ4z7O2VA2wFos0lwx1_5kkm-axigEIiE1NDWs16H3SvNVLWhgpFBXMPKh6H69GoTYp9NSfSXjxnaqs9nDFCABZy3XE3gS_XNDWU2vWDKI20Bl63O82X7K58abLx80rDNdWiF6Ey42g6ezyM3D9jTKD2AGAmSBXewHBamxwl3vEzfmKvEL0v_iLSNTjczh8p9wrys9ElLUcWylkMkIt9ez0v6a5gqM";
//		System.out.println(checkTXLAccessToken(aa));
    }

    public static String downloadPicture(String urlImg, String wxId) {
        URL url = null;
        String imageName = "headimg_" + wxId + ".jpg";
        try {
            url = new URL(urlImg);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());

            FileOutputStream fileOutputStream = new FileOutputStream(new File("E://headimg//" + imageName));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageName;
    }
    
    public static void updateErpEmailDepartmentId() throws SQLException {
        JSONObject job = WeixinUtils.getDepartmentList(null);
        JSONArray jobArr = job.getJSONArray("department");
        Department dao = new Department();
        for (int i = 0; i < jobArr.length(); i++) {
            JSONObject tmpJob = jobArr.getJSONObject(i);
            String name = tmpJob.getString("name");
            Long id = tmpJob.getLong("id");
            dao.clear();
            dao.setConditionDepartmentName("=", name);
            if (null != dao.executeQueryOneRow()) {
                dao.setWeixinDepartmentId(id);
                dao.update();
            }
            System.out.println(tmpJob);
        }
    }
}
