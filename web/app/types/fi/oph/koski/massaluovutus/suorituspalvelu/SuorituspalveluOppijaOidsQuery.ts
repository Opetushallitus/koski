/**
 * Massaluovutusrajapinnan kysely suorituspalvelulle.
 * Palauttaa Suorituspalvelua varten räätälöidyt tiedot annettujen oppijoiden opiskeluoikeuksista.
 * Vastauksen skeema on taulukko <a href="/koski/json-schema-viewer/?schema=suorituspalvelu-result.json">SupaResponse</a>-objekteja.
 *
 * @see `fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluOppijaOidsQuery`
 */
export type SuorituspalveluOppijaOidsQuery = {
  $class: 'fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluOppijaOidsQuery'
  type: 'supa-oppijat'
  format: 'application/json'
  oppijaOids: Array<string>
}

export const SuorituspalveluOppijaOidsQuery = (
  o: {
    type?: 'supa-oppijat'
    format?: 'application/json'
    oppijaOids?: Array<string>
  } = {}
): SuorituspalveluOppijaOidsQuery => ({
  $class:
    'fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluOppijaOidsQuery',
  type: 'supa-oppijat',
  format: 'application/json',
  oppijaOids: [],
  ...o
})

SuorituspalveluOppijaOidsQuery.className =
  'fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluOppijaOidsQuery' as const

export const isSuorituspalveluOppijaOidsQuery = (
  a: any
): a is SuorituspalveluOppijaOidsQuery =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluOppijaOidsQuery'
