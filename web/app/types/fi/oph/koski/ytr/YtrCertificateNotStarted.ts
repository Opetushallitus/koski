/**
 * YtrCertificateNotStarted
 *
 * @see `fi.oph.koski.ytr.YtrCertificateNotStarted`
 */
export type YtrCertificateNotStarted = {
  $class: 'fi.oph.koski.ytr.YtrCertificateNotStarted'
  status: 'NOT_STARTED'
}

export const YtrCertificateNotStarted = (
  o: {
    status?: 'NOT_STARTED'
  } = {}
): YtrCertificateNotStarted => ({
  $class: 'fi.oph.koski.ytr.YtrCertificateNotStarted',
  status: 'NOT_STARTED',
  ...o
})

YtrCertificateNotStarted.className =
  'fi.oph.koski.ytr.YtrCertificateNotStarted' as const

export const isYtrCertificateNotStarted = (
  a: any
): a is YtrCertificateNotStarted =>
  a?.$class === 'fi.oph.koski.ytr.YtrCertificateNotStarted'
