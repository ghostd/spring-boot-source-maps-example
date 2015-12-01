var gulp = require('gulp'),
  uglify = require('gulp-uglify'),
  sourcemaps = require('gulp-sourcemaps'),
  concat = require('gulp-concat'),
  rm = require('rimraf');

var paths = {
  libs: [
    'node_modules/stackframe/dist/stackframe.js',
    'node_modules/error-stack-parser/dist/error-stack-parser.js',
    'node_modules/angular/angular.js'
  ],
  app: ['app/app.js'],
  targetDir: 'target/classes/static' // To be maven compliant
};

gulp.task('clean', function (cb) {
  return rm('target', cb);
});

gulp.task('libs:scripts', ['clean'], function () {
  return gulp.src(paths.libs)
    .pipe(sourcemaps.init())
    .pipe(uglify())
    .pipe(concat('libs.min.js'))
    // We generate an external source map for the POC
    .pipe(sourcemaps.write('../maps'))
    .pipe(gulp.dest(paths.targetDir + '/js'));
});

gulp.task('app:scripts', ['libs:scripts'], function () {
  return gulp.src(paths.app)
    .pipe(sourcemaps.init())
    .pipe(uglify())
    .pipe(concat('app.min.js'))
    // We generate an inline source map for the POC
    .pipe(sourcemaps.write())
    .pipe(gulp.dest(paths.targetDir + '/js'));
});

gulp.task('default', ['app:scripts']);
