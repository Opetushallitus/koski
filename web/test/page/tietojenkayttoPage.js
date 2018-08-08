const TietojenKayttoPage = () => {

  const api = {
    go: () => openPage('/koski/kayttooikeudet')() ,
    isVisible: () => isElementVisible(S('.kayttoluvat-info > h1')) && extractAsText(S('.kayttoluvat-info > h1')) === 'Tietojeni käyttöluvat'
  }
  return api
}
