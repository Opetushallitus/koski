function OpinnotPage() {

  function oppija() { return S('.oppija') }

  var api = {
    getTutkinto() {
      return S('.opintooikeus .tutkinto').text()
    },
    getOppilaitos() {
      return S('.opintooikeus .oppilaitos').text()
    },
    getTutkinnonOsat() {
      return textsOf(S('.tutkinnon-osa'))
    }
  }

  return api
}