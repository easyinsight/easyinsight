var easyInsight = angular.module("easyInsight", ["eiAccounts", "eiDataSources", 'ui.bootstrap', 'ngRoute', 'route-segment', 'view-segment', 'cgBusy']);

easyInsight.config(function ($routeProvider, $locationProvider, $routeSegmentProvider) {
    $locationProvider.html5Mode(true);
    $routeSegmentProvider.when("/missing", "missing").
        segment("missing", {
            templateUrl: '/missing.template.html',
            controller: "MissingFileController"
        });
    $routeProvider.otherwise({ redirectTo: "/missing" });
})

easyInsight.factory('PageInfo', function() {
    var title = '';
    return {
        title: function() {
            if(title.length == 0)
                return "Easy Insight";
            return "Easy Insight - " + title;
        },
        setTitle: function(value) {
            title = value;
        }
    }
})

easyInsight.controller('MissingFileController', function(PageInfo) {
    PageInfo.setTitle("Not Found");
})

easyInsight.config(function($httpProvider) {
    $httpProvider.interceptors.push(function($q) {
        return {
            'request': function(config) {
                config.headers["X-REQUESTED-WITH"] = "XmlHttpRequest";
                return config;
            }
        }
    })
})

easyInsight.run(function ($rootScope, $http, PageInfo) {
    $rootScope.user = {
        "username": "..."
    };
    $http.get("/app/userInfo.json").success(function (d, r) {
        if (r == 401)
            window.location = "/app/login.jsp";
        else {
            $rootScope.bookmarks = d.bookmarks;
            $rootScope.user = d.user;
        }
    }).error(function () {
        window.location = "/app/login.jsp";
    });

    $rootScope.numKeys = function(obj) {
        if(!obj) return 0;
        return Object.keys(obj).length;
    }
    $rootScope.PageInfo = PageInfo;
});

easyInsight.directive("passwordVerify", function() {
   return {
      require: "ngModel",
      scope: {
        passwordVerify: '='
      },
      link: function(scope, element, attrs, ctrl) {
        scope.$watch(function() {
            var combined;
            if (scope.passwordVerify || ctrl.$viewValue) {
               combined = scope.passwordVerify + '_' + ctrl.$viewValue;
            }
            return combined;
        }, function(value) {
            if(scope.passwordVerify && !ctrl.$viewValue) {
                ctrl.$setValidity("passwordVerify", false);
            }
            if (value) {
                ctrl.$parsers.unshift(function(viewValue) {
                    var origin = scope.passwordVerify;
                    if (origin !== viewValue) {
                        ctrl.$setValidity("passwordVerify", false);
                        return undefined;
                    } else {
                        ctrl.$setValidity("passwordVerify", true);
                        return viewValue;
                    }
                });
            }
        });
     }
   };
});