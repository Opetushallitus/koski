/**
 * YtrCertificateOldExamination
 *
 * @see `fi.oph.koski.ytr.YtrCertificateOldExamination`
 */
export type YtrCertificateOldExamination = {
  $class: 'fi.oph.koski.ytr.YtrCertificateOldExamination'
  errorReason: 'NOT_ALLOWED_OLD_EXAMINATION'
  status?: 'ERROR'
}

export const YtrCertificateOldExamination = (
  o: {
    errorReason?: 'NOT_ALLOWED_OLD_EXAMINATION'
    status?: 'ERROR'
  } = {}
): YtrCertificateOldExamination => ({
  $class: 'fi.oph.koski.ytr.YtrCertificateOldExamination',
  errorReason: 'NOT_ALLOWED_OLD_EXAMINATION',
  ...o
})

YtrCertificateOldExamination.className =
  'fi.oph.koski.ytr.YtrCertificateOldExamination' as const

export const isYtrCertificateOldExamination = (
  a: any
): a is YtrCertificateOldExamination =>
  a?.$class === 'fi.oph.koski.ytr.YtrCertificateOldExamination'
