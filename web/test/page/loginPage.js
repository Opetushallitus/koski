function LoginPage() {
  function loginElement() { return S('#content .login') }
  var pageApi = Page(loginElement);

  var api = {
    openPage: function() {
      return Authentication().logout().then(function() {
          return openPage('/koski/', api.isVisible)()
        }
      )
    },
    login: function(username, password) {
      return function() {
        wait.forMilliseconds(100)()
          .then(pageApi.setInputValue('#username', username))
          .then(pageApi.setInputValue('#password', password))
          .then(pageApi.button(function() { return loginElement().find('button') }).click)
      }
    },
    isVisible: function() {
      return isElementVisible(loginElement())

    },
    isLoginErrorVisible: function() {
      return loginElement().hasClass('error')
    }
  }
  return api
}