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

    public static void main(String[] args) {
        ensureDatabaseContainerRunning();
        SpringApplication.run(Application.class, args);
    }

    /**
     * DB コンテナが起動していない場合は自動起動する
     */
    private static void ensureDatabaseContainerRunning() {
        try {
            if (!isContainerRunning()) {
                LOGGER.info("DB コンテナが停止しています。起動します...");
                startContainer();
                waitForContainerHealthy();
                LOGGER.info("DB コンテナが正常に起動しました。");
            }
        } catch (Exception e) {
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
