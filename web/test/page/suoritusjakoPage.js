function SuoritusjakoPage() {
  var api = {
    openPage: function(secretKey) {
      return function() {openPage('/koski/opinnot/' + window.secrets[secretKey], api.isVisible)()}
    },
    changeLanguageButton: function() {
      return S('#topbar .change-lang')
    },
    header: function() {
      return S('.suoritusjako-page .oppija header')
    },
    headerText: function() {
      return S('.suoritusjako-page .oppija header').text()
    },
    isVisible: function() {
      return isElementVisible(S('.suoritusjako-page')) && !isLoading()
    },
    opiskeluoikeudetText: function() {
      return textsOf(S('.oppilaitokset-nav .oppilaitos-nav .oppilaitos-nav-otsikkotiedot'))
    },
    avaaOpiskeluoikeus: function(teksti) {
      return function() {
        return click(findSingle('.oppilaitokset-nav .oppilaitos-nav .oppilaitos-nav-otsikkotiedot:contains(' + teksti + ')'))()
      }
    }
  }
  return api
}
