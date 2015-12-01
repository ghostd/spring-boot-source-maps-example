This is a demo project showing how to serve static resources with Spring Boot.
The URLs of the resources are versioned (either with a fixed version, either with a MD5 hash of the file content).
The application wraps the Angular exception handler and send the crash informations to the server.

# Technical dependencies

- [Spring Boot](https://github.com/spring-projects/spring-boot) for the server side (controller, serving static resources, etc)
- [Gulp](https://github.com/gulpjs/gulp) and [UglifyJS](https://github.com/mishoo/UglifyJS2/) to prepare and mangle the static resources
- [Angular](https://github.com/angular/angular.js) for the fun
- [Google Closure compiler](https://github.com/google/closure-compiler) to read the source maps and map the JS stack frames

# How to play with it
## Run the application

All you need is a JVM and Maven. Then, in the project directory:
```sh
mvn package
cd war
mvn spring-boot:run
```

## Crash it

Open your favorite browser and go to [http://localhost:8080](http://localhost:8080).
You should get a page with a "Crash me!" button. Click it to crash the application.

Now, check your JavaScript console, you should have something like that:
```sh
Error: y is not defined
r.crash@http://localhost:8080/v1.2.3/js/app.min.js:1:605
anonymous/fn@http://localhost:8080/js/libs.min-15d5de83249c076bb2be185454f98af6.js line 6 > Function:2:203
Go[t]</<.compile/</</i@http://localhost:8080/js/libs.min-15d5de83249c076bb2be185454f98af6.js:7:6269
gn/this.$get</d.prototype.$eval@http://localhost:8080/js/libs.min-15d5de83249c076bb2be185454f98af6.js:5:5877
gn/this.$get</d.prototype.$apply@http://localhost:8080/js/libs.min-15d5de83249c076bb2be185454f98af6.js:5:6106
Go[t]</<.compile/</<@http://localhost:8080/js/libs.min-15d5de83249c076bb2be185454f98af6.js:7:6319
Ge@http://localhost:8080/js/libs.min-15d5de83249c076bb2be185454f98af6.js:3:16504
We/n@http://localhost:8080/js/libs.min-15d5de83249c076bb2be185454f98af6.js:3:16453
 undefined
```

Check your server logs, you should get something like that:
```sh
Client JS exception ReferenceError: y is not defined
 with user agent: Mozilla/5.0 (X11; Linux x86_64; rv:42.0) Gecko/20100101 Firefox/42.0 Iceweasel/42.0
 with data: {some=app data}
 with stack:
  r.crash@app.js:52:17
  anonymous/fn@http://localhost:8080/js/libs.min-15d5de83249c076bb2be185454f98af6.jsline6>Function:2:203
  Go[t]</<.compile/</</i@angular.js:23613:17
  gn/this.$get</d.prototype.$eval@angular.js:16052:16
  gn/this.$get</d.prototype.$apply@angular.js:16152:20
  Go[t]</<.compile/</<@angular.js:23618:17
  Ge@angular.js:3346:3
  We/n@angular.js:3334:9
```

Compare the line numbers of the `app.js` and `app.min.js` files, they are not the same.
In your server logs, you now have an helpful stack :-)

# License

[MIT](LICENSE)
