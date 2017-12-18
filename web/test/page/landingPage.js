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
  var pageApi = Page(findSingle('#korhopankki .login'));
  var api = {
    isReady: function() {
      return isElementVisible('#korhopankki .login')
    },
    login: function(hetu) {
      return seq(
        pageApi.setInputValue('#hetu', hetu),
        click(findSingle('button'))
      )
    }
  }
  return api
}
