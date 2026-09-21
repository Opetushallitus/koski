/**
 * Massaluovutusrajapinnan kysely suorituspalvelulle.
 * Palauttaa Suorituspalvelua varten räätälöidyt tiedot annettujen oppijoiden ja koulutusmuodon mukaisista opiskeluoikeuksista tietyn ajanhetken jälkeen.
 * Vastauksen skeema on taulukko <a href="/koski/json-schema-viewer/?schema=suorituspalvelu-result.json">SupaResponse</a>-objekteja.
 *
 * @see `fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluMuuttuneetJalkeenQuery`
 */
export type SuorituspalveluMuuttuneetJalkeenQuery = {
  $class: 'fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluMuuttuneetJalkeenQuery'
  type: 'supa-muuttuneet'
  format: 'application/json'
  muuttuneetJälkeen: string
  muuttuneetEnnen?: string
}

export const SuorituspalveluMuuttuneetJalkeenQuery = (o: {
  type?: 'supa-muuttuneet'
  format?: 'application/json'
  muuttuneetJälkeen: string
  muuttuneetEnnen?: string
}): SuorituspalveluMuuttuneetJalkeenQuery => ({
  $class:
    'fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluMuuttuneetJalkeenQuery',
  type: 'supa-muuttuneet',
  format: 'application/json',
  ...o
})

SuorituspalveluMuuttuneetJalkeenQuery.className =
  'fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluMuuttuneetJalkeenQuery' as const

export const isSuorituspalveluMuuttuneetJalkeenQuery = (
  a: any
): a is SuorituspalveluMuuttuneetJalkeenQuery =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluMuuttuneetJalkeenQuery'
