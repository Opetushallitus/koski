import {
  click,
  findSingle,
  isElementVisible,
  openPage,
  S,
  seq,
  wait
} from '../util/testHelpers.js'
import { Authentication } from './authentication.js'
import { Page } from './pageApi.js'

export function LoginPage() {
  function loginElement() {
    return S('#content .login')
  }
  var pageApi = Page(loginElement)
  function isVisible() {
    return isElementVisible(loginElement)
  }
  var api = {
    openPage: seq(
      Authentication().logout,
      openPage('/koski/virkailija', isVisible)
    ),
    login: function (username, password) {
      return seq(
        wait.forMilliseconds(100),
        pageApi.setInputValue('#username', username),
        pageApi.setInputValue('#password', password),
        click(findSingle('button', loginElement))
      )
    },
    isVisible,
    isLoginErrorVisible: function () {
      return loginElement().hasClass('error')
    }
  }
  return api
}
