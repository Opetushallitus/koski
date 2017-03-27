function AddOppijaPage() {
  function form() { return S('form.oppija') }
  function button() { return form().find('button') }
  function selectedOppilaitos() { return form().find('.oppilaitos .selected') }
  function selectedTutkinto() { return form().find('.tutkinto .selected') }
  var pageApi = Page(form)
  var api = {
    isVisible: function() {
      return isElementVisible(form()) && !KoskiPage().isLoading()
    },
    isEnabled: function() {
      return !button().is(':disabled')
    },
    tutkintoIsEnabled: function() {
      return S('.tutkinto input').is(':visible') && !S('.tutkinto input').is(':disabled')
    },
    enterValidDataPerusopetus: function(params) {
      params = _.merge({  oppilaitos: 'Jyväskylän normaalikoulu' }, {}, params)
      return function() {
        return api.enterData(params)()
      }
    },
    enterValidDataAmmatillinen: function(params) {
      params = _.merge({  oppilaitos: 'Stadin', tutkinto: 'Autoalan perust'}, {}, params)
      return function() {
        return api.enterData(params)()
          .then(api.selectTutkinto(params.tutkinto))
      }
    },
    enterData: function(params) {
      params = _.merge({ etunimet: 'Tero', kutsumanimi: 'Tero', sukunimi: 'Tyhjä'}, {}, params)
      return function() {
        return pageApi.setInputValue('.etunimet input', params.etunimet)()
          .then(pageApi.setInputValue('.kutsumanimi input', params.kutsumanimi))
          .then(pageApi.setInputValue('.sukunimi input', params.sukunimi))
          .then(api.selectOppilaitos(params.oppilaitos))
      }
    },
    enterTutkinto: function(name) {
      return function() {
        return pageApi.setInputValue('.tutkinto input', name)()
      }
    },
    enterOppilaitos: function(name) {
      return function() {
        return OrganisaatioHaku(form()).enter(name)
      }
    },
    selectOppilaitos: function(name) {
      return function() {
        return OrganisaatioHaku(form()).select(name)
      }
    },
    oppilaitokset: function() {
      return OrganisaatioHaku(form()).oppilaitokset()
    },
    selectTutkinto: function(name) {
      if (!name) { return wait.forAjax }
      return function() {
        return wait.until(pageApi.getInput('.tutkinto input').isVisible)()
            .then(pageApi.setInputValue('.tutkinto input', name))
            .then(wait.until(function() { return isElementVisible(selectedTutkinto()) }))
            .then(function() {triggerEvent(selectedTutkinto(), 'click')})
      }
    },
    selectAloituspäivä: function(date) {
      return pageApi.setInputValue('.aloituspaiva input', date)
    },
    submit: function() {
      triggerEvent(button(), 'click')
    },
    submitAndExpectSuccess: function(oppija, tutkinto) {
      tutkinto = tutkinto || "Autoalan perustutkinto"
      return function() {
        api.submit()
        return wait.until(function() {
          return KoskiPage().getSelectedOppija().indexOf(oppija) >= 0 &&
                 OpinnotPage().getTutkinto().indexOf(tutkinto) >= 0
        })()
      }
    },
    isErrorShown: function(field) {
      return function() {
        return isElementVisible(form().find('.error-messages .' + field))
      }
    },
    opiskeluoikeudenTyypit: function() {
      return pageApi.getInputOptions('.opiskeluoikeudentyyppi .dropdown')
    },
    selectOpiskeluoikeudenTyyppi: function(tyyppi) {
      return pageApi.setInputValue('.opiskeluoikeudentyyppi .dropdown', tyyppi)
    },
    oppimäärät: function() {
      return pageApi.getInputOptions('.oppimaara .dropdown')
    },
    selectOppimäärä: function(oppimäärä) {
      return function () {
        return wait.until(pageApi.getInput('.oppimaara').isVisible)().then(
          pageApi.setInputValue('.oppimaara .dropdown', oppimäärä)
        )
      }
    }
  }
  return api
}