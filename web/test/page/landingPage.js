function LandingPage() {
  var api = {
    openPage: function() {
      return openPage('/koski/', api.isVisible)()
    },
    go: function() {
      return openPage('/koski/')()
    },
    isVisible: function() {
      return isElementVisible(S('.lander')) && !isLoading()
    },
    login: function() {
      return click(findSingle('.lander button'))
    }
  }
  return api
}

function KorhoPankki() {
  var pageApi = Page(findSingle('.korhopankki-page .login'));
  var api = {
    isReady: function() {
      return isElementVisible('.korhopankki-page .login')
    },
    login: function(hetu, surname, firstNames, givenName) {
      return seq(
        pageApi.setInputValue('#hetu', hetu ? hetu : ''),
        pageApi.setInputValue('#sn', surname ? surname : ''),
        pageApi.setInputValue('#FirstName', firstNames ? firstNames : ''),
        pageApi.setInputValue('#givenName', givenName ? givenName : ''),
        click(findSingle('button'))
      )
    }
  }
  return api
}
