function OpinnotPage() {

  function oppija() { return S('.oppija') }
  function tutkinnonOsa() { return S('.tutkinnon-osa') }
  function opintoOikeus() { return S('.opintooikeus')}

  var api = {
    getTutkinto: function() {
      return S('.opintooikeus .tutkinto').text()
    },
    getOppilaitos: function() {
      return S('.opintooikeus .oppilaitos').text()
    },
    getTutkinnonOsat: function() {
      return textsOf(tutkinnonOsa())
    },
    selectSuoritustapa: function(suoritustapa) {
      return Page(opintoOikeus).setInputValue(".suoritustapa", suoritustapa)
    },
    selectOsaamisala: function(osaamisala) {
      return Page(opintoOikeus).setInputValue(".osaamisala", osaamisala)
    }

  }

  return api
}