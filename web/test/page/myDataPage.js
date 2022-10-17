function MyDataPage() {
  var callbackURL = window.location.origin + '/koski/pulssi#linkki'

  var api = {
    openPage: function () {
      return openPage(
        '/koski/omadata/valtuutus/hsl?callback=' +
          encodeURIComponent(callbackURL),
        function () {
          return true
        }
      )()
    },
    callbackURL,
    go: function () {
      return openPage('/koski/omadata/valtuutus/hsl')()
    },
    isVisible: function () {
      return (
        isElementVisible(S('.username')) &&
        isElementVisible(S('.user > .dateofbirth'))
      )
    },
    login: function () {
      return click(findSingle('.lander button'))
    },
    delAuthCookie: function () {
      document.cookie =
        '_shibsession_=; path=/; expires=Thu, 01 Jan 1980 00:00:01 GMT; Max-Age=0'
    },
    addLangCookie: function (lang) {
      document.cookie = 'lang=' + lang + '; path=/'
    },
    getUserName: function () {
      return extractAsText(S('.user > .username'))
    },
    getBirthDate: function () {
      return extractAsText(S('.user > .dateofbirth'))
    },
    getMemberName: function () {
      return extractAsText(S('.acceptance-member-name'))
    },
    getMemberPurpose: function () {
      return extractAsText(S('.acceptance-member-purpose'))
    },
    clickAccept: function () {
      return click('.acceptance-button-container > .acceptance-button')()
    },
    clickLogout: function () {
      return click('.logout > a')()
    },
    clickCancel: function () {
      return click('.decline-link > a')()
    },
    clickChangeLang: function () {
      return click('.lang > .change-lang')()
    },
    isInFinnish: function () {
      return (
        isElementVisible(S('.change-lang')) &&
        extractAsText(S('.change-lang')) === 'På svenska'
      )
    },
    isInSwedish: function () {
      return (
        isElementVisible(S('.change-lang')) &&
        extractAsText(S('.change-lang')) === 'Suomeksi'
      )
    },
    accepted: {
      isVisible: function () {
        return isElementVisible(S('.acceptance-title-success'))
      },
      isReturnButtonVisible: function () {
        return isElementVisible(S('.acceptance-return-button'))
      },
      clickReturn: function () {
        return click('.acceptance-return-button')()
      }
    }
  }
  return api
}
