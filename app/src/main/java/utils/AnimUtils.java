package utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import icox.com.scrawlnote.R;


/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-3-23 15:20
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class AnimUtils {
    public static Animation getBottomInAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.bottom_in);
        return  animation;
    }
    public static Animation getBottomOutAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.bottom_out);
        return  animation;
    }
    public static Animation getLeftOutAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.left_out);
        return  animation;
    }
    public static Animation getLeftInAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.left_in);
        return  animation;
    }
}
