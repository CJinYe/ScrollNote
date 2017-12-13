package constants;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-10 13:58
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class Constants {
    public final static int START = 10000;
    public final static int HANDLING = 10001;
    public final static int COMPLETED = 10002;
    public final static int ERROR = 10003;

    public final static String PERCENT = "PERCENT";
    public final static String ERROR_COM = "ERROR";

    //是否开启批注功能的key
    public final static String ON_OFF_POSTIL = "ON_OFF_POSTIL";

    //会议地点
    public final static String MEETING_ADDR = "MEETING_ADDR";
    public final static String MEETING_ADDR_NORMAL = "金龙锋";

    //分享的URL
    public final static String SHARE_URL = "http://icoxtech.cn/1WeChatEnterprise/DrawShare/getDrawing.ashx";
    //找回密码的URL
    public final static String FIND_PWD_URL_1 = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx0688f75d53cbb9e1&redirect_uri=http%3a%2f%2fwww.icoxedu.cn%2f1WeChatEnterprise%2fDrawShare%2fFindePwd.aspx?bl=";
    public final static String FIND_PWD_URL_2 = "&response_type=code&scope=snsapi_userinfo#wechat_redirect";
}
