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
        return $(row).find('td:not(.tila):not(.aika):not(.valitse)').toArray().map(function(td) {
          return $(td).text().trim()
        })
      })
    },
    openVirhesivu: seq(
      click('.virheet-link'),
      wait.untilVisible('#content .tiedonsiirto-virheet')
    ),
    openYhteenveto: seq(
        click('.yhteenveto-link'),
        wait.untilVisible('#content .tiedonsiirto-yhteenveto'),
        wait.forAjax
    )
  }
  return api
}
