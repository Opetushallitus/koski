function PeruskoulunTodistusPage() {
  return {
    isVisible: function() {
      return isElementVisible(S('.todistus.peruskoulu'))
    }
  }
}