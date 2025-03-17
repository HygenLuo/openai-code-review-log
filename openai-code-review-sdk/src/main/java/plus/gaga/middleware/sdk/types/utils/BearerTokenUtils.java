package plus.gaga.middleware.sdk.types.utils;

// jwt， 用于创建和签名 JWT令牌
import com.auth0.jwt.JWT;
// 算法，用于指定签名算法
import com.auth0.jwt.algorithms.Algorithm;
// 缓存服务，用于缓存生成的token
import com.google.common.cache.Cache;
// 缓存构建器, 用于配置缓存参数
import com.google.common.cache.CacheBuilder;

// java 标准字符集，用于字节编码
import java.nio.charset.StandardCharsets;
// java 时间工具类，用于获取当前时间
import java.util.Calendar;
// java 集合工具类，用于创建存储jwt的payload和header
import java.util.HashMap;
import java.util.Map;
// java 并发工具类，用于设置缓存过期时间
import java.util.concurrent.TimeUnit;

public class BearerTokenUtils {

    // 过期时间；默认30分钟
    private static final long expireMillis = 30 * 60 * 1000L;

    // 缓存服务
    // 创建Token缓存，设置缓存项在写入后29分钟过期（比Token实际过期时间提前1分钟）
    public static Cache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(expireMillis - (60 * 1000L), TimeUnit.MILLISECONDS)
            .build();

    public static String getToken(String apiKeySecret) {
        String[] split = apiKeySecret.split("\\.");
        return getToken(split[0], split[1]);
    }

    /**
     * 对 ApiKey 进行签名
     *
     * @param apiKey    登录创建 ApiKey <a href="https://open.bigmodel.cn/usercenter/apikeys">apikeys</a>
     * @param apiSecret apiKey的后半部分 828902ec516c45307619708d3e780ae1.w5eKiLvhnLP8MtIf 取 w5eKiLvhnLP8MtIf 使用
     * @return Token
     */
    public static String getToken(String apiKey, String apiSecret) {
        // 缓存Token
        String token = cache.getIfPresent(apiKey);
        if (null != token) return token;
        // 创建Token
        Algorithm algorithm = Algorithm.HMAC256(apiSecret.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> payload = new HashMap<>();
        payload.put("api_key", apiKey);
        payload.put("exp", System.currentTimeMillis() + expireMillis);
        payload.put("timestamp", Calendar.getInstance().getTimeInMillis());
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("alg", "HS256");
        headerClaims.put("sign_type", "SIGN");
        token = JWT.create().withPayload(payload).withHeader(headerClaims).sign(algorithm);
        cache.put(apiKey, token);
        return token;
    }

}
