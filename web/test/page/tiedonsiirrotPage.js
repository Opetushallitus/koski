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
      return Q().then(click(S('.virheet-link')))
        .then(wait.until(function() { return isElementVisible(S('#content .tiedonsiirto-virheet'))}))
    },
    openYhteenveto: function() {
      return Q().then(click(S('.yhteenveto-link')))
        .then(wait.until(function() { return isElementVisible(S('#content .tiedonsiirto-yhteenveto'))}))
        .then(wait.forAjax)
    }
  }
  return api
}