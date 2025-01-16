import * as R from 'ramda'
import Bacon from 'baconjs'
import { modelData, modelLookup } from '../editor/EditorModel'
import { koodistoValues } from '../uusioppija/koodisto'

export const arvosanaFootnote = { title: 'Ennustettu arvosana', hint: '*' }

export const ibRyhmät = (oppiaineet, päätasonSuoritusModel, edit) => {
  const ryhmätOppiaineista = Bacon.constant(
    oppiaineet.map((oppiaine) => modelData(oppiaine, 'koulutusmoduuli.ryhmä'))
  )
  const ryhmätKoodistosta = edit
    ? koodistoValues('aineryhmaib')
    : Bacon.constant([])
  const ryhmätKaikki = Bacon.combineWith(
    ryhmätOppiaineista,
    ryhmätKoodistosta,
    (oppiaineista, koodistosta) =>
      R.pipe(
        R.uniqBy(R.prop('koodiarvo')),
        R.sortBy(R.prop('koodiarvo'))
      )(koodistosta.concat(oppiaineista))
  )

  const oppiaineetAineryhmittäin = R.groupBy(
    (oppiaine) => modelData(oppiaine, 'koulutusmoduuli.ryhmä').koodiarvo,
    oppiaineet
  )
  const aineryhmät = ryhmätKaikki.map((ryhmät) =>
    ryhmät.map((ryhmä) => ({
      ryhmä,
      aineet: oppiaineetAineryhmittäin[ryhmä.koodiarvo]
    }))
  )

  const yhteisetIbSuoritukset = [
    'theoryOfKnowledge',
    'creativityActionService',
    'extendedEssay'
  ].map((k) => modelLookup(päätasonSuoritusModel, k))
  const footnotes = R.any(
    (s) => modelData(s, 'arviointi.-1.predicted'),
    R.concat(oppiaineet, yhteisetIbSuoritukset)
  )
    ? [arvosanaFootnote]
    : []

  return { aineryhmät, footnotes }
}
