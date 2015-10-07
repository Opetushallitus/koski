function AddOppijaPage() {
  function form() { return S("form.oppija") }
  function button() { return form().find("button") }
  function oppilaitos() { return form().find('.oppilaitos .selected') }
  function tutkinto() { return form().find('.tutkinto .selected') }
  var pageApi = Page(form);
  var api = {
    isVisible: function() {
      return form().is(":visible")
    },
    isEnabled: function() {
      return !button().is(":disabled")
    },
    enterValidData: function(params) {
      params = _.merge({ etunimet: "Ossi Olavi", kutsumanimi: "Ossi", sukunimi: "Oppija", hetu: "300994-9694"}, {}, params)
      return function() {
        return pageApi.setInputValue(".etunimet input", params.etunimet)()
          .then(pageApi.setInputValue(".kutsumanimi input", params.kutsumanimi))
          .then(pageApi.setInputValue(".sukunimi input", params.sukunimi))
          .then(pageApi.setInputValue(".hetu input", params.hetu))
          .then(pageApi.setInputValue(".oppilaitos input", "Helsingin"))
          .then(wait.until(function() { return oppilaitos().is(":visible") }))
          .then(function() {triggerEvent(oppilaitos(), 'click')})
          .then(pageApi.setInputValue(".tutkinto input", "auto"))
          .then(wait.until(function() { return tutkinto().is(":visible") }))
          .then(function() {triggerEvent(tutkinto(), 'click')})
      }
    },
    submit: function() {
      triggerEvent(button(), 'click')
    },
    isErrorShown: function(field) {
      return function() {
        return form().find(".error-messages ." + field).is(":visible")
      }
    }
  }
  return api
}