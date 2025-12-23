package com.example.accounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

    private static final String CONTAINER_NAME = "accounting-postgres";
    private static final int MAX_WAIT_SECONDS = 30;

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
                System.out.println("[Application] DB コンテナが停止しています。起動します...");
                startContainer();
                waitForContainerHealthy();
                System.out.println("[Application] DB コンテナが正常に起動しました。");
            }
        } catch (Exception e) {
            System.err.println("[Application] DB コンテナの起動確認に失敗しました: " + e.getMessage());
            // 起動確認に失敗しても Spring Boot の起動は試行する
        }
    }

    /**
     * コンテナが起動しているか確認
     */
    private static boolean isContainerRunning() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "inspect", "-f", "{{.State.Running}}", CONTAINER_NAME);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            process.waitFor(5, TimeUnit.SECONDS);
            return "true".equals(output);
        }
    }

    /**
     * コンテナを起動
     */
    private static void startContainer() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("docker", "start", CONTAINER_NAME);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Docker] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("docker start failed with exit code: " + exitCode);
        }
    }

    /**
     * コンテナがヘルシーになるまで待機
     */
    private static void waitForContainerHealthy() throws Exception {
        System.out.println("[Application] DB コンテナのヘルスチェックを待機中...");

        for (int i = 0; i < MAX_WAIT_SECONDS; i++) {
            if (isContainerHealthy()) {
                return;
            }
            TimeUnit.SECONDS.sleep(1);
        }

        throw new RuntimeException("DB コンテナが " + MAX_WAIT_SECONDS + " 秒以内にヘルシーになりませんでした");
    }

    /**
     * コンテナがヘルシーか確認
     */
    private static boolean isContainerHealthy() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "inspect", "-f", "{{.State.Health.Status}}", CONTAINER_NAME);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            process.waitFor(5, TimeUnit.SECONDS);
            return "healthy".equals(output);
        }
    }
}
