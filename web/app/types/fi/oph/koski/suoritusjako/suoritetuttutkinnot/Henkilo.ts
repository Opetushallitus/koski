/**
 * Henkilo
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Henkilo`
 */
export type Henkilo = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Henkilo'
  sukunimi: string
  oid: string
  syntymäaika?: string
  kutsumanimi: string
  etunimet: string
}

export const Henkilo = (o: {
  sukunimi: string
  oid: string
  syntymäaika?: string
  kutsumanimi: string
  etunimet: string
}): Henkilo => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Henkilo',
  ...o
})

Henkilo.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Henkilo' as const

export const isHenkilo = (a: any): a is Henkilo =>
  a?.$class === 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Henkilo'
