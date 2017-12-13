package constants;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-8 9:28
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class DrawPath {
    public Path path;// 路径
    public Paint paint;// 画笔
    public Path pathUp;// 上面的路径
    public Paint paintUp;// 上面的画笔
    public Path pathDown;// 下面的路径路径
    public Paint paintDown;// 下面的画笔
    public int rotate = 0;
    public float circleX;
    public float circleY;
    public float circleRadius;
    public Rect rect;

    public int DRAW_TYPE = 0;
    public final int DRAW_TYPE_NORMAL = 0;
    public final int DRAW_TYPE_LINE = 1;
    public final int DRAW_TYPE_CIRCLE = 2;
    public final int DRAW_TYPE_RECT = 3;


}
