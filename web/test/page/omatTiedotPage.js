function OmatTiedotPage() {

  var api = {
    openPage: function() {
      return openPage('/koski/omattiedot', api.isVisible)()
    },
    isVisible: function() {
      return isElementVisible(S('.omattiedot')) && !isLoading()
    },
    nimi: function() {
      return S('.user-info .name').text()
    },
    oppija: function() {
      return S('.main-content.oppija h2').text().replace('JSON', '')
    },
    virhe: function() {
      return S('.ei-suorituksia').text()
    },
    ingressi: function() {
      return S('header .header__caption p').text()
    },
    palvelussaNäkyvätTiedotButton: function() {
      return S('header span:contains(Mitkä tiedot palvelussa näkyvät?)')
    },
    palvelussaNäkyvätTiedotText: function() {
      var el = findFirstNotThrowing('header .tiedot-palvelussa')
      return el ? extractAsText(el) : ''
    },
    palvelussaNäkyvätTiedotCloseButton: function() {
      return S('header .popup__close-button')
    },
    headerNimi: function() {
      var el = findFirstNotThrowing('header .header__name')
      return el ? extractAsText(el) : ''
    }
  }
  return api
}
