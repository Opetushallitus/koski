import {
  click,
  extractAsText,
  isElementVisible,
  openPage,
  S
} from '../util/testHelpers.js'

export function TietojenKayttoPage() {
  const api = {
    go: function () {
      return openPage('/koski/omadata/kayttooikeudet')()
    },
    isVisible: function () {
      return (
        isElementVisible(S('.kayttoluvat-info > h1')) &&
        extractAsText(S('.kayttoluvat-info > h1')) === 'Tietojeni käyttö'
      )
    },
    isPermissionsVisible: function () {
      return isElementVisible(S('.kayttolupa-list > li'))
    },
    firstPermission: function () {
      return S('ul.kayttolupa-list > li:first-child > h3')
    },
    cancelPermission: {
      cancelFirstPermission: function () {
        return click('ul.kayttolupa-list > li:first-child .peru-lupa button')()
      },
      isWaitingForVerification: function () {
        return isElementVisible(
          S('div.modal > div.modal-content > div.actions')
        )
      },
      verifyCancel: function () {
        return click('div.modal-content > div.actions > button.vahvista')()
      }
    },
    getUserName: function () {
      return extractAsText(S('.oppija-nimi > .nimi'))
    },
    isErrorShown: function () {
      return isElementVisible(S('#error.error'))
    }
  }
  return api
}
