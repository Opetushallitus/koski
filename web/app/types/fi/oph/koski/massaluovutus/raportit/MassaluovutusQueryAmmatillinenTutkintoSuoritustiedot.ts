/**
 * Palauttaa ammatillisen tutkinnon suoritustietojen tarkistusraportin.
 * Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot`
 */
export type MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  osasuoritustenAikarajaus?: boolean
  type: 'ammatillinenTutkintoSuoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  osasuoritustenAikarajaus?: boolean
  type?: 'ammatillinenTutkintoSuoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot => ({
  type: 'ammatillinenTutkintoSuoritustiedot',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot' as const

export const isMassaluovutusQueryAmmatillinenTutkintoSuoritustiedot = (
  a: any
): a is MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot'
