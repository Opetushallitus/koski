function AmmatillisenPerustutkinnonTodistusPage() {
  return {
    isVisible: function() {
      return isElementVisible(S('.todistus.ammatillinenperustutkinto'))
    }
  }
}