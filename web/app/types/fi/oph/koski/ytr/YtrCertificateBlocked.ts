/**
 * YtrCertificateBlocked
 *
 * @see `fi.oph.koski.ytr.YtrCertificateBlocked`
 */
export type YtrCertificateBlocked = {
  $class: 'fi.oph.koski.ytr.YtrCertificateBlocked'
  errorReason: 'NOT_ALLOWED_BLOCKED'
  status?: 'ERROR'
}

export const YtrCertificateBlocked = (
  o: {
    errorReason?: 'NOT_ALLOWED_BLOCKED'
    status?: 'ERROR'
  } = {}
): YtrCertificateBlocked => ({
  $class: 'fi.oph.koski.ytr.YtrCertificateBlocked',
  errorReason: 'NOT_ALLOWED_BLOCKED',
  ...o
})

YtrCertificateBlocked.className =
  'fi.oph.koski.ytr.YtrCertificateBlocked' as const

export const isYtrCertificateBlocked = (a: any): a is YtrCertificateBlocked =>
  a?.$class === 'fi.oph.koski.ytr.YtrCertificateBlocked'
