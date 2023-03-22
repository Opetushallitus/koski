import { isElementVisible, isLoading, S } from '../util/testHelpers.js'

export function VirhePage() {
  const api = {
    isVisible: function () {
      return isElementVisible(S('.odottamaton-virhe')) && !isLoading()
    },
    teksti: function () {
      return S('body').text()
    }
  }
  return api
}
