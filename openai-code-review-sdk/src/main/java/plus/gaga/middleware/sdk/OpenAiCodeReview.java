package plus.gaga.middleware.sdk;

// 日志相关
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plus.gaga.middleware.sdk.domain.service.IOpenAiCodeReviewService;

// 领域服务实现类
import plus.gaga.middleware.sdk.domain.service.impl.OpenAiCodeReviewService;
// git命令工具类
import plus.gaga.middleware.sdk.infrastructure.git.GitCommand;
// openai 接口
import plus.gaga.middleware.sdk.infrastructure.openai.IOpenAI;
// chatGLM 实现类
import plus.gaga.middleware.sdk.infrastructure.openai.impl.ChatGLM;
// 微信服务类
import plus.gaga.middleware.sdk.infrastructure.weixin.WeiXin;

// 这个类是整个代码评审系统的入口类，负责初始化各个组件并启动代码评审流程
public class OpenAiCodeReview {

    // 创建日志记录器，用于记录程序运行信息
    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    // 配置配置
    private String weixin_appid = "wx5a228ff69e28a91f"; // 微信公众号appid
    private String weixin_secret = "0bea03aa1310bac050aae79dd8703928"; // 微信公众号secret
    private String weixin_touser = "or0Ab6ivwmypESVp_bYuk92T6SvU"; // 接收信息的用户OpenID
    private String weixin_template_id = "l2HTkntHB71R4NQTW77UkcqvSOIFqE_bss1DAVQSybc"; // 发送的模板ID

    // ChatGLM 配置
    private String chatglm_apiHost = "https://open.bigmodel.cn/api/paas/v4/chat/completions"; // ChatGLM API地址
    private String chatglm_apiKeySecret = ""; // ChatGLM API密钥， 这里为空，实际使用时从环境变量获取

    // Github 配置
    private String github_review_log_uri; // github 日志地址
    private String github_token; // GitHub 访问令牌

    // 工程配置 - 自动获取
    private String github_project; // github 项目名称
    private String github_branch; // github 分支名称
    private String github_author; // github 提交作者

    public static void main(String[] args) throws Exception {
        // 创建日志记录器，用于记录程序运行信息
        // 这些参数都是从环境变量中获取的
        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        /**
         * 项目：{{repo_name.DATA}} 分支：{{branch_name.DATA}} 作者：{{commit_author.DATA}} 说明：{{commit_message.DATA}}
         */
        // 创建WeiXin对象，用于发送微信消息
        WeiXin weiXin = new WeiXin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );


        // 创建ChatGLM对象，用于调用ChatGLM API
        IOpenAI openAI = new ChatGLM(getEnv("CHATGLM_APIHOST"), getEnv("CHATGLM_APIKEYSECRET"));

        // 创建OpenAiCodeReviewService对象，用于执行代码评审流程
        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, openAI, weiXin);
        // 执行代码评审流程
        openAiCodeReviewService.exec();
        // 记录程序运行信息
        logger.info("openai-code-review done!");
    }

    // 从环境变量中获取参数
    private static String getEnv(String key) {
        // 从环境变量中获取参数
        String value = System.getenv(key);
        // 如果参数不存在或为空，则抛出异常
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("value is null");
        }
        // 返回环境变量的值
        return value;
    }

}
