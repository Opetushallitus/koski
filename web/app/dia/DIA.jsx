import * as R from 'ramda'
import Bacon from 'baconjs'
import {modelData} from '../editor/EditorModel'
import {koodistoValues} from '../uusioppija/koodisto'

export const diaRyhmät = (oppiaineet, päätasonSuoritusModel, edit) => {
  const osaAlueetOppiaineista = Bacon.constant(oppiaineet.map(oppiaine => modelData(oppiaine, 'koulutusmoduuli.osaAlue')))
  const osaAlueetKoodistosta = edit ? koodistoValues('diaosaalue') : Bacon.constant([])
  const osaAlueetKaikki = Bacon.combineWith(osaAlueetOppiaineista, osaAlueetKoodistosta,
    (oppiaineista, koodistosta) => R.pipe(R.uniqBy(R.prop('koodiarvo')), R.sortBy(R.prop('koodiarvo')))(koodistosta.concat(oppiaineista)))

  const oppiaineetAineryhmittäin = R.groupBy(oppiaine => modelData(oppiaine, 'koulutusmoduuli.osaAlue').koodiarvo, oppiaineet)
  const aineryhmät = osaAlueetKaikki.map(ryhmät => ryhmät.map(ryhmä => ({ryhmä, aineet: oppiaineetAineryhmittäin[ryhmä.koodiarvo]})))

  const footnotes = []

  return {aineryhmät, footnotes}
}
