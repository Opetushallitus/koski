function TietojenKayttoPage() {

  var api = {
    go: function() {
      return openPage('/koski/omadata/kayttooikeudet')()
    },
    isVisible: function() {
      return isElementVisible(S('.kayttoluvat-info > h1')) && extractAsText(S('.kayttoluvat-info > h1')) === 'Tietojeni käyttöluvat'
    },
    expandPermissions: function() {
      return click('.kayttoluvat-expander > .kayttolupa-button')()
    },
    isPermissionsExpanded: function() {
      return isElementVisible(S('.kayttolupa-list > li'))
    },
    firstPermission: function() {
      return S('ul.kayttolupa-list > li:first-child > h3')
    },
    cancelPermission: {
      cancelFirstPermission: function() {
        return click('ul.kayttolupa-list > li:first-child .peru-lupa button')()
      },
      isWaitingForVerification: function() {
        return isElementVisible(S('div.modal > div.modal-content > div.actions'))
      },
      verifyCancel: function() {
        return click('div.modal-content > div.actions > button.vahvista')()
      }
    }
  }
  return api
}
