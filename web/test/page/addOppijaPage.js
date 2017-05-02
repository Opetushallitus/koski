function AddOppijaPage() {
  function form() { return S('form.uusi-oppija') }
  function button() { return form().find('button') }
  function selectedOppilaitos() { return form().find('.oppilaitos .selected') }
  function selectedTutkinto() { return form().find('.tutkinto .selected') }
  var pageApi = Page(form)
  var api = {
    isVisible: function() {
      return isElementVisible(form()) && !KoskiPage().isLoading()
    },
    isEnabled: function() {
      return button().is(":visible") && !button().is(':disabled')
    },
    tutkintoIsEnabled: function() {
      return S('.tutkinto input').is(':visible') && !S('.tutkinto input').is(':disabled')
    },
    enterValidDataPerusopetus: function(params) {
      params = _.merge({  oppilaitos: 'Jyväskylän normaalikoulu' }, {}, params)
      return function() {
        return api.enterData(params)().then(wait.forAjax)
      }
    },
    enterHenkilötiedot: function(params) {
      params = _.merge({ etunimet: 'Tero', kutsumanimi: 'Tero', sukunimi: 'Tyhjä'}, {}, params)
      return function() {
        return pageApi.setInputValue('.etunimet input', params.etunimet)()
          .then(pageApi.setInputValue('.kutsumanimi input', params.kutsumanimi))
          .then(pageApi.setInputValue('.sukunimi input', params.sukunimi))
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
      return function() {
        return api.enterHenkilötiedot(params)().then(api.selectOppilaitos(params.oppilaitos))
      }
    },
    enterTutkinto: function(name) {
      return function() {
        return pageApi.setInputValue('.tutkinto input', name)()
      }
    },
    enterOppilaitos: function(name) {
      return OrganisaatioHaku(form).enter(name)
    },
    selectOppilaitos: function(name) {
      return OrganisaatioHaku(form).select(name)
    },
    oppilaitokset: OrganisaatioHaku(form).oppilaitokset,
    oppilaitos: OrganisaatioHaku(form).oppilaitos,
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
    henkilötiedot: function() {
      return ['.etunimet', '.kutsumanimi', '.sukunimi'].map(function(selector) {
        return pageApi.getInputValue(selector + ' input')
      })
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
                 OpinnotPage().suoritusOnValittu(1, tutkinto)
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
    opiskeluoikeudenTyyppi: function() {
      return pageApi.getInputValue('.opiskeluoikeudentyyppi input')
    },
    
    selectOpiskeluoikeudenTyyppi: function(tyyppi) {
      return pageApi.setInputValue('.opiskeluoikeudentyyppi .dropdown', tyyppi)
    },
    oppimäärät: function() {
      return pageApi.getInputOptions('.oppimaara .dropdown')
    },
    selectOppimäärä: function(oppimäärä) {
      return function () {
        return wait.until(pageApi.getInput('.oppimaara .dropdown').isVisible)().then(
          pageApi.setInputValue('.oppimaara .dropdown', oppimäärä)
        )
      }
    },
    selectOppiaine: function(oppiaine) {
      return function () {
        return wait.until(pageApi.getInput('.oppiaine .dropdown').isVisible)().then(
          pageApi.setInputValue('.oppiaine .dropdown', oppiaine)
        )
      }
    },
    goBack: function() {
      triggerEvent(S('h1 a'), 'click')
    }
  }
  return api
}