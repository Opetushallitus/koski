import R from 'ramda'

const queuePiwikMethodCall = (methodName, ...methodArgs) => {
  if (window._paq) {
    console.log('queuePiwikMethodCall', methodName, methodArgs[0])
    window._paq.push([methodName, ...methodArgs])
  } else {
    console.log('queuePiwikMethodCall: no window._paq')
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
  trackEvent('RuntimeError', JSON.stringify(R.dissoc('jsonMessage', error)))
}
