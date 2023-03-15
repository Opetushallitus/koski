/**
 * YtrCertificateInProgress
 *
 * @see `fi.oph.koski.ytr.YtrCertificateInProgress`
 */
export type YtrCertificateInProgress = {
  $class: 'fi.oph.koski.ytr.YtrCertificateInProgress'
  status: 'IN_PROGRESS'
  requestedTime: string
}

export const YtrCertificateInProgress = (o: {
  status?: 'IN_PROGRESS'
  requestedTime: string
}): YtrCertificateInProgress => ({
  $class: 'fi.oph.koski.ytr.YtrCertificateInProgress',
  status: 'IN_PROGRESS',
  ...o
})

YtrCertificateInProgress.className =
  'fi.oph.koski.ytr.YtrCertificateInProgress' as const

export const isYtrCertificateInProgress = (
  a: any
): a is YtrCertificateInProgress =>
  a?.$class === 'fi.oph.koski.ytr.YtrCertificateInProgress'
