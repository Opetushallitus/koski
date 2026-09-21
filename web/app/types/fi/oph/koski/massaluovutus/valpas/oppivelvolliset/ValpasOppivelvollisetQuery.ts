/**
 * Palauttaa kaikki kunnan oppijat, jotka ovat oppivelvollisuuden piirissä.
 * HUOM! Oppijan asuinkunta voi olla eri kuin oppijan virallinen kotikunta. Tuloksissa eivät näy henkilöt, joilla on turvakielto, tai henkilöt, joista ei ole mitään tietoja tallennettuna Opintopolun palveluihin.
 *
 * @see `fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery`
 */
export type ValpasOppivelvollisetQuery = {
  $class: 'fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery'
  type: 'oppivelvolliset'
  format: 'application/json'
  kuntaOid: string
}

export const ValpasOppivelvollisetQuery = (o: {
  type?: 'oppivelvolliset'
  format?: 'application/json'
  kuntaOid: string
}): ValpasOppivelvollisetQuery => ({
  $class:
    'fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery',
  type: 'oppivelvolliset',
  format: 'application/json',
  ...o
})

ValpasOppivelvollisetQuery.className =
  'fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery' as const

export const isValpasOppivelvollisetQuery = (
  a: any
): a is ValpasOppivelvollisetQuery =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery'
