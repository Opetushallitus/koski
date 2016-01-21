function AddOppijaPage() {
  function form() { return S('form.oppija') }
  function button() { return form().find('button') }
  function selectedOppilaitos() { return form().find('.oppilaitos .selected') }
  function selectedTutkinto() { return form().find('.tutkinto .selected') }
  var pageApi = Page(form)
  var api = {
    isVisible: function() {
      return isElementVisible(form()) && !TorPage().isLoading()
    },
    isEnabled: function() {
      return !button().is(':disabled')
    },
    tutkintoIsEnabled: function() {
      return !S('.tutkinto input').is(':disabled')
    },
    enterValidData: function(params) {
      var self = this
      params = _.merge({ etunimet: 'Ossi Olavi', kutsumanimi: 'Ossi', sukunimi: 'Oppija', hetu: '300994-9694', oppilaitos: 'Helsingin', tutkinto: 'Autoalan perust'}, {}, params)
      return function() {
        return pageApi.setInputValue('.etunimet input', params.etunimet)()
          .then(pageApi.setInputValue('.kutsumanimi input', params.kutsumanimi))
          .then(pageApi.setInputValue('.sukunimi input', params.sukunimi))
          .then(pageApi.setInputValue('.hetu input', params.hetu))
          .then(self.selectOppilaitos(params.oppilaitos))
          .then(self.selectTutkinto(params.tutkinto))
      }
    },
    enterTutkinto: function(name) {
      return function() {
        return pageApi.setInputValue('.tutkinto input', name)()
      }
    },
    enterOppilaitos: function(name) {
      return function() {
        return pageApi.setInputValue('.oppilaitos input', name)().then(wait.forAjax)
      }
    },
    selectOppilaitos: function(name) {
      return function() {
        return pageApi.setInputValue('.oppilaitos input', name)()
          .then(wait.until(function() { return isElementVisible(selectedOppilaitos()) }))
          .then(function() {triggerEvent(selectedOppilaitos(), 'click')})
      }
    },
    oppilaitokset: function() {
      return textsOf(form().find('.oppilaitos .results li'))
    },
    selectTutkinto: function(name) {
      return function() {
        return pageApi.setInputValue('.tutkinto input', name)()
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
          return TorPage().getSelectedOppija().indexOf(oppija) >= 0 && (OpinnotPage().getTutkinto().indexOf(tutkinto) >= 0)
        })()
      }
    },
    isErrorShown: function(field) {
      return function() {
        return isElementVisible(form().find('.error-messages .' + field))
      }
    }
  }
  return api
}