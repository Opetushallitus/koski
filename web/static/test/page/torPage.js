function TorPage() {
  var api = {
    openPage: function() {
      return openPage("/", api.isVisible)()
    },
    isVisible: function() {
      return S("#content .oppija-haku").is(":visible")
    }
  }
  return api
}