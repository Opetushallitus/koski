function LukionTodistusPage() {
  return {
    isVisible: function() {
      return isElementVisible(S('.todistus.lukio'))
    }
  }
}
function AmmatillisenPerustutkinnonTodistusPage() {
  return {
    isVisible: function() {
      return isElementVisible(S('.todistus.ammatillinenperustutkinto'))
    }
  }
}

function PeruskoulunTodistusPage() {
  return {
    isVisible: function() {
      return isElementVisible(S('.todistus.peruskoulu'))
    }
  }
}