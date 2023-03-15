/**
 * YtrCertificateServiceUnavailable
 *
 * @see `fi.oph.koski.ytr.YtrCertificateServiceUnavailable`
 */
export type YtrCertificateServiceUnavailable = {
  $class: 'fi.oph.koski.ytr.YtrCertificateServiceUnavailable'
  requestedTime: string
  errorReason: 'SERVICE_UNAVAILABLE'
  status?: 'ERROR'
}

export const YtrCertificateServiceUnavailable = (o: {
  requestedTime: string
  errorReason?: 'SERVICE_UNAVAILABLE'
  status?: 'ERROR'
}): YtrCertificateServiceUnavailable => ({
  $class: 'fi.oph.koski.ytr.YtrCertificateServiceUnavailable',
  errorReason: 'SERVICE_UNAVAILABLE',
  ...o
})

YtrCertificateServiceUnavailable.className =
  'fi.oph.koski.ytr.YtrCertificateServiceUnavailable' as const

export const isYtrCertificateServiceUnavailable = (
  a: any
): a is YtrCertificateServiceUnavailable =>
  a?.$class === 'fi.oph.koski.ytr.YtrCertificateServiceUnavailable'
