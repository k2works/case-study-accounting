package com.example.accounting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final String CONTAINER_NAME = "accounting-postgres";
    private static final int MAX_WAIT_SECONDS = 30;
    private static final String DOCKER_PATH = resolveDockerPath();
    private static final String DOCKER_COMPOSE_PATH = resolveDockerComposePath();

    public static void main(String[] args) {
        ensureDatabaseContainerRunning();
        SpringApplication.run(Application.class, args);
    }

    /**
     * DB コンテナが起動していない場合は自動起動する
     */
    @SuppressWarnings("PMD.DoNotUseThreads") // InterruptedException 処理で Thread.currentThread().interrupt() は標準的
    private static void ensureDatabaseContainerRunning() {
        try {
            if (!containerExists()) {
                LOGGER.info("DB コンテナが存在しません。docker-compose で作成・起動します...");
                createAndStartContainer();
                waitForContainerHealthy();
                LOGGER.info("DB コンテナが正常に作成・起動しました。");
                return;
            }

            if (!isContainerRunning()) {
                LOGGER.info("DB コンテナが停止しています。起動します...");
                startContainer();
                waitForContainerHealthy();
                LOGGER.info("DB コンテナが正常に起動しました。");
            }
        } catch (InterruptedException e) {
            LOGGER.error("DB コンテナの起動確認が中断されました: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            LOGGER.error("DB コンテナの起動確認に失敗しました: {}", e.getMessage());
            // 起動確認に失敗しても Spring Boot の起動は試行する
        }
    }

    /**
     * Docker コマンドの絶対パスを解決する
     * 環境変数 DOCKER_PATH が設定されている場合はそれを使用し、
     * なければ一般的なインストールパスから検索する
     */
    private static String resolveDockerPath() {
        String envPath = System.getenv("DOCKER_PATH");
        if (envPath != null && Files.isExecutable(Path.of(envPath))) {
            return envPath;
        }

        String[] candidates = {
            "/usr/bin/docker",
            "/usr/local/bin/docker",
            "C:\\Program Files\\Docker\\Docker\\resources\\bin\\docker.exe"
        };

        for (String candidate : candidates) {
            if (Files.isExecutable(Path.of(candidate))) {
                return candidate;
            }
        }

        LOGGER.warn("Docker の絶対パスが見つかりません。PATH から docker を使用します。");
        return "docker";
    }

    /**
     * docker-compose コマンドの絶対パスを解決する
     */
    private static String resolveDockerComposePath() {
        String envPath = System.getenv("DOCKER_COMPOSE_PATH");
        if (envPath != null && Files.isExecutable(Path.of(envPath))) {
            return envPath;
        }

        String[] candidates = {
            "/usr/bin/docker-compose",
            "/usr/local/bin/docker-compose",
            "C:\\Program Files\\Docker\\Docker\\resources\\bin\\docker-compose.exe"
        };

        for (String candidate : candidates) {
            if (Files.isExecutable(Path.of(candidate))) {
                return candidate;
            }
        }

        LOGGER.warn("docker-compose の絶対パスが見つかりません。PATH から docker-compose を使用します。");
        return "docker-compose";
    }

    /**
     * コンテナが存在するか確認
     */
    private static boolean containerExists() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(DOCKER_PATH, "ps", "-a", "-q", "-f", "name=" + CONTAINER_NAME);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String output = reader.readLine();
            process.waitFor(5, TimeUnit.SECONDS);
            return output != null && !output.isEmpty();
        }
    }

    /**
     * docker-compose でコンテナを作成・起動
     */
    private static void createAndStartContainer() throws IOException, InterruptedException {
        // docker-compose.yml はプロジェクトルートにあると想定
        // 実行時のカレントディレクトリが apps/backend の場合、2つ上の階層
        ProcessBuilder pb = new ProcessBuilder(DOCKER_COMPOSE_PATH, "up", "-d", "postgres");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            readProcessOutput(reader);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // docker-compose が失敗した場合、docker compose (V2) を試行
            ProcessBuilder pbV2 = new ProcessBuilder(DOCKER_PATH, "compose", "up", "-d", "postgres");
            pbV2.redirectErrorStream(true);
            Process processV2 = pbV2.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(processV2.getInputStream(), StandardCharsets.UTF_8))) {
                readProcessOutput(reader);
            }
            exitCode = processV2.waitFor();
        }

        if (exitCode != 0) {
            throw new IllegalStateException("docker-compose up failed with exit code: " + exitCode);
        }
    }

    /**
     * コンテナが起動しているか確認
     */
    private static boolean isContainerRunning() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                DOCKER_PATH, "inspect", "-f", "{{.State.Running}}", CONTAINER_NAME);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String output = reader.readLine();
            process.waitFor(5, TimeUnit.SECONDS);
            return "true".equals(output);
        }
    }

    /**
     * コンテナを起動
     */
    private static void startContainer() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(DOCKER_PATH, "start", CONTAINER_NAME);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            readProcessOutput(reader);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("docker start failed with exit code: " + exitCode);
        }
    }

    private static void readProcessOutput(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            LOGGER.debug("Docker output: {}", line);
            line = reader.readLine();
        }
    }

    /**
     * コンテナがヘルシーになるまで待機
     */
    private static void waitForContainerHealthy() throws IOException, InterruptedException {
        LOGGER.info("DB コンテナのヘルスチェックを待機中...");

        for (int i = 0; i < MAX_WAIT_SECONDS; i++) {
            if (isContainerHealthy()) {
                return;
            }
            TimeUnit.SECONDS.sleep(1);
        }

        throw new IllegalStateException("DB コンテナが " + MAX_WAIT_SECONDS + " 秒以内にヘルシーになりませんでした");
    }

    /**
     * コンテナがヘルシーか確認
     */
    private static boolean isContainerHealthy() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                DOCKER_PATH, "inspect", "-f", "{{.State.Health.Status}}", CONTAINER_NAME);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String output = reader.readLine();
            process.waitFor(5, TimeUnit.SECONDS);
            return "healthy".equals(output);
        }
    }
}
