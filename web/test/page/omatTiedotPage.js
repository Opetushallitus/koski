function OmatTiedotPage() {

  var api = {
    openPage: function() {
      return openPage('/koski/omattiedot', api.isVisible)()
    },
    isVisible: function() {
      return isElementVisible(S('.omattiedot .main-content:not(.ajax-indicator-bg)'))
    },
    oppija: function() {
      return S('.main-content.oppija h2').text()
    },
    virhe: function() {
      return S('.ei-opiskeluoikeuksia').text()
    }
  }
  return api
}