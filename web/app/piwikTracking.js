const queuePiwikMethodCall = (methodName, ...methodArgs) => {
  if (window._paq) {
    window._paq.push([methodName, ...methodArgs])
  }
}

export const trackPageView = (path) => {
  queuePiwikMethodCall('setCustomUrl', path)
  queuePiwikMethodCall('trackPageView', path)
}

export const trackEvent = (name, data) => {
  queuePiwikMethodCall('trackEvent', name, '' + data)
}

export const trackRuntimeError = (error) => {
  trackEvent('RuntimeError', JSON.stringify(error))
}
