package message.common;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;

import com.pomplatform.db.bean.BaseSystemConfig;

import delicacy.common.BaseHelpUtils;
import delicacy.system.dao.Employee;
import message.util.HttpClientUtils;
import message.util.StaticUtils;

public class OnTelMobile {

    /**
     * 手机短发发送信息通知
     *
     * @param employeeId
     * @param content
     * @throws Exception
     * @throws SocketTimeoutException
     * @throws ConnectTimeoutException
     */
    public static void onTelMobileSendMeg(int employeeId, String content)
            throws ConnectTimeoutException, SocketTimeoutException, Exception {
        // 加载职员信息
        Employee dao = new Employee();
        dao.setEmployeeId(employeeId);
        if (dao.load()) {
            // 获取该人员手机号
            String telMobile = dao.getMobile();
            if (!BaseHelpUtils.isNullOrEmpty(telMobile)) {
                onTelMobileSendMeg(telMobile, content);
            }
        }
    }

    /**
     * 根据发送短信给指定的手机号码
     *
     * @param phoneNumber
     * @param content
     * @throws Exception
     */
    public static void onTelMobileSendMeg(String phoneNumber, String content) throws Exception {
        if (!BaseHelpUtils.isNullOrEmpty(phoneNumber) && !BaseHelpUtils.isNullOrEmpty(content)) {
            try {
                BaseSystemConfig scBean = StaticUtils.configMap.get(StaticUtils.CONFIG_TYPE_5);
                if (!BaseHelpUtils.isNullOrEmpty(scBean)
                        && !BaseHelpUtils.isNullOrEmpty(scBean.getImagePath())
                        && !BaseHelpUtils.isNullOrEmpty(scBean.getUserId())
                        && !BaseHelpUtils.isNullOrEmpty(scBean.getAccount())
                        && !BaseHelpUtils.isNullOrEmpty(scBean.getPassword())
                        && !BaseHelpUtils.isNullOrEmpty(scBean.getContentTitle())) {
                    Map<String, String> params = new HashMap<>();
                    String url = BaseHelpUtils.getString(scBean.getImagePath());
                    params.put("UserID", scBean.getUserId());
                    params.put("Account", scBean.getAccount());
                    params.put("Password", scBean.getPassword());
                    params.put("Phones", phoneNumber + ";");
                    params.put("Content", content + scBean.getContentTitle());
                    params.put("SendTime", "");
                    params.put("SendType", "1");
                    params.put("PostFixNumber", "1");
                    String backResult = HttpClientUtils.postParameters(url, params);
                    System.out.println("backResult=========" + backResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
