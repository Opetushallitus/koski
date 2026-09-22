/**
 * Palauttaa lukion 2019 suoritustietojen tarkistusraportin (LOPS 2021).
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot`
 */
export type MassaluovutusQueryLukio2019Suoritustiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot'
  loppu: string
  osasuoritustenAikarajaus?: boolean
  type: 'lukio2019Suoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}

export const MassaluovutusQueryLukio2019Suoritustiedot = (o: {
  loppu: string
  osasuoritustenAikarajaus?: boolean
  type?: 'lukio2019Suoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}): MassaluovutusQueryLukio2019Suoritustiedot => ({
  type: 'lukio2019Suoritustiedot',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryLukio2019Suoritustiedot.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot' as const

export const isMassaluovutusQueryLukio2019Suoritustiedot = (
  a: any
): a is MassaluovutusQueryLukio2019Suoritustiedot =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot'
