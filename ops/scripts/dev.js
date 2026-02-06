'use strict';

import { spawn } from 'child_process';
import { platform } from 'os';

/**
 * クロスプラットフォーム対応の開発サーバー起動スクリプト
 * Windows と Unix 系 OS の両方で動作する
 */

/**
 * 子プロセスを起動し、ライフサイクルイベントを管理する
 * @param {string} cmd - 実行コマンド
 * @param {string[]} args - コマンド引数
 * @param {Object} options - spawn オプション
 * @param {string} label - ログ表示用ラベル
 * @param {Function} done - Gulp 完了コールバック
 * @returns {import('child_process').ChildProcess}
 */
function spawnServer(cmd, args, options, label, done) {
    const child = spawn(cmd, args, options);

    child.on('error', (error) => {
        console.error(`Error starting ${label}:`, error.message);
        done(error);
    });

    child.on('close', (code) => {
        if (code !== 0) {
            console.error(`${label} process exited with code ${code}`);
        }
        done();
    });

    process.on('SIGINT', () => {
        console.log(`\nStopping ${label} server...`);
        child.kill();
        process.exit();
    });

    return child;
}

/**
 * 開発環境関連の Gulp タスクを登録する
 * @param {import('gulp').Gulp} gulp
 */
export default function (gulp) {
    const PROJECT_ROOT = process.cwd();
    const isWindows = platform() === 'win32';
    const gradlewCmd = isWindows ? '.\\apps\\backend\\gradlew.bat' : './apps/backend/gradlew';
    const npmCmd = isWindows ? 'npm.cmd' : 'npm';
    const shellOpt = isWindows;

    /**
     * バックエンド開発サーバーを起動する
     */
    gulp.task('dev:backend', (done) => {
        try {
            console.log('Starting backend development server...');
            spawnServer(
                gradlewCmd,
                ['-p', 'apps/backend', 'bootRun'],
                { stdio: 'inherit', cwd: PROJECT_ROOT, shell: shellOpt },
                'backend',
                done
            );
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
            spawnServer(
                npmCmd,
                ['run', 'dev'],
                { stdio: 'inherit', cwd: `${PROJECT_ROOT}/apps/frontend`, shell: shellOpt },
                'frontend',
                done
            );
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
            spawnServer(
                npmCmd,
                ['run', 'dev:e2e'],
                { stdio: 'inherit', cwd: `${PROJECT_ROOT}/apps/frontend`, shell: shellOpt },
                'frontend E2E',
                done
            );
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

            const backend = spawn(gradlewCmd, ['-p', 'apps/backend', 'bootRun'], {
                stdio: 'inherit',
                cwd: PROJECT_ROOT,
                shell: shellOpt
            });

            const frontend = spawn(npmCmd, ['run', 'dev'], {
                stdio: 'inherit',
                cwd: `${PROJECT_ROOT}/apps/frontend`,
                shell: shellOpt
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
