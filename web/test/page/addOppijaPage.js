function AddOppijaPage() {
  function form() { return S('form.oppija') }
  function button() { return form().find('button') }
  function oppilaitos() { return form().find('.oppilaitos .selected') }
  function tutkinto() { return form().find('.tutkinto .selected') }
  var pageApi = Page(form)
  var api = {
    isVisible: function() {
      return form().is(':visible')
    },
    isEnabled: function() {
      return !button().is(':disabled')
    },
    tutkintoIsEnabled: function() {
      return !S('.tutkinto input').is(':disabled')
    },
    enterValidData: function(params) {
      var self = this
      params = _.merge({ etunimet: 'Ossi Olavi', kutsumanimi: 'Ossi', sukunimi: 'Oppija', hetu: '300994-9694', oppilaitos: 'Helsingin', tutkinto: 'auto'}, {}, params)
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
        return pageApi.setInputValue('.oppilaitos input', name)()
      }
    },
    selectOppilaitos: function(name) {
      return function() {
        return pageApi.setInputValue('.oppilaitos input', name)()
          .then(wait.until(function() { return oppilaitos().is(':visible') }))
          .then(function() {triggerEvent(oppilaitos(), 'click')})
      }
    },
    selectTutkinto: function(name) {
      return function() {
        return pageApi.setInputValue('.tutkinto input', name)()
          .then(wait.until(function() { return tutkinto().is(':visible') }))
          .then(function() {triggerEvent(tutkinto(), 'click')})
      }
    },
    submit: function() {
      triggerEvent(button(), 'click')
    },
    isErrorShown: function(field) {
      return function() {
        return form().find('.error-messages .' + field).is(':visible')
      }
    }
  }
  return api
}