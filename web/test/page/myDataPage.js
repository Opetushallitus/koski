const MyDataPage = () => {

  const currentURL = new URL(window.location.href)
  const callbackURL = `${currentURL.protocol}//${currentURL.hostname}:${currentURL.port}/koski/pulssi`

  const api = {
    openPage: () => {
      return openPage(`/koski/omadata/hsl?callback=${callbackURL}`, () => true )()
    },
    currentURL,
    callbackURL,
    go: () => {
      return openPage('/koski/omadata/hsl')()
    },
    isVisible: () => {
      return isElementVisible(S('.username')) && isElementVisible(S('.user > .dateofbirth'))
    },
    login: ()  => {
      return click(findSingle('.lander button'))
    },
    delAuthCookie: () => {
      document.cookie = '_shibsession_=; path=/; expires=Thu, 01 Jan 1980 00:00:01 GMT; Max-Age=0'
    },
    addLangCookie: (lang) => {
      document.cookie = `lang=${lang}; path=/`
    },
    getUserName: () => {
      return extractAsText(S('.user > .username'))
    },
    getBirthDate: () => {
      return extractAsText(S('.user > .dateofbirth'))
    },
    getMemberName: () => {
      return extractAsText(S('.acceptance-member-name'))
    },
    clickAccept: () => {
      return click('.acceptance-button-container > .acceptance-button')()
    },
    clickLogout: () => click('.logout > a')(),
    clickCancel: () => click('a > .decline-link')(),
    accepted: {
      isVisible: () => isElementVisible(S('.acceptance-title-success')),
      isReturnButtonVisible: () => isElementVisible(S('.acceptance-return-button')),
      clickReturn: () => click('.acceptance-return-button')()

    }
  }
  return api
}
