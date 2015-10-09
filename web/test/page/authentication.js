function Authentication() {
  return {
    login: function() {
      return Q($.ajax({
        type: 'POST',
        url: '/tor/user/login',
        data: JSON.stringify({username: 'kalle', password: 'asdf'}),
        contentType : 'application/json',
        dataType: 'json'
      }))
    },
    logout: function() {
      return Q($.ajax('/tor/user/logout'))
    }
  }
}