function Authentication() {
  return {
    login: function(username) {
      if (!username) username = 'kalle'
      return function() {
        return Q($.ajax({
          type: 'POST',
          url: '/tor/user/login',
          data: JSON.stringify({username: username, password: 'asdf'}),
          contentType : 'application/json',
          dataType: 'json'
        }))
      }
    },
    logout: function() {
      return Q($.ajax('/tor/user/logout'))
    }
  }
}