package constants;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-11-13 18:29
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NotePositConf {
    /**
     * 画线状态，双线的时候的距离
     */
    public static float DARW_LINE_TWO_DISTANCE = 15;

    /**
     * 画线状态，波浪线峰高
     */
    public static float DARW_LINE_WAVE_HEIGHT = 20;
    /**
     * 画线状态，波浪线跨度宽
     */
    public static float DARW_LINE_WAVE_WIDTH = 15;

    public static final int DRAW_TYPE_NORMAL = 0; //正常状态涂画
    public static final int DRAW_TYPE_LINE = 1;   //画线状态
    public static final int DRAW_TYPE_CIRCLE = 2; //画圆状态
    public static final int DRAW_TYPE_REC = 3;    //画矩形状态

    public static final int DRAW_LINE_TYPE_JUST = 0;      //直线
    public static final int DRAW_LINE_TYPE_JUST_TWO = 1;  //双直线
    public static final int DRAW_LINE_TYPE_EMPTY = 2;     //虚线
    public static final int DRAW_LINE_TYPE_WAVE = 3;      //波浪线
    public static final int DRAW_LINE_TYPE_WAVE_TWO = 4;  //双波浪线

    public static void initLineTwoDistance(int width) {
        DARW_LINE_TWO_DISTANCE = ((float) width / 1280) * 10;
    }
}
