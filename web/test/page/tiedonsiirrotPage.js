function TiedonsiirrotPage() {
  var api = {
    openPage: function() {
      return openPage('/koski/tiedonsiirrot', api.isVisible)()
    },
    isVisible: function() {
      return isElementVisible(S('#content .tiedonsiirrot-content'))
    },
    tiedot: function() {
      return S(".tiedonsiirrot-content table tbody tr").toArray().map(function(row) {
        return $(row).find("td:not(.tila):not(.aika)").toArray().map(function(td) {
          return $(td).text()
        })
      })
    }
  }
  return api
}