/**
 * Oppivelvollisen tukea koskevat päätöstiedot
 *
 * @see `fi.oph.koski.schema.TukeaKoskevaPäätös`
 */
export type TukeaKoskevaPäätös = {
  $class: 'fi.oph.koski.schema.TukeaKoskevaPäätös'
  alku?: string
  loppu?: string
}

export const TukeaKoskevaPäätös = (
  o: {
    alku?: string
    loppu?: string
  } = {}
): TukeaKoskevaPäätös => ({
  $class: 'fi.oph.koski.schema.TukeaKoskevaPäätös',
  ...o
})

TukeaKoskevaPäätös.className = 'fi.oph.koski.schema.TukeaKoskevaPäätös' as const

export const isTukeaKoskevaPäätös = (a: any): a is TukeaKoskevaPäätös =>
  a?.$class === 'fi.oph.koski.schema.TukeaKoskevaPäätös'
