function AddOppijaPage() {
  function form() { return S("form.oppija") }
  function button() { return form().find("button") }
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
      }
    },
    submit: function() {
      triggerEvent(button(), 'click')
    }
  }
  return api
}