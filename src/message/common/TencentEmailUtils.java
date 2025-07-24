package message.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.pomplatform.db.dao.Department;

import delicacy.common.BaseHelpUtils;
import delicacy.common.MapUtils;
import delicacy.system.dao.Employee;
import message.util.HttpClientUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @Title: TencentEmailUtils.java 
 * @Package delicacy.email 
 * @Description: API_DOC: https://exmail.qq.com/qy_mng_logic/doc
 * @author CL  
 * @date 2017年5月17日
 */
public class TencentEmailUtils {
	
	private static final Logger log = Logger.getLogger(HttpClientUtils.class);
	
	public static String CORP_ID = "wm5353aebb56b2b501";
	
	public static String CORP_SECRET = "dRO3Ez6_SSwV2YqTUCYw8JDx7HPnntgTDEMDBq8abJszcXymgpOFYGiQqxLrhU8F";
	
	public static String SYSTEM_LOG_CORP_SECRET = "clDbu2oj3gmbsQkvrPlp_5IBnmNyVK1KP0M37SCfZKs";
	
	private static String ACCESS_TOKEN;
	
	private static String SYSTEM_LOG_ACCESS_TOKEN;

	public static String TEST_USERID = "chenlei@jaid.cn";
	
	public final static Long TEST_DEPARTMENT_ID = 4681599755550737935L;//研发部
	
	public final static Long COMMON_ACCOUNT = 4681599755550741287L;//公用账号存在部门

	/**
	 * 
	 * @Title: getAccessToken 
	 * @Description: 获取AccessToken
	 * @param @return   
	 * @return String   
	 * @throws
	 */
	public static String getAccessToken() {
		try {
			String url = "https://api.exmail.qq.com/cgi-bin/gettoken?" + "corpid=" + CORP_ID + "&corpsecret="
					+ CORP_SECRET;

			if (!BaseHelpUtils.isNullOrEmpty(ACCESS_TOKEN)) {
				// 检验是否有效
				boolean b = checkAccessToken(ACCESS_TOKEN);
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
	
	public static String getSysteLogAccessToken() {
		try {
			String url = "https://api.exmail.qq.com/cgi-bin/gettoken?" + "corpid=" + CORP_ID + "&corpsecret="
					+ SYSTEM_LOG_CORP_SECRET;

			if (!BaseHelpUtils.isNullOrEmpty(SYSTEM_LOG_ACCESS_TOKEN)) {
				// 检验是否有效
				boolean b = checkAccessToken(SYSTEM_LOG_ACCESS_TOKEN);
				if (b) {
					return SYSTEM_LOG_ACCESS_TOKEN;
				} else {
					String result = HttpClientUtils.get(url, "utf-8");
					JSONObject json = JSONObject.fromString(result);
					SYSTEM_LOG_ACCESS_TOKEN = (String) json.get("access_token");
					// int expires_in = json.getInt("expires_in");
				}
			} else {// AccessToken已过期
				String result = HttpClientUtils.get(url, "utf-8");
				JSONObject json = JSONObject.fromString(result);
				SYSTEM_LOG_ACCESS_TOKEN = (String) json.get("access_token");
				// int expires_in = json.getInt("expires_in");
			}
		} catch (Exception e) {
			log.error("getAccessToken Exception", e);
		}
		return SYSTEM_LOG_ACCESS_TOKEN;
	}
	
	/**
	 * 
	 * @Title: checkAccessToken 
	 * @Description: 校验AccessToken有效性
	 * @param @param access_token
	 * @param @return   
	 * @return boolean   
	 * @throws
	 */
	public static boolean checkAccessToken(String access_token) {
		boolean b = true;
		try {
			String url = "https://api.exmail.qq.com/cgi-bin/user/get?access_token=" + access_token + "&userid="
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

	
	/****** 腾讯企业邮箱部门数据处理模块 开始 *****/
	/**
	 * 
	 * @Title: createDepartment 
	 * @Description: 创建腾讯企业邮箱部门    API_DOC: https://exmail.qq.com/qy_mng_logic/doc#10008
	 * @param @param params
	 * @param @return   
	 * @return Integer   
	 * @throws
	 */
	public static Long createDepartment(Map<String, Object> params){
		Long departmentId = null;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/department/create?access_token=" + access_token;
			String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if ("0".equals(errcode)) {
				departmentId = json.getLong("id");
			}
		} catch (Exception e) {
			log.error("创建腾讯企业邮箱部门失败", e);
		}
		return departmentId;
	}
	
	/**
	 * 
	 * @Title: deleteDepartmentById 
	 * @Description: 删除部门  API_DOC: https://exmail.qq.com/qy_mng_logic/doc#10010
	 * @param @param departmentId
	 * @param @return   
	 * @return boolean   
	 * @throws
	 */
	public static boolean deleteDepartmentById(Long departmentId){
		boolean b = true;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/department/delete?access_token=" + access_token + "&id="
					+ departmentId;
			String result = HttpClientUtils.get(url, "utf-8");
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if (!"0".equals(errcode)) {//0删除成功
				b = false;
			}
		} catch (Exception e) {
			log.error("删除腾讯企业邮箱部门信息失败", e);
		}
		return b;
	}
	
	/**
	 * 
	 * @Title: updateDepartment 
	 * @Description: 更新腾讯企业邮箱部门信息  API_DOC: https://exmail.qq.com/qy_mng_logic/doc#10009
	 * @param @param params
	 * @param @return   
	 * @return boolean   
	 * @throws
	 */
	public static boolean updateDepartment(Map<String, Object> params){
		boolean b = true;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/department/update?access_token=" + access_token;
			String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if (!"0".equals(errcode)) {
				b = false;
			}
		} catch (Exception e) {
			log.error("更新腾讯企业邮箱部门失败", e);
		}
		return b;
	}
	
	/**
	 * 
	 * @Title: getDepartmentList 
	 * @Description: 获取部门列表信息  API_DOC:https://exmail.qq.com/qy_mng_logic/doc#10011
	 * @param @param departmentId
	 * @param @return   
	 * @return JSONObject   
	 * @throws
	 */
	public static JSONObject getDepartmentList(Long departmentId){
		JSONObject dataJson = null;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/department/list?access_token=" + access_token ;
			if(!BaseHelpUtils.isNullOrEmpty(departmentId)){
				url += "&id=" + departmentId;
			}
			String result = HttpClientUtils.get(url, "utf-8");
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if ("0".equals(errcode)) {
				dataJson = json;
			}
		} catch (Exception e) {
			log.error("获取腾讯企业邮箱部门信息列表失败", e);
		}
		
		return dataJson;
	}
	
	/**
	 * 
	 * @Title: getDepartmentListByName 
	 * @Description: 
	 * @param @param departmentName
	 * @param @param fuzzy    1/0  是/否 模糊搜索
	 * @param @return   
	 * @return JSONObject   
	 * @throws
	 */
	public static JSONObject getDepartmentListByName(String departmentName, Integer fuzzy){
		JSONObject dataJson = null;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/department/search?access_token=" + access_token ;
			Map<String, Object> params = new HashMap<>();
			if(null != fuzzy){
				params.put("fuzzy", fuzzy);
			}
			params.put("name", departmentName);
			String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if ("0".equals(errcode)) {
				dataJson = json;
			}
		} catch (Exception e) {
			log.error("获取腾讯企业邮箱部门信息列表失败", e);
		}
		
		return dataJson;
	}
	
	
	/****** 腾讯企业邮箱部门数据处理模块 结束 *****/
	
	/****** 腾讯企业邮箱用户数据处理模块  开始 *****/
	
	/**
	 * 
	 * @Title: createUser 
	 * @Description: 创建腾讯企业邮箱用户     API_DOC: https://exmail.qq.com/qy_mng_logic/doc#10014
	 * @param @param params
	 * @param @return
	 * @param @throws Exception   
	 * @return boolean   
	 * @throws
	 */
	public static boolean createUser(Map<String, Object> params){
		boolean b = true;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/user/create?access_token=" + access_token;
			String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if (!"0".equals(errcode)) {
				b = false;
			}
		} catch (Exception e) {
			log.error("创建腾讯企业邮箱用户失败", e);
		}
		return b;
	}
	
	/**
	 * 
	 * @Title: updateUser 
	 * @Description: 更新腾讯企业邮箱账户  API_DOC: https://exmail.qq.com/qy_mng_logic/doc#10015
	 * @param @param params
	 * @param @return   
	 * @return boolean   
	 * @throws
	 */
	public static boolean updateUser(Map<String, Object> params){
		boolean b = true;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/user/update?access_token=" + access_token;
			String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if (!"0".equals(errcode)) {
				b = false;
			}
		} catch (Exception e) {
			log.error("更新腾讯企业邮箱用户失败", e);
		}
		return b;
	}
	
	/**
	 * 
	 * @Title: getUserByUserId 
	 * @Description: 根据UserId读取腾讯企业邮箱账号信息
	 * @param @param userId
	 * @param @return
	 * @param @throws Exception   
	 * @return JSONObject   
	 * @throws
	 */
	public static JSONObject getUserByUserId(String userId) {
		JSONObject dataJson = null;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/user/get?access_token=" + access_token + "&userid=" + userId;
			String result = HttpClientUtils.get(url, "utf-8");
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if ("0".equals(errcode)) {
				dataJson = json;
			}
		} catch (Exception e) {
			log.error("获取腾讯企业邮箱用户信息失败", e);
		}
		
		return dataJson;
	}
	
	/**
	 * 获取部门下用户 <p>fetchChild 1/0：是否递归获取子部门下面的成员</p>
	 * API_DOC : https://exmail.qq.com/qy_mng_logic/doc#10019
	 * 
	 * @param departmentId
	 * @param fetchChild
	 * @return
	 */
	public static JSONObject getDepartmentUserByDepartmentId(Long departmentId, Integer fetchChild){
		JSONObject dataJson = null;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/user/list?access_token=" + access_token + "&department_id=" + departmentId + "&fetch_child=" + fetchChild;
			String result = HttpClientUtils.get(url, "utf-8");
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if ("0".equals(errcode)) {
				dataJson = json;
			}
		} catch (Exception e) {
			log.error("获取腾讯企业邮箱部门用户信息失败", e);
		}
		return dataJson;
	}
	
	/**
	 * 
	 * @Title: deleteUser 
	 * @Description: 删除腾讯企业邮箱成员
	 * @param @param touser
	 * @param @return
	 * @param @throws Exception   
	 * @return boolean   
	 * @throws
	 */
	public static boolean deleteUserById(String touser) throws Exception{
		boolean b = true;
		try {
			String access_token = getAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/user/delete?access_token=" + access_token + "&userid="
					+ touser;
			String result = HttpClientUtils.get(url, "utf-8");
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if (!"0".equals(errcode)) {//0删除成功
				b = false;
			}
		} catch (Exception e) {
			log.error("删除腾讯企业邮箱用户信息失败", e);
		}
		
		return b;
	}
	
	/**
	 * 获取腾讯企业邮箱用户收发邮件历史
	 * API_DOC : https://exmail.qq.com/qy_mng_logic/doc#10028
	 * @param params
	 * @return
	 */
	public static JSONObject getUserEmailHistory(Map<String, Object> params) {
		JSONObject dataJson = null;
		try {
			String access_token = getSysteLogAccessToken();
			String url = "https://api.exmail.qq.com/cgi-bin/log/mail?access_token=" + access_token;
			String result = HttpClientUtils.postParameters(url, MapUtils.toJSON(params));
			JSONObject json = JSONObject.fromString(result);
			String errcode = json.getString("errcode");
			if ("0".equals(errcode)) {
				dataJson = json;
			}
		} catch (Exception e) {
			log.error("获取腾讯企业邮箱用户收发邮件历史失败", e);
		}
		
		return dataJson;
	}


	public static void writeEror_to_txt(String path,String logs) throws IOException {
		if(BaseHelpUtils.isNullOrEmpty(path) || BaseHelpUtils.isNullOrEmpty(logs)) {
			return;
		}
		Calendar ca = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = sdf.format(ca.getTime());
		System.out.println("path=============="+path);
		File F=new File(path);
		//如果文件不存在,就动态创建文件
		if(!F.exists()){
			F.createNewFile();
		}
		FileWriter fw=null;
		String writeDate=logs;
		try {
			//设置为:True,表示写入的时候追加数据
			fw=new FileWriter(F, true);
//			//回车并换行
			fw.write(writeDate);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(fw!=null){
				fw.close();
			}
		}

	}
	
	
	/****** 腾讯企业邮箱用户数据处理模块  结束 *****/

//	public static void main(String[] args) throws Exception {
//	    int ii = 0;
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("begin_date","2018-05-01");
//        map.put("end_date","2018-07-30");
//        map.put("mailtype",1);
//        map.put("userid","ranxf@jaid.cn");
//		PlateRecord plateRecordDao = new PlateRecord();
//		plateRecordDao.setConditionParentId("=",14);
//		List<BasePlateRecord> basePlateRecords = plateRecordDao.conditionalLoad();
//		for (BasePlateRecord basePlateRecord : basePlateRecords){
//			Employee employeeDao = new Employee();
//			employeeDao.setConditionPlateId("=",basePlateRecord.getPlateId());
//			List<BaseEmployee> baseEmployees = employeeDao.conditionalLoad();
//			for (BaseEmployee baseEmployee : baseEmployees){
//			    if (!BaseHelpUtils.isNullOrEmpty(baseEmployee.getCompanyEmail())){
//                    map.put("userid",baseEmployee.getCompanyEmail());
//                }else {
//			        continue;
//                }
//				JSONObject userEmailHistory = getUserEmailHistory(map);
//			    if (userEmailHistory != null) {
//                    JSONArray listArray = userEmailHistory.getJSONArray("list");
//                    if (listArray.length() > 0) {
//                        for (int i = 0; i < listArray.length(); i++) {
//                            JSONObject emailObj = listArray.getJSONObject(i);
//                            String sender = emailObj.getString("sender");
//                            Long time = emailObj.getLong("time");
//                            Date date = new Date(time);
//                            String receiver = emailObj.getString("receiver");
//                            String[] split = receiver.split("@");
//                            String s = split[1];
//                            if (!s.equals("jaid.cn")) {//
//                                Temp tempDao = new Temp();
//                                tempDao.setConditionEmail("=", receiver);
//                                BaseTemp baseTemp = tempDao.executeQueryOneRow();
//                                if (BaseHelpUtils.isNullOrEmpty(baseTemp)) {
//                                    tempDao.setEmail(receiver);
//                                    tempDao.save();
//                                    ii++;
//                                    String logs = receiver;
////                            String logs = "发送人邮箱：" + sender +" ," + "收件人邮箱: " + receiver + " ," + "发送时间:" + date;
//                                    if (ii % 10 == 0) {
//                                        logs = logs + "\r\n";
//                                    } else {
//                                        logs = logs + ",";
//                                    }
//                                    System.out.println(logs);
//                                    writeEror_to_txt("D:\\test\\test.txt", logs);
//                                }
////                            ii++;
////                            String logs = receiver;
//////                            String logs = "发送人邮箱：" + sender +" ," + "收件人邮箱: " + receiver + " ," + "发送时间:" + date;
////                            if (ii % 10 == 0){
////                                logs = logs + "\r\n";
////                            }else{
////                                logs = logs + ",";
////                            }
////                            System.out.println(logs);
////                            writeEror_to_txt("D:\\test\\test.txt",logs);
//                            }
//
//                        }
//                    }
//                }
//			}
//		}
//        System.out.println("结束==========================");
////		System.out.println("@@@@@@@@@@@@@@@@@@" + getUserEmailHistory(map));
//		
////		System.out.println(getUserIdByCode("L8wqRyIQzMAuADAyTHWNziiM_gtxJEozz9rW6qLpsho"));
////		System.out.println(getUserByUserId(TEST_USERID));
////		System.out.println(getDepartmentList(TEST_DEPARTMENT_ID));
////		
////		System.out.println(deleteUserById("test@jaid.cn"));
////		System.out.println(getUserByUserId("dl@jaid.cn"));
////		JSONObject job = getDepartmentList(1L);
////		System.out.println(job);
////		updateErpEmailDepartmentId();
//		
//		
////		Map<String, Object> params = new HashMap<>();
////		params.put("name", "Tencent部门接口测试2"); 
////		params.put("parentid", TEST_DEPARTMENT_ID);
////		System.out.println(createDepartment(params));
////		System.out.println(getDepartmentListByName("Tencent部门接口测试", 1));
//		//创建用户
////		Map<String, Object> params = new HashMap<String, Object>();
////		params.put("userid", "zhouhan@jaid.cn");
////		params.put("name", "周涵");
////		Object [] arr = new Object[]{4681599755550737946L};
////		params.put("department", arr);
////		params.put("mobile", "18702781778");
////		params.put("cpwd_login", 1);
////		params.put("gender",  "2");
////		params.put("password", "Jaid2017");
////		System.out.println(createUser(params));
////		System.out.println(MapUtils.toJSON(params));
////		System.out.println(updateUser(params));
////		System.out.println(deleteUserById("testtest@jaid.cn"));
//		//更新部门
////		Map<String, Object> params = new HashMap<String, Object>();
////		params.put("id", 4681599755550742264L);
////		params.put("parentid", 4681599755550742266L);
////		System.out.println(deleteDepartmentById(4681599755550742264L));
////		System.out.println(updateDepartment(params));
//	}
	
	/**
	 * 同步企业邮箱的座机号到ERP
	 * @throws SQLException
	 */
	public static void updateErpEmployeeTel() throws SQLException{
		JSONObject json = getDepartmentUserByDepartmentId(1L, 1);
		JSONArray jsArr = json.getJSONArray("userlist");
		Employee dao = new Employee();
		for (int i = 0; i < jsArr.length(); i++) {
			if(!BaseHelpUtils.isNullOrEmpty(jsArr.getJSONObject(i).getString("tel"))){
				dao.setConditionCompanyEmail("=", jsArr.getJSONObject(i).getString("userid"));
				dao.setPhone(jsArr.getJSONObject(i).getString("tel"));
				dao.conditionalUpdate();
			}
			
		}
	}
	
	public static void updateErpEmailDepartmentId() throws SQLException{
		JSONObject job = getDepartmentList(1L);
		System.out.println(job);
		JSONArray jsa = job.getJSONArray("department");
		Department dao = new Department();
		int ll = 0;
		for (int i = 0; i < jsa.length(); i++) {
			String name = jsa.getJSONObject(i).getString("name");
			Long id = jsa.getJSONObject(i).getLong("id");
			JSONObject __job = getDepartmentList(id);
			if(__job.getJSONArray("department").length() > 1){
				JSONArray __jsa = __job.getJSONArray("department");
				for (int j = 0; j < __jsa.length(); j++) {
					String __name = __jsa.getJSONObject(j).getString("name");
					Long __id = __jsa.getJSONObject(j).getLong("id");
					JSONObject ____job = getDepartmentList(__id);
					if(____job.getJSONArray("department").length() > 1){
						JSONArray ____jsa = ____job.getJSONArray("department");
						for (int k = 0; k < ____jsa.length(); k++) {
							String ____name = ____jsa.getJSONObject(k).getString("name");
							Long ____id = ____jsa.getJSONObject(k).getLong("id");
							dao.clear();
							dao.setConditionDepartmentName("=", ____name);
							if(null != dao.executeQueryOneRow()){
								ll ++;
								dao.setEmailDepartmentId(____id);
								dao.update();
							}
						}
					}else{
						dao.clear();
						dao.setConditionDepartmentName("=", __name);
						if(null != dao.executeQueryOneRow()){
							ll ++;
							dao.setEmailDepartmentId(__id);
							dao.update();
						}
					}
				}
			}else{
				dao.clear();
				dao.setConditionDepartmentName("=", name);
				if(null != dao.executeQueryOneRow()){
					ll ++;
					dao.setEmailDepartmentId(id);
					dao.update();
				}
			}
			
		}
		System.out.println(" allsize " + jsa.length() + " ------------" + ll);
	}
}
