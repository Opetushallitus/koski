function LandingPage() {
  var api = {
    openPage: function () {
      return openPage('/koski/', api.isVisible)()
    },
    openMobilePage: function () {
      return openPage('/koski/', api.isVisible, 375)()
    },
    go: function () {
      return openPage('/koski/')()
    },
    isVisible: function () {
      return isElementVisible(S('.lander')) && !isLoading()
    },
    login: function () {
      return click(findSingle('.lander button'))
    }
  }
  return api
}

function KorhoPankki() {
  var pageApi = Page(findSingle('.korhopankki-page .login'))
  var api = {
    isReady: function () {
      return isElementVisible('.korhopankki-page .login')
    },
    login: function (hetu, surname, firstNames, givenName, lang) {
      return seq(
        pageApi.setInputValue('#hetu', hetu || ''),
        pageApi.setInputValue('#sn', surname || ''),
        pageApi.setInputValue('#FirstName', firstNames || ''),
        pageApi.setInputValue('#givenName', givenName || ''),
        pageApi.setInputValue('#lang', lang || ''),
        click(findSingle('button'))
      )
    }
  }
  return api
}
