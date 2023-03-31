/**
 * YtrCertificateInternalError
 *
 * @see `fi.oph.koski.ytr.YtrCertificateInternalError`
 */
export type YtrCertificateInternalError = {
  $class: 'fi.oph.koski.ytr.YtrCertificateInternalError'
  requestedTime: string
  errorReason: 'INTERNAL_ERROR'
  status?: 'ERROR'
}

export const YtrCertificateInternalError = (o: {
  requestedTime: string
  errorReason?: 'INTERNAL_ERROR'
  status?: 'ERROR'
}): YtrCertificateInternalError => ({
  $class: 'fi.oph.koski.ytr.YtrCertificateInternalError',
  errorReason: 'INTERNAL_ERROR',
  ...o
})

YtrCertificateInternalError.className =
  'fi.oph.koski.ytr.YtrCertificateInternalError' as const

export const isYtrCertificateInternalError = (
  a: any
): a is YtrCertificateInternalError =>
  a?.$class === 'fi.oph.koski.ytr.YtrCertificateInternalError'
