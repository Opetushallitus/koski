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
      return textsOf(tutkinnonOsa().find('.name'))
    },
    getTutkinnonOsa: function(nimi) {
      return TutkinnonOsa(nimi)
    },
    selectSuoritustapa: function(suoritustapa) {
      return function() {
        return Page(opintoOikeus).setInputValue(".suoritustapa", suoritustapa)().then(wait.forAjax())
      }
    },
    selectOsaamisala: function(osaamisala) {
      return function() {
        return Page(opintoOikeus).setInputValue(".osaamisala", osaamisala)().then(wait.forAjax())
      }
    }

  }

  return api
}

function TutkinnonOsa(nimi) {
  function tutkinnonOsaElement() {
    return S(".tutkinnon-osa .name:contains(" + nimi + ")").parent()
  }
  function saveButton() {
    return tutkinnonOsaElement().find("button")
  }
  api = {
    addArviointi: function(arvosana) {
      return function() {
        tutkinnonOsaElement().find(".arvosanat li:contains("+arvosana+")").click()
        saveButton().click()
        return wait.forAjax()
      }
    },
    getArvosana: function() {
      return tutkinnonOsaElement().find(".arvosana").text()
    }
  }

  return api
}