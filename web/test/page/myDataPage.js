const MyDataPage = () => {
  const api = {
    openPage: () => {
      return openPage('/koski/omadata/hsl', () => true )()
    },
    go: () => {
      return openPage('/koski/omadata/hsl')()
    },
    isVisible: () => {
      return isElementVisible(S('.username'))
    },
    login: ()  => {
      return click(findSingle('.lander button'))
    },
    delAuthCookie: () => {
      document.cookie = '_shibsession_=; path=/; expires=Thu, 01 Jan 1980 00:00:01 GMT; Max-Age=0'
    },
    getUserName: () => {
      return extractAsText(S('.header > .user > .username'))
    },
    getBirthDate: () => {
      return extractAsText(S('.user > .dateofbirth'))
    }
  }
  return api
}
