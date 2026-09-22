/**
 * Palauttaa kaikki kunnan oppivelvolliset oppijat, joilla ei ole tällä hetkellä aktiivista oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta KOSKI-tietovarannossa.
 * HUOM! Oppijan asuinkunta voi olla eri kuin oppijan virallinen kotikunta. Tuloksissa eivät näy henkilöt, joilla on turvakielto, tai henkilöt, joista ei ole mitään tietoja tallennettuna Opintopolun palveluihin.
 *
 * @see `fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQuery`
 */
export type ValpasEiOppivelvollisuuttaSuorittavatQuery = {
  $class: 'fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQuery'
  type: 'eiSuoritaOppivelvollisuutta'
  format: 'application/json'
  kuntaOid: string
  vainAktiivisetKuntailmoitukset: boolean
}

export const ValpasEiOppivelvollisuuttaSuorittavatQuery = (o: {
  type?: 'eiSuoritaOppivelvollisuutta'
  format?: 'application/json'
  kuntaOid: string
  vainAktiivisetKuntailmoitukset?: boolean
}): ValpasEiOppivelvollisuuttaSuorittavatQuery => ({
  $class:
    'fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQuery',
  type: 'eiSuoritaOppivelvollisuutta',
  format: 'application/json',
  vainAktiivisetKuntailmoitukset: false,
  ...o
})

ValpasEiOppivelvollisuuttaSuorittavatQuery.className =
  'fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQuery' as const

export const isValpasEiOppivelvollisuuttaSuorittavatQuery = (
  a: any
): a is ValpasEiOppivelvollisuuttaSuorittavatQuery =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQuery'
