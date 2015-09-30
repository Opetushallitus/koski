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
    enterValidData: function() {
      return pageApi.setInputValue(".etunimet input", "Ossi Olavi")()
        .then(pageApi.setInputValue(".kutsumanimi input", "Ossi"))
        .then(pageApi.setInputValue(".sukunimi input", "Oppija"))
        .then(pageApi.setInputValue(".hetu input", "300994-9694"))
    },
    submit: function() {
      triggerEvent(button(), 'click')
    }
  }
  return api
}