function LoginPage() {
  function loginElement() { return S("#content .login") }
  var pageApi = Page(loginElement);

  var api = {
    openPage: function() {
      return openPage("/tor/", function() { return S("#content").is(":visible")})()
        .then(function() {
          return testFrame().jQuery.get("/tor/logout")
        })
        .then(function() {
          return openPage("/tor/", api.isVisible)()
        })
    },
    login: function(username, password) {
      return function() {
        return pageApi.setInputValue("#username", username)()
          .then(pageApi.setInputValue("#password", password))
          .then(pageApi.button(function() { return loginElement().find("button") }).click)
      }
    },
    isVisible: function() {
      return loginElement().is(":visible")
    },
    isLoginErrorVisible: function() {
      return loginElement().hasClass("error")
    }
  }
  return api
}