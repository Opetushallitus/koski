const TietojenKayttoPage = () => {

  const api = {
    go: () => openPage('/koski/kayttooikeudet')() ,
    isVisible: () => isElementVisible(S('.kayttoluvat-info > h1')) && extractAsText(S('.kayttoluvat-info > h1')) === 'Tietojeni käyttöluvat',
    expandPermissions: () => click('.kayttoluvat-expander > .kayttolupa-button')(),
    isPermissionsExpanded: () => isElementVisible(S('.kayttolupa-list > li')),
    firstPermission: () => S('ul.kayttolupa-list > li:first-child > h3'),
    cancelPermission: {
      cancelFirstPermission: () => click('ul.kayttolupa-list > li:first-child .peru-lupa button')(),
      isWaitingForVerification: () => isElementVisible(S('div.modal > div.modal-content > div.actions')),
      verifyCancel: () => click('div.modal-content > div.actions > button.vahvista')()
    }
  }
  return api
}
