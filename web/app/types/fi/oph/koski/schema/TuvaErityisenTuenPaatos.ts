/**
 * TUVA-opiskeluoikeuden erityisen tuen päätöstiedot
 *
 * @see `fi.oph.koski.schema.TuvaErityisenTuenPäätös`
 */
export type TuvaErityisenTuenPäätös = {
  $class: 'fi.oph.koski.schema.TuvaErityisenTuenPäätös'
  alku?: string
  loppu?: string
}

export const TuvaErityisenTuenPäätös = (
  o: {
    alku?: string
    loppu?: string
  } = {}
): TuvaErityisenTuenPäätös => ({
  $class: 'fi.oph.koski.schema.TuvaErityisenTuenPäätös',
  ...o
})

export const isTuvaErityisenTuenPäätös = (
  a: any
): a is TuvaErityisenTuenPäätös =>
  a?.$class === 'fi.oph.koski.schema.TuvaErityisenTuenPäätös'
