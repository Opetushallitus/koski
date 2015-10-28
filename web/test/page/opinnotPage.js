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
    getTutkinnonOsa: function(nimi) {
      return TutkinnonOsa(nimi)
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

function TutkinnonOsa(nimi) {
  function tutkinnonOsaElement() {
    return S(".tutkinnon-osa .name:contains(" + nimi + ")").parent()
  }
  function saveButton() {
    return tutkinnonOsaElement().find("button:visible")
  }
  api = {
    addArviointi: function(arvosana) {
      return function() {
        api.click()
        tutkinnonOsaElement().find(".arvosanat li:contains("+arvosana+")").click()
        saveButton().click()
      }
    },
    getArvosana: function() {
      return tutkinnonOsaElement().find(".arvosana").text()
    },
    click: function() {
      tutkinnonOsaElement().click()
    },
    saveButtonIsVisible: function() {
      return saveButton().is(":visible")
    }
  }

  return api
}