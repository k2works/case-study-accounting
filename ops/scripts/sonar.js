'use strict';

import { execSync } from 'child_process';
import path from 'path';

/**
 * @type {boolean} Windows環境かどうかをチェック
 */
const isWindows = process.platform === 'win32';

/**
 * @type {boolean} macOS環境かどうかをチェック
 */
const isMac = process.platform === 'darwin';

/**
 * SonarQube関連のGulpタスクを登録する
 * @param {import('gulp').Gulp} gulp
 */
export default function (gulp) {
    const SONAR_URL = 'http://localhost:9000';
    const BACKEND_DIR = path.join(process.cwd(), 'apps', 'backend');
    const FRONTEND_DIR = path.join(process.cwd(), 'apps', 'frontend');

    /**
     * SonarQube サービスを起動する
     */
    gulp.task('sonar:start', (done) => {
        try {
            console.log('Starting SonarQube services using Docker Compose...');

            // Normalize DOCKER_HOST on Windows if it's incorrect
            const env = { ...process.env };
            if (isWindows && env.DOCKER_HOST === 'npipe://./pipe/docker_engine') {
                env.DOCKER_HOST = 'npipe:////./pipe/docker_engine';
            }

            execSync('docker compose up -d sonarqube-db sonarqube', { stdio: 'inherit', env });

            console.log('\nSonarQube services started successfully!');
            console.log(`SonarQube dashboard is available at ${SONAR_URL}`);
            console.log('Note: Initial startup may take a few minutes.');

            done();
        } catch (error) {
            console.error('Error starting SonarQube services:', error.message);
            done(error);
        }
    });

    /**
     * SonarQube サービスを停止する
     */
    gulp.task('sonar:stop', (done) => {
        try {
            console.log('Stopping SonarQube services...');

            // Normalize DOCKER_HOST on Windows if it's incorrect
            const env = { ...process.env };
            if (isWindows && env.DOCKER_HOST === 'npipe://./pipe/docker_engine') {
                env.DOCKER_HOST = 'npipe:////./pipe/docker_engine';
            }

            execSync('docker compose stop sonarqube sonarqube-db', { stdio: 'inherit', env });

            console.log('SonarQube services stopped successfully!');

            done();
        } catch (error) {
            console.error('Error stopping SonarQube services:', error.message);
            done(error);
        }
    });

    /**
     * SonarQube ダッシュボードをブラウザで開く
     */
    gulp.task('sonar:open', (done) => {
        try {
            console.log('Opening SonarQube dashboard...');

            const openCmd = isWindows ? 'start' : isMac ? 'open' : 'xdg-open';
            const command = `${openCmd} ${SONAR_URL}`;
            execSync(command, { stdio: 'inherit' });

            console.log('SonarQube dashboard opened successfully!');

            done();
        } catch (error) {
            console.error('Error opening SonarQube dashboard:', error.message);
            done(error);
        }
    });

    /**
     * バックエンドのSonarQube解析を実行する
     */
    gulp.task('sonar:analyze:backend', (done) => {
        try {
            console.log('Running SonarQube analysis for backend...');
            console.log(`Working directory: ${BACKEND_DIR}`);

            // Gradle を使用して SonarQube 解析を実行
            const gradleCommand = isWindows ? 'gradlew.bat' : './gradlew';
            execSync(`${gradleCommand} sonar`, {
                stdio: 'inherit',
                cwd: BACKEND_DIR
            });

            console.log('\nBackend SonarQube analysis completed successfully!');
            console.log(`View results at ${SONAR_URL}`);

            done();
        } catch (error) {
            console.error('Error running backend SonarQube analysis:', error.message);
            done(error);
        }
    });

    /**
     * フロントエンドのSonarQube解析を実行する
     */
    gulp.task('sonar:analyze:frontend', (done) => {
        try {
            console.log('Running SonarQube analysis for frontend...');
            console.log(`Working directory: ${FRONTEND_DIR}`);

            // npm を使用して SonarQube 解析を実行
            const npmCommand = isWindows ? 'npm.cmd' : 'npm';
            execSync(`${npmCommand} run sonar`, {
                stdio: 'inherit',
                cwd: FRONTEND_DIR
            });

            console.log('\nFrontend SonarQube analysis completed successfully!');
            console.log(`View results at ${SONAR_URL}`);

            done();
        } catch (error) {
            console.error('Error running frontend SonarQube analysis:', error.message);
            done(error);
        }
    });

    /**
     * 全プロジェクトのSonarQube解析を実行する
     */
    gulp.task('sonar:analyze', gulp.series('sonar:analyze:backend', 'sonar:analyze:frontend'));

    /**
     * SonarQube サービスの状態を確認する
     */
    gulp.task('sonar:status', (done) => {
        try {
            console.log('Checking SonarQube service status...');

            // Normalize DOCKER_HOST on Windows if it's incorrect
            const env = { ...process.env };
            if (isWindows && env.DOCKER_HOST === 'npipe://./pipe/docker_engine') {
                env.DOCKER_HOST = 'npipe:////./pipe/docker_engine';
            }

            execSync('docker compose ps sonarqube sonarqube-db', { stdio: 'inherit', env });

            done();
        } catch (error) {
            console.error('Error checking SonarQube status:', error.message);
            done(error);
        }
    });

    /**
     * SonarQube のログを表示する
     */
    gulp.task('sonar:logs', (done) => {
        try {
            console.log('Showing SonarQube logs...');

            // Normalize DOCKER_HOST on Windows if it's incorrect
            const env = { ...process.env };
            if (isWindows && env.DOCKER_HOST === 'npipe://./pipe/docker_engine') {
                env.DOCKER_HOST = 'npipe:////./pipe/docker_engine';
            }

            execSync('docker compose logs -f sonarqube', { stdio: 'inherit', env });

            done();
        } catch (error) {
            console.error('Error showing SonarQube logs:', error.message);
            done(error);
        }
    });
}
