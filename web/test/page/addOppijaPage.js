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
      params = _.merge({ etunimet: 'Tero', kutsumanimi: 'Tero', sukunimi: 'Tyhjä', hetu: '230872-7258'}, {}, params)
      return function() {
        return pageApi.setInputValue('.etunimet input', params.etunimet)()
          .then(pageApi.setInputValue('.kutsumanimi input', params.kutsumanimi))
          .then(pageApi.setInputValue('.sukunimi input', params.sukunimi))
          .then(pageApi.setInputValue('.hetu input', params.hetu))
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
        return pageApi.setInputValue('.oppilaitos input', name)()
      }
    },
    selectOppilaitos: function(name) {
      if (!name) { return wait.forAjax }
      return function() {
        return pageApi.setInputValue('.oppilaitos input', name)()
          .then(wait.until(function() { return isElementVisible(selectedOppilaitos()) }))
          .then(function() {triggerEvent(selectedOppilaitos(), 'click')})
          .then(wait.forAjax)
      }
    },
    oppilaitokset: function() {
      return textsOf(form().find('.oppilaitos .results li'))
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
      return textsOf(S('.opiskeluoikeudentyyppi select option'))
    },
    selectOpiskeluoikeudenTyyppi: function(tyyppi) {
      return pageApi.setInputValue('.opiskeluoikeudentyyppi select', tyyppi)
    }
  }
  return api
}