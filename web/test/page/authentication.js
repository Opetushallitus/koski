function Authentication() {
  return {
    login: function(username) {
      if (!username) username = 'kalle'
      return function() {
        return postJson('/koski/user/login', {username: username, password: username})
      }
    },
    logout: function() {
      return Q($.ajax('/koski/user/logout'))
    }
  }
}