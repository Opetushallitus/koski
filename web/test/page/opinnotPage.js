function OpinnotPage() {

  function oppija() { return S('.oppija') }

  var api = {
    getTutkinto() {
      return S('.opintooikeus .tutkinto').text()
    },
    getOppilaitos() {
      return S('.opintooikeus .oppilaitos').text()
    }
  }

  return api
}