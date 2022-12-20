/**
 * Arvioitsijan/arvioitsijoiden tiedot
 *
 * @see `fi.oph.koski.schema.NäytönArvioitsija`
 */
export type NäytönArvioitsija = {
  $class: 'fi.oph.koski.schema.NäytönArvioitsija'
  nimi: string
  ntm?: boolean
}

export const NäytönArvioitsija = (o: {
  nimi: string
  ntm?: boolean
}): NäytönArvioitsija => ({
  $class: 'fi.oph.koski.schema.NäytönArvioitsija',
  ...o
})

export const isNäytönArvioitsija = (a: any): a is NäytönArvioitsija =>
  a?.$class === 'fi.oph.koski.schema.NäytönArvioitsija'
