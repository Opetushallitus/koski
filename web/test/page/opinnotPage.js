function OpinnotPage() {

  function oppija() { return S('.oppija') }

  var api = {
    getTutkinto() {
      return S('.tutkinto .tutkinto-name').text()
    },
    getOppilaitos() {
      return S('.tutkinto .oppilaitos').text()
    }
  }

  return api
}