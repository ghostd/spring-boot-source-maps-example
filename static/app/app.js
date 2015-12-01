(function () {
  'use strict';

  angular.module('crashApp', [])

    .factory('crashReporter', ['$injector', function ($injector) {
      var $http = null;

      return {
        logException: function (exception, cause) {
          if ($http == null) {
            $http = $injector.get('$http'); // avoid cyclic dependency...
          }

          // try to parse stack to send usable info to client
          var parsedStack;
          try {
            parsedStack = window.ErrorStackParser.parse(exception);
          } catch (e) {
            parsedStack = null;
          }

          var exceptionData = {
            name: exception.name,
            message: exception.message,
            rawStack: exception.stack,
            parsedStack: parsedStack
          };

          exceptionData.additionalData = {
            some: 'app data' // Like who was the connected user?
          };

          $http.post('/api/crash-reporter', exceptionData);
        }
      };
    }])

    .config(['$provide', function ($provide) {
      $provide.decorator('$exceptionHandler', ['$delegate', 'crashReporter',
        function ($delegate, remoteExceptionLogger) {
          return function (exception, cause) {
            $delegate(exception, cause);
            remoteExceptionLogger.logException(exception, cause);
          };
        }]);
    }])

    .controller('CrashCtrl', ['$scope', function ($scope) {
      $scope.crash = function () {
        // Oops, y is undefined
        var x = y / 2;
      };
    }])

  ;
})();
