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
      return S('.ei-opiskeluoikeuksia').text()
    }
  }
  return api
}
