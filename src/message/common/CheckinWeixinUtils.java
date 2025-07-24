package message.common;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import message.servlet.SystemInitConfig;
import org.apache.log4j.Logger;

import delicacy.common.BaseHelpUtils;
import delicacy.system.bean.BaseEmployee;
import delicacy.system.dao.Employee;
import message.util.HttpClientUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CheckinWeixinUtils {

	private static final Logger log = Logger.getLogger(CheckinWeixinUtils.class);

	public static Integer AGENTID;

	public static String CORP_SECRET;

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

	/**
	 *
	 * @param startTime 打卡记录的开始时间
	 * @param endTime 打卡记录的结束时间
	 * @param opencheckindatatype 打卡类型。1：上下班打卡；2：外出打卡；3：全部打卡
	 * @param args
	 * 注：1、获取记录时间跨度不超过一个月
	 * 注：2、用户列表不超过100个。若用户超过100个，请分批获取
	 * 注：3、有打卡记录即可获取打卡数据，与当前”打卡应用”是否开启无关
	 * @return
	 */
	public static JSONArray sendCheckInOfUser(Date startTime,Date endTime,int opencheckindatatype, java.lang.Object... args) {
		JSONArray checkindata = null;
		Object[] employeeIds = null;
		if (null != args && args.length > 0) {
			employeeIds = new Object[args.length];
			for (int i = 0; i < args.length; i++) {
				employeeIds[i] = BaseHelpUtils.getIntValue(args[i]);
			}
        }
		try {
			Employee dao = new Employee();
			dao.unsetSelectFlags();
			dao.setSelectEmployeeId(true);
			dao.setSelectCompanyWeixin(true);
			dao.setConditionEmployeeId(">",0);
			dao.setConditionStatus("=", 0);//只检索在职的人员
			dao.setConditionIsCheck("=",Boolean.TRUE);//只检索参与考勤的人
			//如果传参数过来，则不考虑分页，否则设置分页
			if(employeeIds != null && employeeIds.length > 0) {
				dao.addCondition(BaseEmployee.CS_EMPLOYEE_ID, "in", employeeIds);
			}
//			dao.setConditionEmployeeId("=", 2908);
//			String sql = "employee_id in (2908,2947) ";
			List<BaseEmployee> list = dao.conditionalLoad();
			int size = list.size();
			int pageLines = 100;
			if(size > 100) {
				int page = size/pageLines;
				int leftNum = size%pageLines;
				if(leftNum > 0) {
					page++;
				}
				for(int i = 1;i<=page;i++) {
					list = null;
					dao.setCurrentPage(i);
					dao.setPageLines(pageLines);
					list = dao.conditionalLoad("order by employee_id");
					checkindata = sendCheckInCommon(startTime, endTime, opencheckindatatype, list);
				}
			}else {
				checkindata = sendCheckInCommon(startTime, endTime, opencheckindatatype, list);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return checkindata;
	}

	/**
	 * 公用方法
	 * @param startTime
	 * @param endTime
	 * @param opencheckindatatype
	 * @param list
	 * @return
	 */
	public static JSONArray sendCheckInCommon(Date startTime,Date endTime,int opencheckindatatype,List<BaseEmployee> list) {
		JSONArray checkindata = null;
		if (!BaseHelpUtils.isNullOrEmpty(list) && list.size() > 0) {
			int size = list.size();
			String[] useridlist = new String[size];
			for(int i = 0;i<size;i++) {
				BaseEmployee baseEmployee = list.get(i);
				//获取员工的企业微信id
				String companyWeixin = BaseHelpUtils.getString(baseEmployee.getCompanyWeixin());
				if (!BaseHelpUtils.isNullOrEmpty(companyWeixin)) {
					useridlist[i] = companyWeixin;
				}
			}
			Long starttime = null;
			Long endtime = null;
			Calendar ca = Calendar.getInstance();
			//如果开始时间为空，则设置当天0点为开始时间
			if(BaseHelpUtils.isNullOrEmpty(startTime)) {
				ca.set(Calendar.HOUR_OF_DAY,0);
				ca.set(Calendar.MINUTE,0);
				ca.set(Calendar.SECOND,0);
				startTime = ca.getTime();
			}
			//如果结束时间为空，则设置当天23点59分为开始时间
			if(BaseHelpUtils.isNullOrEmpty(endTime)) {
				ca.set(Calendar.HOUR_OF_DAY,23);
				ca.set(Calendar.MINUTE,59);
				ca.set(Calendar.SECOND,0);
				endTime = ca.getTime();
			}
			starttime = toUnixTimeStamp(startTime);
			endtime = toUnixTimeStamp(endTime);
			checkindata = WeixinUtils.sendCheckInMsg(getAccessToken(), AGENTID, useridlist, opencheckindatatype, starttime, endtime);
		}
		return checkindata;
	}

	/**
     * 时间戳转Unix时间戳
     * 时间戳是以毫秒为单位，而Unix时间戳是以秒为单位
     * @param time
     * @return
     */
    public static long toUnixTimeStamp(Date time){
    	Long timestamp = time.getTime();
        return timestamp/1000;
    }

	public static void main(String[] args) throws SQLException {
		CheckinWeixinUtils checkinWeixinUtils = new CheckinWeixinUtils();
		SystemInitConfig.onLoadSystemConfigInfo();
		checkinWeixinUtils.sendCheckInOfUser(null, null, 1, null);
	}

}
