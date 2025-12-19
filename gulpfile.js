'use strict';

/**
 * Gulpfile that loads tasks from the script directory
 */

import gulp from 'gulp';
import mkdocsTasks from './scripts/mkdocs.js';
import journalTasks from './scripts/journal.js';
import sonarTasks from './scripts/sonar.js';
import schemaspyTasks from './scripts/schemaspy.js';
import deployTasks from './scripts/deploy.js';

// Load gulp tasks from script modules
mkdocsTasks(gulp);
journalTasks(gulp);
sonarTasks(gulp);
schemaspyTasks(gulp);
deployTasks(gulp);

export const dev = gulp.series('mkdocs:serve', 'mkdocs:open', 'sonar:start', 'schemaspy:regenerate');

// Export gulp to make it available to the gulp CLI
export default gulp;
