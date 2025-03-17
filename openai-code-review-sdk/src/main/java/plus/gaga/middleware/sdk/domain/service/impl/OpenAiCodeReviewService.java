package plus.gaga.middleware.sdk.domain.service.impl;


import plus.gaga.middleware.sdk.domain.model.Model; // AI模型枚举
import plus.gaga.middleware.sdk.domain.service.AbstractOpenAiCodeReviewService; // 抽象父类
import plus.gaga.middleware.sdk.infrastructure.git.GitCommand; // Git命令工具类
import plus.gaga.middleware.sdk.infrastructure.openai.IOpenAI; // OpenAI接口
import plus.gaga.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO; // ChatCompletion请求DTO
import plus.gaga.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO; // ChatCompletion响应DTO
import plus.gaga.middleware.sdk.infrastructure.weixin.WeiXin; // 微信服务类
import plus.gaga.middleware.sdk.infrastructure.weixin.dto.TemplateMessageDTO; // 模板消息DTO

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenAiCodeReviewService 类实现了抽象类中定义的四个抽象方法，分别对应代码评审流程的四个步骤：
 * 获取代码差异、AI 评审代码、记录评审结果和发送通知。
 */

/**
 * @author ：wangbo
 * @date ：Created in 2023/3/17 11:21
 * @description：OpenAi代码评审服务
 * @modified By：
 * @version: $
 */

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        // 创建AI请求对象
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        // 设置AI模型
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        // 设置AI请求消息，包括用户和AI的对话内容
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            // 序列化ID， 用于ArrayList的序列化
            private static final long serialVersionUID = -7988151926241837899L;

            {
                // 添加系统提示，定义AI角色和内容
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一位资深编程专家，拥有深厚的编程基础和广泛的技术栈知识。你的专长在于识别代码中的低效模式、安全隐患、以及可维护性问题，并能提出针对性的优化策略。你擅长以易于理解的方式解释复杂的概念，确保即使是初学者也能跟随你的指导进行有效改进。在提供优化建议时，你注重平衡性能、可读性、安全性、逻辑错误、异常处理、边界条件，以及可维护性方面的考量，同时尊重原始代码的设计意图。\n" +
                        "你总是以鼓励和建设性的方式提出反馈，致力于提升团队的整体编程水平，详尽指导编程实践，雕琢每一行代码至臻完善。用户会将仓库代码分支修改代码给你，以git diff 字符串的形式提供，你需要根据变化的代码，帮忙review本段代码。然后你review内容的返回内容必须严格遵守下面我给你的格式，包括标题内容。\n" +
                        "模板中的变量内容解释：\n" +
                        "变量1是给review打分，分数区间为0~100分。\n" +
                        "变量2 是code review发现的问题点，包括：可能的性能瓶颈、逻辑缺陷、潜在问题、安全风险、命名规范、注释、以及代码结构、异常情况、边界条件、资源的分配与释放等等\n" +
                        "变量3是具体的优化修改建议。\n" +
                        "变量4是你给出的修改后的代码。 \n" +
                        "变量5是代码中的优点。\n" +
                        "变量6是代码的逻辑和目的，识别其在特定上下文中的作用和限制\n" +
                        "\n" +
                        "必须要求：\n" +
                        "1. 以精炼的语言、严厉的语气指出存在的问题。\n" +
                        "2. 你的反馈内容必须使用严谨的markdown格式\n" +
                        "3. 不要携带变量内容解释信息。\n" +
                        "4. 有清晰的标题结构\n" +
                        "返回格式严格如下：\n" +
                        "# 小傅哥项目： OpenAi 代码评审.\n" +
                        "### \uD83D\uDE00代码评分：{变量1}\n" +
                        "#### \uD83D\uDE00代码逻辑与目的：\n" +
                        "{变量6}\n" +
                        "#### ✅代码优点：\n" +
                        "{变量5}\n" +
                        "#### \uD83E\uDD14问题点：\n" +
                        "{变量2}\n" +
                        "#### \uD83C\uDFAF修改建议：\n" +
                        "{变量3}\n" +
                        "#### \uD83D\uDCBB修改后的代码：\n" +
                        "{变量4}\n" +
                        "`;代码如下:"));

                // 添加用户输入的代码差异
                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
            }
        });
        // 调用OpenAI接口，进行代码评审
        ChatCompletionSyncResponseDTO completions = openAI.completions(chatCompletionRequest);
        // 获取代码评审结果
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        // 返回代码评审结果
        return message.getContent();
    }

    @Override
    // 将代码评审结果记录到Git仓库中
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    // 发送微信消息通知
    protected void pushMessage(String logUrl) throws Exception {
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl, data);
    }

}
