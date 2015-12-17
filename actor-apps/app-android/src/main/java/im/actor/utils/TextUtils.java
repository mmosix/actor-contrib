package im.actor.utils;

public class TextUtils {
    public final static boolean isValidEmail(CharSequence target) {
        return !android.text.TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}
