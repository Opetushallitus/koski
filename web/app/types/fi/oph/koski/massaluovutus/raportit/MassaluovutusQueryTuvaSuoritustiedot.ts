/**
 * Palauttaa TUVA-koulutuksen suoritustietojen tarkistusraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot`
 */
export type MassaluovutusQueryTuvaSuoritustiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'tuvaSuoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryTuvaSuoritustiedot = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'tuvaSuoritustiedot'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryTuvaSuoritustiedot => ({
  type: 'tuvaSuoritustiedot',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryTuvaSuoritustiedot.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot' as const

export const isMassaluovutusQueryTuvaSuoritustiedot = (
  a: any
): a is MassaluovutusQueryTuvaSuoritustiedot =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot'
