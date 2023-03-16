/**
 * YtrCertificateCompleted
 *
 * @see `fi.oph.koski.ytr.YtrCertificateCompleted`
 */
export type YtrCertificateCompleted = {
  $class: 'fi.oph.koski.ytr.YtrCertificateCompleted'
  status: 'COMPLETED'
  requestedTime: string
  completionTime: string
  certificateUrl: string
}

export const YtrCertificateCompleted = (o: {
  status?: 'COMPLETED'
  requestedTime: string
  completionTime: string
  certificateUrl: string
}): YtrCertificateCompleted => ({
  $class: 'fi.oph.koski.ytr.YtrCertificateCompleted',
  status: 'COMPLETED',
  ...o
})

YtrCertificateCompleted.className =
  'fi.oph.koski.ytr.YtrCertificateCompleted' as const

export const isYtrCertificateCompleted = (
  a: any
): a is YtrCertificateCompleted =>
  a?.$class === 'fi.oph.koski.ytr.YtrCertificateCompleted'
