package bean;

import java.io.File;
import java.util.List;

import constants.DrawPath;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-6 18:14
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class TuyaViewBean {
    public List<DrawPath> savePaths;
    public int currentColor;
    public int backgroundColor;
    public int currentSize;
    public int currentStyle;
    public String text;
    public File bitmapPath;
    public boolean isChangerBackground;

    //保存时的屏幕宽度
    public int saveScreenWidth;
}

