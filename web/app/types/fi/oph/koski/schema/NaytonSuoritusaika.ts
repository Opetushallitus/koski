/**
 * Näyttötilaisuuden ajankohta
 *
 * @see `fi.oph.koski.schema.NäytönSuoritusaika`
 */
export type NäytönSuoritusaika = {
  $class: 'fi.oph.koski.schema.NäytönSuoritusaika'
  alku: string
  loppu: string
}

export const NäytönSuoritusaika = (o: {
  alku: string
  loppu: string
}): NäytönSuoritusaika => ({
  $class: 'fi.oph.koski.schema.NäytönSuoritusaika',
  ...o
})

NäytönSuoritusaika.className = 'fi.oph.koski.schema.NäytönSuoritusaika' as const

export const isNäytönSuoritusaika = (a: any): a is NäytönSuoritusaika =>
  a?.$class === 'fi.oph.koski.schema.NäytönSuoritusaika'
