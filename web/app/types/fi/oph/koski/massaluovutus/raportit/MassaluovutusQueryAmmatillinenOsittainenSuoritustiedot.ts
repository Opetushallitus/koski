/**
 * Palauttaa ammatillisen osittaisen tutkinnon suoritustietojen tarkistusraportin.
 * Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot`
 */
export type MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  osasuoritustenAikarajaus?: boolean
  type: 'ammatillinenOsittainenSuoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  osasuoritustenAikarajaus?: boolean
  type?: 'ammatillinenOsittainenSuoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot => ({
  type: 'ammatillinenOsittainenSuoritustiedot',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot' as const

export const isMassaluovutusQueryAmmatillinenOsittainenSuoritustiedot = (
  a: any
): a is MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot'
