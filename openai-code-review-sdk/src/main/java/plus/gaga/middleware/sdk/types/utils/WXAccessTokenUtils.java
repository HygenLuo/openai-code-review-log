package plus.gaga.middleware.sdk.types.utils;

// Fastjson2 库，用于解析 JSON 数据
import com.alibaba.fastjson2.JSON;

// java 标准库，用于处理 HTTP 请求和响应
import java.io.BufferedReader;
import java.io.InputStreamReader;
// java 网络库，用于发送 HTTP 请求
import java.net.HttpURLConnection;
import java.net.URL;

// 这个类是用于获取微信公众号的 access_token 的工具类
public class WXAccessTokenUtils {

    // 公众号的 appid 和 secret
    private static final String APPID = "wx7500895aaa3d7dbb";
    private static final String SECRET = "b4baaf7b02332c355176fb3b199a2f75";
    // 授权类型
    private static final String GRANT_TYPE = "client_credential";
    // 微信公众号获取 access_token 的模板
    private static final String URL_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&appid=%s&secret=%s";

    public static String getAccessToken() {
        // 调用重载方法，使用默认的 appid 和 secret
        return getAccessToken(APPID, SECRET);
    }

    // 指定 appid 和 secret 获取 access_token
    public static String getAccessToken(String APPID, String SECRET) {
        try {
            // 使用String.format()方法来格式化URL字符串，填入授权类型、appid和secret
            String urlString = String.format(URL_TEMPLATE, GRANT_TYPE, APPID, SECRET);
            // 创建一个URL对象
            URL url = new URL(urlString);
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法为GET
            connection.setRequestMethod("GET");

            // 获取响应码
            int responseCode = connection.getResponseCode();
            // Print the response code
            System.out.println("Response Code: " + responseCode);

            // 如果响应码为200，则读取响应内容
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Print the response
                System.out.println("Response: " + response.toString());

                Token token = JSON.parseObject(response.toString(), Token.class);

                return token.getAccess_token();
            } else {
                System.out.println("GET request failed");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Token {
        private String access_token;
        private Integer expires_in;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }
    }


}
