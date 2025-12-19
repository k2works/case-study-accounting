'use strict';

import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

/**
 * @type {boolean} Windows環境かどうかをチェック
 */
const isWindows = process.platform === 'win32';

/**
 * SchemaSpy関連のGulpタスクを登録する
 * @param {import('gulp').Gulp} gulp
 */
export default function (gulp) {
    const OUTPUT_DIR = path.join(process.cwd(), 'docs', 'assets', 'schemaspy-output');
    const INDEX_FILE = path.join(OUTPUT_DIR, 'index.html');

    /**
     * SchemaSpy ER図を生成する
     */
    gulp.task('schemaspy:generate', (done) => {
        try {
            console.log('Generating SchemaSpy ER diagram...');

            // 出力ディレクトリを作成（存在しない場合）
            if (!fs.existsSync(OUTPUT_DIR)) {
                fs.mkdirSync(OUTPUT_DIR, { recursive: true });
                console.log(`Created output directory: ${OUTPUT_DIR}`);
            }

            // PostgreSQL が起動していることを確認
            console.log('Checking PostgreSQL status...');
            try {
                execSync('docker compose ps postgres --format json', { stdio: 'pipe' });
            } catch (e) {
                console.log('PostgreSQL is not running. Starting it...');
                execSync('docker compose up -d postgres', { stdio: 'inherit' });
                console.log('Waiting for PostgreSQL to be ready...');
                // ヘルスチェックを待つ
                execSync('docker compose exec postgres pg_isready -U postgres', {
                    stdio: 'inherit',
                    timeout: 30000
                });
            }

            // SchemaSpy を実行
            console.log('\nRunning SchemaSpy...');
            execSync('docker compose run --rm schemaspy', { stdio: 'inherit' });

            console.log('\nSchemaSpy ER diagram generated successfully!');
            console.log(`Output directory: ${OUTPUT_DIR}`);

            done();
        } catch (error) {
            console.error('Error generating SchemaSpy ER diagram:', error.message);
            done(error);
        }
    });

    /**
     * 生成されたSchemaSpy ER図をブラウザで開く
     */
    gulp.task('schemaspy:open', (done) => {
        try {
            console.log('Opening SchemaSpy ER diagram...');

            // index.html が存在するか確認
            if (!fs.existsSync(INDEX_FILE)) {
                console.error(`Error: SchemaSpy output not found at ${INDEX_FILE}`);
                console.log('Please run "gulp schemaspy:generate" first.');
                done(new Error('SchemaSpy output not found'));
                return;
            }

            const command = isWindows ? `start ${INDEX_FILE}` : `open ${INDEX_FILE}`;
            execSync(command, { stdio: 'inherit' });

            console.log('SchemaSpy ER diagram opened successfully!');

            done();
        } catch (error) {
            console.error('Error opening SchemaSpy ER diagram:', error.message);
            done(error);
        }
    });

    /**
     * SchemaSpy 出力をクリーンアップする
     */
    gulp.task('schemaspy:clean', (done) => {
        try {
            console.log('Cleaning SchemaSpy output directory...');

            if (fs.existsSync(OUTPUT_DIR)) {
                // ディレクトリ内のファイルを削除（.gitkeep は残す）
                const files = fs.readdirSync(OUTPUT_DIR);
                files.forEach(file => {
                    if (file !== '.gitkeep') {
                        const filePath = path.join(OUTPUT_DIR, file);
                        if (fs.lstatSync(filePath).isDirectory()) {
                            fs.rmSync(filePath, { recursive: true, force: true });
                        } else {
                            fs.unlinkSync(filePath);
                        }
                    }
                });
                console.log('SchemaSpy output directory cleaned successfully!');
            } else {
                console.log('SchemaSpy output directory does not exist.');
            }

            done();
        } catch (error) {
            console.error('Error cleaning SchemaSpy output directory:', error.message);
            done(error);
        }
    });

    /**
     * SchemaSpy を再生成する（クリーン後に生成）
     */
    gulp.task('schemaspy:regenerate', gulp.series('schemaspy:clean', 'schemaspy:generate'));

    /**
     * SchemaSpy を生成してブラウザで開く
     */
    gulp.task('schemaspy', gulp.series('schemaspy:generate', 'schemaspy:open'));
}
