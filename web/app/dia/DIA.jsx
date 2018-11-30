import * as R from 'ramda'
import Bacon from 'baconjs'
import {modelData, modelLookup} from '../editor/EditorModel'
import {koodistoValues} from '../uusioppija/koodisto'

export const diaRyhmät = (oppiaineet, päätasonSuoritusModel, edit) => {
  const [aineryhmäAineet, muutAineet] = R.partition(a => modelLookup(a, 'koulutusmoduuli').value.classes.includes('diaosaalueoppiaine'), oppiaineet)

  const osaAlueetOppiaineista = Bacon.constant(aineryhmäAineet.map(oppiaine => modelData(oppiaine, 'koulutusmoduuli.osaAlue')))
  const osaAlueetKoodistosta = edit ? koodistoValues('diaosaalue') : Bacon.constant([])
  const osaAlueetKaikki = Bacon.combineWith(osaAlueetOppiaineista, osaAlueetKoodistosta,
    (oppiaineista, koodistosta) => R.pipe(R.uniqBy(R.prop('koodiarvo')), R.sortBy(R.prop('koodiarvo')))(koodistosta.concat(oppiaineista)))

  const oppiaineetAineryhmittäin = R.groupBy(oppiaine => modelData(oppiaine, 'koulutusmoduuli.osaAlue').koodiarvo, aineryhmäAineet)
  const aineryhmät = osaAlueetKaikki.map(ryhmät => ryhmät.map(ryhmä => ({ryhmä, aineet: oppiaineetAineryhmittäin[ryhmä.koodiarvo]})))

  const footnotes = []

  return {aineryhmät, muutAineet, footnotes}
}
