package im.actor.enterprise;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;

import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.util.Randoms;
import im.actor.sdk.view.avatar.AvatarPlaceholderDrawable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import static im.actor.utils.TextUtils.isValidEmail;

public class LogoActor extends Actor {

    public static final int DELAY = 400;
    public static final String TAG = "LogoActor";
    LogoCallBack callBack;
    int debounceId = 0;

    private void onInputDebounce(String text) {
        debounceId = Randoms.randomInt();
        self().send(new OnInputDebounce(text, debounceId), DELAY);
    }

    private void onInput(String text, int id) {
        if (id != debounceId) {
            Log.d(TAG, "debounce - skip");
            return;
        }
        Log.d(TAG, "debounce - ok");
        HashMap<String, String> params = new HashMap<>();

        if (isValidEmail(text)) {
            Log.d(TAG, "valid email, trying to get logo");

            params.put("domain_url", text.substring(text.indexOf("@") + 1, text.length()));

            String resp = performPostCall("https://run.blockspring.com/api_v2/blocks/get-company-logo-by-domain-with-clearbit?api_key=br_18010_96cc1235266219052243a68a355a35889114776b",
                    params);

            if (resp != null && !resp.isEmpty()) {
                try {
                    JSONObject jsonResp = new JSONObject(resp);
                    JSONObject logoContainer = jsonResp.getJSONObject("logo");
                    Log.d("LogoActor", "got logo for " + params.get("domain_url"));

                    String base64Logo = logoContainer.getString("data");
                    Drawable avatar;
                    if (base64Logo != null) {
                        byte[] decodedByte = Base64.decode(base64Logo, Base64.NO_WRAP);
                        BitmapDrawable b = new BitmapDrawable(BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length));
                        callBack.onDownloaded(b);
                        return;
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "no logo for " + params.get("domain_url"));
                    callBack.onDownloaded(null);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    callBack.onDownloaded(null);
                }

            }
        }
        callBack.onDownloaded(null);
    }

    public String performPostCall(String requestURL,
                                  HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private void addCallback(LogoCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnInput) {
            onInputDebounce(((OnInput) message).getInput());
        } else if (message instanceof AddCallback) {
            addCallback(((AddCallback) message).getCallBack());
        } else if (message instanceof OnInputDebounce) {
            onInput(((OnInputDebounce) message).getInput(), ((OnInputDebounce) message).getId());
        }
    }

    public static class OnInput {
        String input;

        public OnInput(String input) {
            this.input = input;
        }

        public String getInput() {
            return input;
        }
    }

    public static class OnInputDebounce {
        String input;
        int id;

        public OnInputDebounce(String input, int id) {
            this.input = input;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String getInput() {
            return input;
        }
    }

    public static class AddCallback {
        LogoCallBack callBack;

        public AddCallback(LogoCallBack callBack) {
            this.callBack = callBack;
        }

        public LogoCallBack getCallBack() {
            return callBack;
        }
    }

    public interface LogoCallBack {
        void onDownloaded(Drawable drawable);
    }
}
