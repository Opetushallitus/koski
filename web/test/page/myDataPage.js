import {
  click,
  extractAsText,
  findSingle,
  isElementVisible,
  openPage,
  S
} from '../util/testHelpers.js'

export function MyDataPage() {
  let callbackURL = window.location.origin + '/koski/pulssi#linkki'

  let api = {
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
    clickChangeLangSwedish: function () {
      return click('#change-lang-sv')()
    },
    clickChangeLangEnglish: function () {
      return click('#change-lang-en')()
    },
    isInFinnish: function () {
      return (
        isElementVisible(S('#change-lang-sv')) &&
        isElementVisible(S('#change-lang-en')) &&
        extractAsText(S('#change-lang-sv')) === 'Svenska' &&
        extractAsText(S('#change-lang-en')) === 'English'
      )
    },
    isInSwedish: function () {
      return (
        isElementVisible(S('#change-lang-fi')) &&
        isElementVisible(S('#change-lang-en')) &&
        extractAsText(S('#change-lang-fi')) === 'Suomi' &&
        extractAsText(S('#change-lang-en')) === 'English'
      )
    },
    isInEnglish: function () {
      return (
        isElementVisible(S('#change-lang-fi')) &&
        isElementVisible(S('#change-lang-sv')) &&
        extractAsText(S('#change-lang-fi')) === 'Suomi' &&
        extractAsText(S('#change-lang-sv')) === 'Svenska'
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
