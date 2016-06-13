function OpinnotPage() {

  function oppija() { return S('.oppija') }
  function tutkinnonOsa() { return S('.tutkinnon-osa') }
  function opiskeluOikeus() { return S('.opiskeluoikeus')}

  var api = {
    getTutkinto: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      var nth = S('.opiskeluoikeus .tutkinto')[index]
      return S(nth).text()
    },
    isRakenneVisible: function() {
      return S('.opiskeluoikeus .suoritus .tutkinto-rakenne').is(":visible")
    },
    getOppilaitos: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      return S(S('.oppilaitos .oppilaitos')[index]).text()
    },
    getOpintoOikeus: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      return S(S('.opiskeluoikeus .tutkinnon-tila')[index]).text()
    },
    getTutkinnonOsat: function() {
      return textsOf(tutkinnonOsa().find('.name'))
    },
    getTutkinnonOsa: function(nimi) {
      return TutkinnonOsa(nimi)
    },
    selectSuoritustapa: function(suoritustapa) {
      return function() {
        return Page(opiskeluOikeus).setInputValue(".suoritustapa", suoritustapa)().then(wait.forAjax)
      }
    },
    isSuoritustapaSelectable: function() {
      return isElementVisible(S(".suoritustapa"))
    },
    selectOsaamisala: function(osaamisala) {
      return function() {
        return Page(opiskeluOikeus).setInputValue(".osaamisala", osaamisala)().then(wait.forAjax)
      }
    },
    isOsaamisalaSelectable: function() {
      return isElementVisible(S(".osaamisala"))
    },
    avaaOpintosuoritusote: function (index) {
      return function() {
        triggerEvent(S('li.oppilaitos:nth-child('+index+') a.opintosuoritusote'), 'click')
        return wait.until(OpintosuoritusotePage().isVisible)()
      }
    },
    avaaTodistus: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      return function() {
        triggerEvent(S(S('a.todistus')[index]), 'click')
        return wait.until(TodistusPage().isVisible)()
      }
    },
    waitUntilRakenneVisible: function() {
      return wait.until(api.isRakenneVisible)
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
        triggerEvent(tutkinnonOsaElement().find(".arvosanat li:contains(" + arvosana + ")"), "click")
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