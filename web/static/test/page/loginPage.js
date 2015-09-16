function LoginPage() {
  function loginElement() { return S("#content .login") }
  var pageApi = Page(loginElement);

  var api = {
    openPage: function() {
      return openPage("/", function() { return S("#content").is(":visible")})()
        .then(function() {
          return testFrame().jQuery.get("/logout")
        })
        .then(function() {
          return openPage("/", api.isVisible)()
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