'use strict';

import { execSync } from 'child_process';
import path from 'path';

/**
 * Heroku デプロイ関連のGulpタスクを登録する
 * @param {import('gulp').Gulp} gulp
 */
export default function (gulp) {
    const BACKEND_DIR = path.join(process.cwd(), 'apps', 'backend');
    const FRONTEND_DIR = path.join(process.cwd(), 'apps', 'frontend');

    // Heroku アプリ名
    const BACKEND_APP = 'case-study-accounting-backend';
    const FRONTEND_APP = 'case-study-accounting-frontend';

    // Heroku アプリ URL
    const BACKEND_URL = `https://${BACKEND_APP}.herokuapp.com`;
    const FRONTEND_URL = `https://${FRONTEND_APP}.herokuapp.com`;

    /**
     * Heroku Container Registry にログインする
     */
    gulp.task('deploy:login', (done) => {
        try {
            console.log('Logging in to Heroku Container Registry...');

            execSync('heroku container:login', { stdio: 'inherit' });

            console.log('Login succeeded!');

            done();
        } catch (error) {
            console.error('Error logging in to Heroku:', error.message);
            done(error);
        }
    });

    /**
     * バックエンドの Docker イメージをビルドする
     */
    gulp.task('deploy:backend:build', (done) => {
        try {
            console.log('Building backend Docker image...');
            console.log(`Context: ${BACKEND_DIR}`);

            execSync(`docker build -t registry.heroku.com/${BACKEND_APP}/web ${BACKEND_DIR}`, {
                stdio: 'inherit',
                cwd: process.cwd()
            });

            console.log('\nBackend Docker image built successfully!');

            done();
        } catch (error) {
            console.error('Error building backend Docker image:', error.message);
            done(error);
        }
    });

    /**
     * バックエンドの Docker イメージをプッシュする
     */
    gulp.task('deploy:backend:push', (done) => {
        try {
            console.log('Pushing backend Docker image to Heroku...');

            execSync(`docker push registry.heroku.com/${BACKEND_APP}/web`, { stdio: 'inherit' });

            console.log('\nBackend Docker image pushed successfully!');

            done();
        } catch (error) {
            console.error('Error pushing backend Docker image:', error.message);
            done(error);
        }
    });

    /**
     * バックエンドをリリースする
     */
    gulp.task('deploy:backend:release', (done) => {
        try {
            console.log('Releasing backend to Heroku...');

            execSync(`heroku container:release web -a ${BACKEND_APP}`, { stdio: 'inherit' });

            console.log('\nBackend released successfully!');
            console.log(`Backend URL: ${BACKEND_URL}`);

            done();
        } catch (error) {
            console.error('Error releasing backend:', error.message);
            done(error);
        }
    });

    /**
     * バックエンドをデプロイする（ビルド → プッシュ → リリース）
     */
    gulp.task('deploy:backend', gulp.series(
        'deploy:login',
        'deploy:backend:build',
        'deploy:backend:push',
        'deploy:backend:release'
    ));

    /**
     * フロントエンドの Docker イメージをビルドする
     */
    gulp.task('deploy:frontend:build', (done) => {
        try {
            console.log('Building frontend Docker image...');
            console.log(`Context: ${FRONTEND_DIR}`);

            execSync(`docker build -t registry.heroku.com/${FRONTEND_APP}/web ${FRONTEND_DIR}`, {
                stdio: 'inherit',
                cwd: process.cwd()
            });

            console.log('\nFrontend Docker image built successfully!');

            done();
        } catch (error) {
            console.error('Error building frontend Docker image:', error.message);
            done(error);
        }
    });

    /**
     * フロントエンドの Docker イメージをプッシュする
     */
    gulp.task('deploy:frontend:push', (done) => {
        try {
            console.log('Pushing frontend Docker image to Heroku...');

            execSync(`docker push registry.heroku.com/${FRONTEND_APP}/web`, { stdio: 'inherit' });

            console.log('\nFrontend Docker image pushed successfully!');

            done();
        } catch (error) {
            console.error('Error pushing frontend Docker image:', error.message);
            done(error);
        }
    });

    /**
     * フロントエンドをリリースする
     */
    gulp.task('deploy:frontend:release', (done) => {
        try {
            console.log('Releasing frontend to Heroku...');

            execSync(`heroku container:release web -a ${FRONTEND_APP}`, { stdio: 'inherit' });

            console.log('\nFrontend released successfully!');
            console.log(`Frontend URL: ${FRONTEND_URL}`);

            done();
        } catch (error) {
            console.error('Error releasing frontend:', error.message);
            done(error);
        }
    });

    /**
     * フロントエンドをデプロイする（ビルド → プッシュ → リリース）
     */
    gulp.task('deploy:frontend', gulp.series(
        'deploy:login',
        'deploy:frontend:build',
        'deploy:frontend:push',
        'deploy:frontend:release'
    ));

    /**
     * 全アプリケーションをデプロイする
     */
    gulp.task('deploy:all', gulp.series(
        'deploy:login',
        gulp.parallel('deploy:backend:build', 'deploy:frontend:build'),
        gulp.parallel('deploy:backend:push', 'deploy:frontend:push'),
        gulp.parallel('deploy:backend:release', 'deploy:frontend:release')
    ));

    /**
     * デプロイ状態を確認する
     */
    gulp.task('deploy:status', (done) => {
        try {
            console.log('Checking deployment status...\n');

            console.log('=== Backend ===');
            execSync(`heroku ps -a ${BACKEND_APP}`, { stdio: 'inherit' });

            console.log('\n=== Frontend ===');
            execSync(`heroku ps -a ${FRONTEND_APP}`, { stdio: 'inherit' });

            done();
        } catch (error) {
            console.error('Error checking deployment status:', error.message);
            done(error);
        }
    });

    /**
     * バックエンドのログを表示する
     */
    gulp.task('deploy:backend:logs', (done) => {
        try {
            console.log('Showing backend logs...');

            execSync(`heroku logs --tail -a ${BACKEND_APP}`, { stdio: 'inherit' });

            done();
        } catch (error) {
            console.error('Error showing backend logs:', error.message);
            done(error);
        }
    });

    /**
     * フロントエンドのログを表示する
     */
    gulp.task('deploy:frontend:logs', (done) => {
        try {
            console.log('Showing frontend logs...');

            execSync(`heroku logs --tail -a ${FRONTEND_APP}`, { stdio: 'inherit' });

            done();
        } catch (error) {
            console.error('Error showing frontend logs:', error.message);
            done(error);
        }
    });

    /**
     * バックエンドをブラウザで開く
     */
    gulp.task('deploy:backend:open', (done) => {
        try {
            console.log('Opening backend in browser...');

            execSync(`heroku open -a ${BACKEND_APP}`, { stdio: 'inherit' });

            done();
        } catch (error) {
            console.error('Error opening backend:', error.message);
            done(error);
        }
    });

    /**
     * フロントエンドをブラウザで開く
     */
    gulp.task('deploy:frontend:open', (done) => {
        try {
            console.log('Opening frontend in browser...');

            execSync(`heroku open -a ${FRONTEND_APP}`, { stdio: 'inherit' });

            done();
        } catch (error) {
            console.error('Error opening frontend:', error.message);
            done(error);
        }
    });

    /**
     * 両アプリケーションをブラウザで開く
     */
    gulp.task('deploy:open', gulp.parallel('deploy:backend:open', 'deploy:frontend:open'));
}
