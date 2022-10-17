import Bacon from 'baconjs'
import { appendQueryParams } from '../util/location'

export const downloadExcel = (params) => {
  let iframe = document.getElementById('raportti-iframe')
  if (iframe) {
    iframe.parentNode.removeChild(iframe)
  }
  iframe = document.createElement('iframe')
  iframe.id = 'raportti-iframe'
  iframe.style.display = 'none'
  document.body.appendChild(iframe)

  const resultBus = Bacon.Bus()
  let downloadTimer = null

  iframe.addEventListener('load', () => {
    let response
    try {
      response = {
        text: iframe.contentDocument.body.textContent,
        httpStatus: 400
      }
    } catch (err) {
      response = { text: 'Tuntematon virhe', httpStatus: 500 }
    }
    if (downloadTimer !== null) {
      window.clearInterval(downloadTimer)
    }
    resultBus.error(response)
    resultBus.end()
  })

  const downloadToken = 'raportti' + new Date().getTime()
  const { baseUrl, ...queryParams } = params
  const url = appendQueryParams(baseUrl, { ...queryParams, downloadToken })
  iframe.src = url

  // detect when download has started by polling a cookie set by the backend.
  // based on https://stackoverflow.com/questions/1106377/detect-when-browser-receives-file-download
  let attempts = 1200
  downloadTimer = window.setInterval(() => {
    if (document.cookie.indexOf('koskiDownloadToken=' + downloadToken) >= 0) {
      window.clearInterval(downloadTimer)
      resultBus.push(true)
      resultBus.end()
    } else {
      if (--attempts < 0) {
        window.clearInterval(downloadTimer)
        resultBus.error({ text: 'Timeout', httpStatus: 400 })
        resultBus.end()
      }
    }
  }, 1000)

  return resultBus
}
