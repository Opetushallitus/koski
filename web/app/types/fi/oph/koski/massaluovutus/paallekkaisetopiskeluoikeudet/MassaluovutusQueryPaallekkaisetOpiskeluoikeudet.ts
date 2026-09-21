/**
 * Palauttaa hakuehtojen mukaiset organisaation ja sen alaorganisaatioiden päällekkäiset opiskeluoikeudet.
 * Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa, mutta se on mahdollista ladata myös paremmin koneluettavassa csv-muodossa.
 *
 * @see `fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet`
 */
export type MassaluovutusQueryPaallekkaisetOpiskeluoikeudet = {
  $class: 'fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'paallekkaisetOpiskeluoikeudet'
  alku: string
  password?: string
  organisaatioOid?: string
  format:
    | 'text/csv'
    | 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryPaallekkaisetOpiskeluoikeudet = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'paallekkaisetOpiskeluoikeudet'
  alku: string
  password?: string
  organisaatioOid?: string
  format:
    | 'text/csv'
    | 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryPaallekkaisetOpiskeluoikeudet => ({
  type: 'paallekkaisetOpiskeluoikeudet',
  $class:
    'fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet',
  ...o
})

MassaluovutusQueryPaallekkaisetOpiskeluoikeudet.className =
  'fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet' as const

export const isMassaluovutusQueryPaallekkaisetOpiskeluoikeudet = (
  a: any
): a is MassaluovutusQueryPaallekkaisetOpiskeluoikeudet =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet'
