function DocumentationPage() {

  var api = {
    openPage: function() {
      return openPage('/koski/dokumentaatio', api.isVisible)()
    },
    isVisible: function() {
      return isElementVisible(S('.content h1'))
    }
  }

  return api
}