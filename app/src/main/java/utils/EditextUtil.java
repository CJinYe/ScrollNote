package utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-5-9 15:18
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class EditextUtil {
    /**
     * 禁止EditText输入空格
     *
     * @param editText
     */
    public static void setEditTextInhibitInputSpace(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(" ")) {
                    return "";
                } else {
                    return null;
                }

            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    /**
     * 禁止EditText输入特殊字符
     *
     * @param editText
     */
    public static void setEditTextInhibitInputSpeChat(int length, EditText editText) {

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？ 　]";
                Pattern pattern = Pattern.compile(speChat);
                Matcher matcher = pattern.matcher(source.toString());
                Pattern patternEmoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                        Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
                Matcher matcherEmoji = patternEmoji.matcher(source.toString());
                if (matcher.find() || matcherEmoji.find())
                    return "";
                else
                    return null;
            }
        };
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length), filter});
    }

    /**
     * 规定EditText的长度
     *
     * @param editText
     */
    public static void setEditTextLenght(int lenght, EditText editText) {

        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(lenght)});
    }
}
