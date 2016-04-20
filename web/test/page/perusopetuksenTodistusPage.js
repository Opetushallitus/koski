function PerusopetuksenTodistusPage() {
  return {
    isVisible: function() {
      return isElementVisible(S('.todistus.perusopetus'))
    }
  }
}