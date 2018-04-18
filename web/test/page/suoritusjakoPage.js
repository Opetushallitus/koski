function SuoritusjakoPage() {
  var api = {
    openPage: function() {
      return function() {openPage('/koski/opinnot/' + window.secret, api.isVisible)()}
    },
    go: function(secret) {
      return openPage('/koski/opinnot' + secret)()
    },
    headerText: function() {
      return S('.suoritusjako-page .oppija header').text()
    },
    isVisible: function() {
      return isElementVisible(S('.suoritusjako-page')) && !isLoading()
    }
  }
  return api
}
