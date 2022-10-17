import { lang, t } from '../i18n/i18n'
import * as L from 'partial.lenses'
import * as R from 'ramda'
import Bacon from 'baconjs'
import Http from '../util/http'

export const toKoodistoEnumValue = (koodistoUri, koodiarvo, nimi) => ({
  data: { koodiarvo, koodistoUri },
  title: nimi
})
export const koodiviiteToEnumValue = (koodiviite) => ({
  data: koodiviite,
  title: t(koodiviite.nimi)
})
export const enumValueToKoodiviite = (enumValue) => enumValue && enumValue.data
export const enumValueToKoodiviiteLens = L.iso(
  enumValueToKoodiviite,
  koodiviiteToEnumValue
)

export const tutkinnonOsanRyhmät = (diaarinumero, suoritustapa) =>
  suoritustapa
    ? Http.cachedGet(
        `/koski/api/tutkinnonperusteet/tutkinnonosat/ryhmat/${encodeURIComponent(
          diaarinumero
        )}/${suoritustapa.koodiarvo}`
      ).map((json) =>
        R.fromPairs(
          json.map((koodi) => [
            koodi.koodiarvo,
            koodi.nimi[lang] || koodi.nimi.fi
          ])
        )
      )
    : Bacon.constant([])
