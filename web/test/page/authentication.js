function Authentication() {
  return {
    login: function (username) {
      if (!username) username = 'kalle'
      return function () {
        return postJson('/koski/user/login', { username, password: username })
      }
    },
    logout: function () {
      return Promise.resolve($.ajax('/koski/user/logout'))
    }
  }
}
