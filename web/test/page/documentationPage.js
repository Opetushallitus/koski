function DocumentationPage() {
  var api = {
    openPage: function () {
      return openPage('/koski/documentation', api.isVisible)()
    },
    isVisible: function () {
      return isElementVisible(S('.dokumentaatio-content h2'))
    }
  }

  return api
}
