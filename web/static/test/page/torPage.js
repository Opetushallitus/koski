function TorPage() {
  var api = {
    openPage: function() {
      return openPage("/", api.isVisible)()
    },
    isVisible: function() {
      return S("#content").is(":visible")
    }
  }
  return api
}