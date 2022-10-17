import * as R from 'ramda'
import Bacon from 'baconjs'
import {
  modelData,
  modelItems,
  modelLookup,
  modelSetValue,
  modelTitle
} from '../editor/EditorModel'
import { koodistoValues } from '../uusioppija/koodisto'
import { parseLocation } from '../util/location'
import Http from '../util/http'
import { hasArviointi } from '../suoritus/Suoritus'
import { hasLasketaanKokonaispistemäärään } from './DIATutkintovaiheenLukukaudenArviointiEditor'
import { flatMapArray } from '../util/util'

export const arvosanaFootnote = {
  title: 'Ei lasketa kokonaispistemäärään',
  hint: '*'
}

export const diaRyhmät = (oppiaineet, päätasonSuoritusModel, edit) => {
  const [aineryhmäAineet, muutAineet] = R.partition(
    (a) =>
      modelLookup(a, 'koulutusmoduuli').value.classes.includes(
        'diaosaalueoppiaine'
      ),
    oppiaineet
  )

  const osaAlueetOppiaineista = Bacon.constant(
    aineryhmäAineet.map((oppiaine) =>
      modelData(oppiaine, 'koulutusmoduuli.osaAlue')
    )
  )
  const osaAlueetKoodistosta = edit
    ? koodistoValues('diaosaalue')
    : Bacon.constant([])
  const osaAlueetKaikki = Bacon.combineWith(
    osaAlueetOppiaineista,
    osaAlueetKoodistosta,
    (oppiaineista, koodistosta) =>
      R.pipe(
        R.uniqBy(R.prop('koodiarvo')),
        R.sortBy(R.prop('koodiarvo'))
      )(koodistosta.concat(oppiaineista))
  )

  const oppiaineetAineryhmittäin = R.groupBy(
    (oppiaine) => modelData(oppiaine, 'koulutusmoduuli.osaAlue').koodiarvo,
    aineryhmäAineet
  )
  const aineryhmät = osaAlueetKaikki.map((ryhmät) =>
    ryhmät.map((ryhmä) => ({
      ryhmä,
      aineet: oppiaineetAineryhmittäin[ryhmä.koodiarvo]
    }))
  )

  const footnotes = R.any(
    R.both(hasLasketaanKokonaispistemäärään, eiLasketaKokonaispistemäärään),
    flatMapArray(oppiaineet, (aine) => modelItems(aine, 'osasuoritukset'))
  )
    ? [arvosanaFootnote]
    : []

  return { aineryhmät, muutAineet, footnotes }
}

export const diaLukukausiAlternativesCompletionFn = (
  oppiaine,
  kurssiPrototypes
) => {
  const alternativesForField = (model) => {
    const koodistoAlternativesPath = modelLookup(
      model,
      'tunniste'
    ).alternativesPath
    const koodistoUri =
      koodistoAlternativesPath && R.nth(-2, koodistoAlternativesPath.split('/'))
    const koodiarvot =
      koodistoAlternativesPath && R.last(koodistoAlternativesPath.split('/'))

    const loc = parseLocation(
      `/koski/api/editor/koodit/${koodistoUri}/${koodiarvot}`
    )

    return Http.cachedGet(loc.toString()).map((alternatives) =>
      alternatives.map((enumValue) =>
        modelSetValue(model, enumValue, 'tunniste')
      )
    )
  }

  return Bacon.combineAsArray(kurssiPrototypes.map(alternativesForField))
    .last()
    .map(R.unnest)
}

export const isDIAOppiaineenTutkintovaiheenOsasuoritus = (osasuoritus) =>
  osasuoritus.value.classes.includes(
    'diaoppiaineentutkintovaiheenosasuorituksensuoritus'
  )

export const eiLasketaKokonaispistemäärään = (osasuoritus) =>
  isDIAOppiaineenTutkintovaiheenOsasuoritus(osasuoritus) &&
  hasArviointi(osasuoritus) &&
  modelData(osasuoritus, 'arviointi.-1.lasketaanKokonaispistemäärään') === false

export const diaKurssitSortFn = (k1, k2) =>
  modelTitle(k1, 'koulutusmoduuli').localeCompare(
    modelTitle(k2, 'koulutusmoduuli')
  )
