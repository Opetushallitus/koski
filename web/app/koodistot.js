import {lang, t} from './i18n'
import * as L from 'partial.lenses'
import R from 'ramda'

import ammatillisentutkinnonosanryhmaRaw from '../../src/main/resources/mockdata/koodisto/koodit/ammatillisentutkinnonosanryhma.json'
import suorituksentilaRaw from '../../src/main/resources/mockdata/koodisto/koodit/suorituksentila.json'

const koodiMetadata = rawKoodi => rawKoodi.metadata.find(m => m.kieli.toLowerCase() == lang) || rawKoodi.metadata.find(m => m.kieli == 'FI') || rawKoodi.metadata[0]
const readKoodisto = json => R.fromPairs(json.map(rawKoodi => ([ rawKoodi.koodiArvo, koodiMetadata(rawKoodi).nimi ])))

export const ammatillisentutkinnonosanryhmaKoodisto = readKoodisto(ammatillisentutkinnonosanryhmaRaw)
export const suorituksentilaKoodisto = readKoodisto(suorituksentilaRaw)
export const toKoodistoEnumValue = (koodistoUri, koodiarvo, nimi) => ({data: { koodiarvo, koodistoUri }, title: nimi})
export const koodiviiteToEnumValue = (koodiviite) => ({data: koodiviite, title: t(koodiviite.nimi)})
export const enumValueToKoodiviite = (enumValue) => enumValue && enumValue.data
export const enumValueToKoodiviiteLens = L.iso(enumValueToKoodiviite, koodiviiteToEnumValue)