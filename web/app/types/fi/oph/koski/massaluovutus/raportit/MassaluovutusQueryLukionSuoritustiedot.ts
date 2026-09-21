/**
 * Palauttaa lukion suoritustietojen tarkistusraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot`
 */
export type MassaluovutusQueryLukionSuoritustiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot'
  loppu: string
  osasuoritustenAikarajaus?: boolean
  type: 'lukionSuoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}

export const MassaluovutusQueryLukionSuoritustiedot = (o: {
  loppu: string
  osasuoritustenAikarajaus?: boolean
  type?: 'lukionSuoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}): MassaluovutusQueryLukionSuoritustiedot => ({
  type: 'lukionSuoritustiedot',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryLukionSuoritustiedot.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot' as const

export const isMassaluovutusQueryLukionSuoritustiedot = (
  a: any
): a is MassaluovutusQueryLukionSuoritustiedot =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot'
