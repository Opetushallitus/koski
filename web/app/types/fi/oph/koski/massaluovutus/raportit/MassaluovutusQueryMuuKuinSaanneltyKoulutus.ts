/**
 * Palauttaa muun kuin säännellyn koulutuksen (MUKS) raportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus`
 */
export type MassaluovutusQueryMuuKuinSaanneltyKoulutus = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'muuKuinSaanneltyKoulutus'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryMuuKuinSaanneltyKoulutus = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'muuKuinSaanneltyKoulutus'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryMuuKuinSaanneltyKoulutus => ({
  type: 'muuKuinSaanneltyKoulutus',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryMuuKuinSaanneltyKoulutus.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus' as const

export const isMassaluovutusQueryMuuKuinSaanneltyKoulutus = (
  a: any
): a is MassaluovutusQueryMuuKuinSaanneltyKoulutus =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus'
