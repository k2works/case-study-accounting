'use strict';

import { execSync } from 'child_process';

/**
 * Docker Compose ビルド関連の Gulp タスクを登録する
 * @param {import('gulp').Gulp} gulp
 */
export default function (gulp) {
    const PROJECT_ROOT = process.cwd();

    // Docker Compose ファイル
    const COMPOSE_FILE = 'docker-compose.yml';
    const COMPOSE_DEMO_FILE = 'docker-compose-demo.yml';

    /**
     * 開発環境をビルドする
     */
    gulp.task('build:dev', (done) => {
        try {
            console.log('Building development environment...');
            console.log(`Using: ${COMPOSE_FILE}`);

            execSync(`docker compose -f ${COMPOSE_FILE} build`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            console.log('\nDevelopment environment built successfully!');

            done();
        } catch (error) {
            console.error('Error building development environment:', error.message);
            done(error);
        }
    });

    /**
     * デモ環境をビルドする
     */
    gulp.task('build:demo', (done) => {
        try {
            console.log('Building demo environment...');
            console.log(`Using: ${COMPOSE_DEMO_FILE}`);

            execSync(`docker compose -f ${COMPOSE_DEMO_FILE} build`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            console.log('\nDemo environment built successfully!');

            done();
        } catch (error) {
            console.error('Error building demo environment:', error.message);
            done(error);
        }
    });

    /**
     * 開発環境を起動する
     */
    gulp.task('build:up:dev', (done) => {
        try {
            console.log('Starting development environment...');

            execSync(`docker compose -f ${COMPOSE_FILE} up -d`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            console.log('\nDevelopment environment started!');
            console.log('Frontend: http://localhost:3001');
            console.log('Backend API: http://localhost:8081');
            console.log('Adminer: http://localhost:8888');

            done();
        } catch (error) {
            console.error('Error starting development environment:', error.message);
            done(error);
        }
    });

    /**
     * デモ環境を起動する
     */
    gulp.task('build:up:demo', (done) => {
        try {
            console.log('Starting demo environment...');

            execSync(`docker compose -f ${COMPOSE_DEMO_FILE} up -d`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            console.log('\nDemo environment started!');
            console.log('Frontend: http://localhost:3001');
            console.log('Backend API: http://localhost:8081');
            console.log('Swagger UI: http://localhost:8081/swagger-ui.html');

            done();
        } catch (error) {
            console.error('Error starting demo environment:', error.message);
            done(error);
        }
    });

    /**
     * 開発環境を停止する
     */
    gulp.task('build:down:dev', (done) => {
        try {
            console.log('Stopping development environment...');

            execSync(`docker compose -f ${COMPOSE_FILE} down`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            console.log('\nDevelopment environment stopped!');

            done();
        } catch (error) {
            console.error('Error stopping development environment:', error.message);
            done(error);
        }
    });

    /**
     * デモ環境を停止する
     */
    gulp.task('build:down:demo', (done) => {
        try {
            console.log('Stopping demo environment...');

            execSync(`docker compose -f ${COMPOSE_DEMO_FILE} down`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            console.log('\nDemo environment stopped!');

            done();
        } catch (error) {
            console.error('Error stopping demo environment:', error.message);
            done(error);
        }
    });

    /**
     * 開発環境のログを表示する
     */
    gulp.task('build:logs:dev', (done) => {
        try {
            console.log('Showing development environment logs...');

            execSync(`docker compose -f ${COMPOSE_FILE} logs -f`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            done();
        } catch (error) {
            console.error('Error showing development logs:', error.message);
            done(error);
        }
    });

    /**
     * デモ環境のログを表示する
     */
    gulp.task('build:logs:demo', (done) => {
        try {
            console.log('Showing demo environment logs...');

            execSync(`docker compose -f ${COMPOSE_DEMO_FILE} logs -f`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            done();
        } catch (error) {
            console.error('Error showing demo logs:', error.message);
            done(error);
        }
    });

    /**
     * 開発環境をビルドして起動する
     */
    gulp.task('build:dev:start', gulp.series('build:dev', 'build:up:dev'));

    /**
     * デモ環境をビルドして起動する
     */
    gulp.task('build:demo:start', gulp.series('build:demo', 'build:up:demo'));

    /**
     * 開発環境の状態を確認する
     */
    gulp.task('build:status:dev', (done) => {
        try {
            console.log('Checking development environment status...\n');

            execSync(`docker compose -f ${COMPOSE_FILE} ps`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            done();
        } catch (error) {
            console.error('Error checking development status:', error.message);
            done(error);
        }
    });

    /**
     * デモ環境の状態を確認する
     */
    gulp.task('build:status:demo', (done) => {
        try {
            console.log('Checking demo environment status...\n');

            execSync(`docker compose -f ${COMPOSE_DEMO_FILE} ps`, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT
            });

            done();
        } catch (error) {
            console.error('Error checking demo status:', error.message);
            done(error);
        }
    });
}
