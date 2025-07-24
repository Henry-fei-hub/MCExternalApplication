package message.servlet;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import message.common.*;
import org.apache.log4j.Logger;

import com.pomplatform.db.bean.BaseSystemConfig;
import com.pomplatform.db.dao.SystemConfig;

import delicacy.common.BaseHelpUtils;
import message.util.StaticUtils;

@WebServlet(name = "ExternalServlet", loadOnStartup = 1, urlPatterns = {"/ExternalServlet"})
public class SystemInitConfig extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	private final static Logger LOGGER = Logger.getLogger(SystemInitConfig.class);

	@Override
	public void init() {
		try {
			LOGGER.info("开始设置第三方应用(企业微信、企业邮箱、短信)配置数据");
			onLoadSystemConfigInfo();
			LOGGER.info("设置第三方应用(企业微信、企业邮箱、短信)配置数据成功");
		} catch (Exception e) {
			LOGGER.error("设置第三方应用(企业微信、企业邮箱、短信)配置数据异常");
		}
	}

	public static void onLoadSystemConfigInfo() throws SQLException{
    	//加载配置表数据集
    	SystemConfig dao = new SystemConfig();
    	dao.setConditionConfigType(">",0);
    	List<BaseSystemConfig> resultList = dao.conditionalLoad(" order by config_type ");
    	if(!BaseHelpUtils.isNullOrEmpty(resultList) && resultList.size() > 0){
    		for(BaseSystemConfig e : resultList){
    			int configType = BaseHelpUtils.getIntValue(e.getConfigType());
    			if(configType == StaticUtils.CONFIG_TYPE_1){
    				WeixinUtils.CORP_ID = BaseHelpUtils.getString(e.getCorpId());
    				ERPWeixinUtils.AGENTID = BaseHelpUtils.getIntValue(e.getAgentId());
    				ERPWeixinUtils.CORP_SECRET = BaseHelpUtils.getString(e.getCorpSecret());
    				WeixinUtils.TXL_CORP_SECRET = BaseHelpUtils.getString(e.getTxlCorpSecret());
    				WeixinUtils.TEST_USERID = BaseHelpUtils.getString(e.getCheckUserId());


    			}else if(configType == StaticUtils.CONFIG_TYPE_2){
    				TencentEmailUtils.CORP_ID = BaseHelpUtils.getString(e.getCorpId());
    				TencentEmailUtils.CORP_SECRET = BaseHelpUtils.getString(e.getCorpSecret());
    				TencentEmailUtils.TEST_USERID = BaseHelpUtils.getString(e.getCheckUserId());
    			}else if(configType == StaticUtils.CONFIG_TYPE_3){
    				SendEmailTemplate.hostName = BaseHelpUtils.getString(e.getHostName());
    				SendEmailTemplate.fromEmail = BaseHelpUtils.getString(e.getFromEmail());
    				SendEmailTemplate.password = BaseHelpUtils.getString(e.getPassword());
    				SendEmailTemplate.fromTitleCn = BaseHelpUtils.getString(e.getFromTitle());
    			}else if(configType == StaticUtils.CONFIG_TYPE_5) {
    				StaticUtils.configMap.put(configType, e);
				} else if (configType == StaticUtils.CONFIG_TYPE_13) {
					CheckinWeixinUtils.AGENTID = BaseHelpUtils.getIntValue(e.getAgentId());
					CheckinWeixinUtils.CORP_SECRET = BaseHelpUtils.getString(e.getCorpSecret());
				}
    		}
    	}
    }
}
