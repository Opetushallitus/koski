function VirhePage() {
  var api = {
    isVisible: function () {
      return isElementVisible(S('.odottamaton-virhe')) && !isLoading()
    },
    teksti: function () {
      return S('body').text()
    }
  }
  return api
}
