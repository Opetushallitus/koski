/**
 * IB-tutkintokerta (examination session), joita on vuosittain kaksi: toukokuu ja marraskuu
 *
 * @see `fi.oph.koski.schema.IBExaminationSession`
 */
export type IBExaminationSession = {
  $class: 'fi.oph.koski.schema.IBExaminationSession'
  year: number
  month: number
}

export const IBExaminationSession = (o: {
  year: number
  month: number
}): IBExaminationSession => ({
  $class: 'fi.oph.koski.schema.IBExaminationSession',
  ...o
})

IBExaminationSession.className =
  'fi.oph.koski.schema.IBExaminationSession' as const

export const isIBExaminationSession = (a: any): a is IBExaminationSession =>
  a?.$class === 'fi.oph.koski.schema.IBExaminationSession'
