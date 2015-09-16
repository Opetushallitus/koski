function TorPage() {
  var api = {
    openPage: function() {
      return openPage("/tor/", api.isVisible)()
    },
    isVisible: function() {
      return S("#content .oppija-haku").is(":visible")
    }
  }
  return api
}