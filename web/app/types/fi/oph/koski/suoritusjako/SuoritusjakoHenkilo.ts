/**
 * SuoritusjakoHenkilö
 *
 * @see `fi.oph.koski.suoritusjako.SuoritusjakoHenkilö`
 */
export type SuoritusjakoHenkilö = {
  $class: 'fi.oph.koski.suoritusjako.SuoritusjakoHenkilö'
  sukunimi: string
  oid: string
  syntymäaika?: string
  kutsumanimi: string
  etunimet: string
}

export const SuoritusjakoHenkilö = (o: {
  sukunimi: string
  oid: string
  syntymäaika?: string
  kutsumanimi: string
  etunimet: string
}): SuoritusjakoHenkilö => ({
  $class: 'fi.oph.koski.suoritusjako.SuoritusjakoHenkilö',
  ...o
})

SuoritusjakoHenkilö.className =
  'fi.oph.koski.suoritusjako.SuoritusjakoHenkilö' as const

export const isSuoritusjakoHenkilö = (a: any): a is SuoritusjakoHenkilö =>
  a?.$class === 'fi.oph.koski.suoritusjako.SuoritusjakoHenkilö'
