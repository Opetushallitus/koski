function TiedonsiirrotPage() {
  var api = {
    openPage: function() {
      return openPage('/koski/tiedonsiirrot', api.isVisible)()
    },
    isVisible: function() {
      return isElementVisible(S('#content .tiedonsiirrot-content'))
    }
  }
  return api
}