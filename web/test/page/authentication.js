function Authentication() {
  return {
    login: function (username) {
      if (!username) username = 'kalle'
      return function () {
        // Closing the previous page aborts its in-flight requests.
        closeTestFrame()
        return postJson('/koski/user/login', { username, password: username })
      }
    },
    logout: function () {
      return Promise.resolve($.ajax('/koski/user/logout'))
    }
  }
}
