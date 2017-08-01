import {lang} from './i18n'
import R from 'ramda'

import ammatillisentutkinnonosanryhmaRaw from '../../src/main/resources/mockdata/koodisto/koodit/ammatillisentutkinnonosanryhma.json'

const koodiMetadata = rawKoodi => rawKoodi.metadata.find(m => m.kieli.toLowerCase() == lang) || rawKoodi.metadata.find(m => m.kieli == 'FI') || rawKoodi.metadata[0]
const readKoodisto = json => R.fromPairs(json.map(rawKoodi => ([ rawKoodi.koodiArvo, koodiMetadata(rawKoodi).nimi ])))

export const ammatillisentutkinnonosanryhma = readKoodisto(ammatillisentutkinnonosanryhmaRaw)