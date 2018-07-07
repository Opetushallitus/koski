const MyDataPage = () => {
  const api = {
    openPage: () => {
      const currentURL = new URL(window.location.href)
      const callbackURL = `${currentURL.protocol}//${currentURL.hostname}:${currentURL.port}/koski/pulssi`
      return openPage(`/koski/omadata/hsl?callback=${callbackURL.toString()}`, () => true )()
    },
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
    accepted: {
      isVisible: () => isElementVisible(S('.acceptance-title-success')),
      isReturnButtonVisible: () => isElementVisible(S('.acceptance-return-button')),
      clickReturn: () => click('.acceptance-return-button')()

    }
  }
  return api
}
