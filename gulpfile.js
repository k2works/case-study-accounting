'use strict';

/**
 * Gulpfile that loads tasks from the script directory
 */

import gulp from 'gulp';
import mkdocsTasks from './ops/scripts/mkdocs.js';
import journalTasks from './ops/scripts/journal.js';
import sonarTasks from './ops/scripts/sonar.js';
import schemaspyTasks from './ops/scripts/schemaspy.js';
import deployTasks from './ops/scripts/deploy.js';
import buildTasks from './ops/scripts/build.js';
import devTasks from './ops/scripts/dev.js';

// Load gulp tasks from script modules
mkdocsTasks(gulp);
journalTasks(gulp);
sonarTasks(gulp);
schemaspyTasks(gulp);
deployTasks(gulp);
buildTasks(gulp);
devTasks(gulp);

export const dev = gulp.series('mkdocs:serve', 'mkdocs:open', 'sonar:start', 'schemaspy:regenerate', 'build:dev:start');

// Export gulp to make it available to the gulp CLI
export default gulp;
