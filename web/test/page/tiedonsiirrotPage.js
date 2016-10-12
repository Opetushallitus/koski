function TiedonsiirrotPage() {

  var api = {
    openPage: function() {
      return openPage('/koski/tiedonsiirrot', api.isVisible)()
    },
    isVisible: function() {
      return isElementVisible(S('#content .tiedonsiirrot-content .tiedonsiirto-taulukko'))
    },
    tiedot: function() {
      return S('.tiedonsiirrot-content table tbody tr').toArray().map(function(row) {
        return $(row).find('td:not(.tila):not(.aika)').toArray().map(function(td) {
          return $(td).text()
        })
      })
    },
    openVirhesivu: function() {
      return function() {
        triggerEvent(S('.virheet-link'), 'click')
        return wait.until(function() { return isElementVisible(S('#content .tiedonsiirto-virheet'))})()
      }
    },
    openYhteenveto: function() {
      return function() {
        triggerEvent(S('.yhteenveto-link'), 'click')
        return wait.until(function() { return isElementVisible(S('#content .tiedonsiirto-yhteenveto'))})()
      }
    }
  }
  return api
}