package plus.gaga.middleware.sdk.infrastructure.git;

// 导入Git类，用于执行Git操作
import org.eclipse.jgit.api.Git;
// 导入git异常类
import org.eclipse.jgit.api.errors.GitAPIException;
// 导入git凭证类，用于身份验证
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
// 导入日志记录器
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 导入随机字符串生成工具类
import plus.gaga.middleware.sdk.types.utils.RandomStringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// 这个类是Git操作的工具类，提供了一些常用的Git操作方法
public class GitCommand {

    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    private final String githubReviewLogUri;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public GitCommand(String githubReviewLogUri, String githubToken, String project, String branch, String author, String message) {
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    // 获取最近一次提交的代码差异
    public String diff() throws IOException, InterruptedException {
        // openai.itedus.cn
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));
        // 启动进程
        Process logProcess = logProcessBuilder.start();

        // 创建一个BufferedReader对象，用于读取进程的输出
        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        // 读取最近一次提交的哈希值
        String latestCommitHash = logReader.readLine();
        logReader.close();
        // 等待进程执行完成
        logProcess.waitFor();

        // 获取最近一次提交的代码差异
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        // 设置命令执行目录为当前目录
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        // 创建一个StringBuilder对象，用于存储代码差异
        StringBuilder diffCode = new StringBuilder();
        // 创建一个BufferedReader对象，用于读取进程的输出
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        // 读取代码差异
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        // 等待进程执行完成
        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to get diff, exit code:" + exitCode);
        }

        return diffCode.toString();
    }

    // 提交代码并推送到远程仓库
    public String commitAndPush(String recommend) throws Exception {
        // 使用JGit克隆代码评审日志仓库
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri + ".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                // 执行克隆操作
                .call();

        // 创建分支
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = project + "-" + branch + "-" + author + System.currentTimeMillis() + "-" + RandomStringUtils.randomNumeric(4) + ".md";
        File newFile = new File(dateFolder, fileName);
        // 使用try-with-resources语句创建一个FileWriter对象，用于写入文件内容
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(recommend);
        }

        // 提交内容
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("add code review new file" + fileName).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();

        // 记录和提交代码评审日志
        logger.info("openai-code-review git commit and push done! {}", fileName);

        // 返回代码评审日志的URL
        return githubReviewLogUri + "/blob/master/" + dateFolderName + "/" + fileName;
    }

    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }
}
