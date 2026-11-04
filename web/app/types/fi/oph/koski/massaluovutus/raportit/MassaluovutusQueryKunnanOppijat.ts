/**
 * Palauttaa oppilaskohtaisen listan kunnan perusopetusikäisistä asukkaista, joiden opetuksen järjestäjä on valittuna päivänä muu kuin kunta itse. Kysely vaatii kunnassa oikeudet sekä perusopetuksen että esiopetuksen opiskeluoikeuksiin ja luottamuksellisiin tietoihin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKunnanOppijat`
 */
export type MassaluovutusQueryKunnanOppijat = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKunnanOppijat'
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type: 'kunnanOppijat'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryKunnanOppijat = (o: {
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'kunnanOppijat'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryKunnanOppijat => ({
  type: 'kunnanOppijat',
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKunnanOppijat',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryKunnanOppijat.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKunnanOppijat' as const

export const isMassaluovutusQueryKunnanOppijat = (
  a: any
): a is MassaluovutusQueryKunnanOppijat =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKunnanOppijat'
