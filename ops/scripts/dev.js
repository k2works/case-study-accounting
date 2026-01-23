'use strict';

import { spawn } from 'child_process';
import { platform } from 'os';

/**
 * クロスプラットフォーム対応の開発サーバー起動スクリプト
 * Windows と Unix 系 OS の両方で動作する
 */

/**
 * 開発環境関連の Gulp タスクを登録する
 * @param {import('gulp').Gulp} gulp
 */
export default function (gulp) {
    const PROJECT_ROOT = process.cwd();
    const isWindows = platform() === 'win32';

    /**
     * バックエンド開発サーバーを起動する
     */
    gulp.task('dev:backend', (done) => {
        try {
            console.log('Starting backend development server...');

            const gradlewCmd = isWindows
                ? '.\\apps\\backend\\gradlew.bat'
                : './apps/backend/gradlew';

            const args = ['-p', 'apps/backend', 'bootRun'];

            const backend = spawn(gradlewCmd, args, {
                stdio: 'inherit',
                cwd: PROJECT_ROOT,
                shell: isWindows
            });

            backend.on('error', (error) => {
                console.error('Error starting backend:', error.message);
                done(error);
            });

            backend.on('close', (code) => {
                if (code !== 0) {
                    console.error(`Backend process exited with code ${code}`);
                }
                done();
            });

            // Ctrl+C でのクリーンな終了
            process.on('SIGINT', () => {
                console.log('\nStopping backend server...');
                backend.kill();
                process.exit();
            });

        } catch (error) {
            console.error('Error starting backend development server:', error.message);
            done(error);
        }
    });

    /**
     * フロントエンド開発サーバーを起動する
     */
    gulp.task('dev:frontend', (done) => {
        try {
            console.log('Starting frontend development server...');

            const npmCmd = isWindows ? 'npm.cmd' : 'npm';
            const args = ['run', 'dev'];

            const frontend = spawn(npmCmd, args, {
                stdio: 'inherit',
                cwd: `${PROJECT_ROOT}/apps/frontend`,
                shell: isWindows
            });

            frontend.on('error', (error) => {
                console.error('Error starting frontend:', error.message);
                done(error);
            });

            frontend.on('close', (code) => {
                if (code !== 0) {
                    console.error(`Frontend process exited with code ${code}`);
                }
                done();
            });

            // Ctrl+C でのクリーンな終了
            process.on('SIGINT', () => {
                console.log('\nStopping frontend server...');
                frontend.kill();
                process.exit();
            });

        } catch (error) {
            console.error('Error starting frontend development server:', error.message);
            done(error);
        }
    });

    /**
     * フロントエンド E2E テスト用開発サーバーを起動する
     */
    gulp.task('dev:frontend:e2e', (done) => {
        try {
            console.log('Starting frontend E2E development server...');

            const npmCmd = isWindows ? 'npm.cmd' : 'npm';
            const args = ['run', 'dev:e2e'];

            const frontend = spawn(npmCmd, args, {
                stdio: 'inherit',
                cwd: `${PROJECT_ROOT}/apps/frontend`,
                shell: isWindows
            });

            frontend.on('error', (error) => {
                console.error('Error starting frontend E2E server:', error.message);
                done(error);
            });

            frontend.on('close', (code) => {
                if (code !== 0) {
                    console.error(`Frontend E2E process exited with code ${code}`);
                }
                done();
            });

            // Ctrl+C でのクリーンな終了
            process.on('SIGINT', () => {
                console.log('\nStopping frontend E2E server...');
                frontend.kill();
                process.exit();
            });

        } catch (error) {
            console.error('Error starting frontend E2E development server:', error.message);
            done(error);
        }
    });

    /**
     * バックエンドとフロントエンドの開発サーバーを同時に起動する
     */
    gulp.task('dev:all', (done) => {
        try {
            console.log('Starting backend and frontend development servers...');

            const gradlewCmd = isWindows
                ? '.\\apps\\backend\\gradlew.bat'
                : './apps/backend/gradlew';

            const npmCmd = isWindows ? 'npm.cmd' : 'npm';

            // バックエンド起動
            const backend = spawn(gradlewCmd, ['-p', 'apps/backend', 'bootRun'], {
                stdio: 'inherit',
                cwd: PROJECT_ROOT,
                shell: isWindows
            });

            // フロントエンド起動
            const frontend = spawn(npmCmd, ['run', 'dev'], {
                stdio: 'inherit',
                cwd: `${PROJECT_ROOT}/apps/frontend`,
                shell: isWindows
            });

            backend.on('error', (error) => {
                console.error('Error starting backend:', error.message);
                frontend.kill();
                done(error);
            });

            frontend.on('error', (error) => {
                console.error('Error starting frontend:', error.message);
                backend.kill();
                done(error);
            });

            // いずれかが終了したら両方終了
            backend.on('close', (code) => {
                if (code !== 0) {
                    console.error(`Backend process exited with code ${code}`);
                }
                frontend.kill();
                done();
            });

            frontend.on('close', (code) => {
                if (code !== 0) {
                    console.error(`Frontend process exited with code ${code}`);
                }
                backend.kill();
                done();
            });

            // Ctrl+C でのクリーンな終了
            process.on('SIGINT', () => {
                console.log('\nStopping development servers...');
                backend.kill();
                frontend.kill();
                process.exit();
            });

        } catch (error) {
            console.error('Error starting development servers:', error.message);
            done(error);
        }
    });
}
