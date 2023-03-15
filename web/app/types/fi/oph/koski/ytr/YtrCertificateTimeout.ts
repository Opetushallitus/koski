/**
 * YtrCertificateTimeout
 *
 * @see `fi.oph.koski.ytr.YtrCertificateTimeout`
 */
export type YtrCertificateTimeout = {
  $class: 'fi.oph.koski.ytr.YtrCertificateTimeout'
  requestedTime: string
  errorReason: 'TIMEOUT'
  status?: 'ERROR'
}

export const YtrCertificateTimeout = (o: {
  requestedTime: string
  errorReason?: 'TIMEOUT'
  status?: 'ERROR'
}): YtrCertificateTimeout => ({
  $class: 'fi.oph.koski.ytr.YtrCertificateTimeout',
  errorReason: 'TIMEOUT',
  ...o
})

YtrCertificateTimeout.className =
  'fi.oph.koski.ytr.YtrCertificateTimeout' as const

export const isYtrCertificateTimeout = (a: any): a is YtrCertificateTimeout =>
  a?.$class === 'fi.oph.koski.ytr.YtrCertificateTimeout'
