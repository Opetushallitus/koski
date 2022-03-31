import React from 'baret'
import * as L from 'partial.lenses'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import ModalDialog from '../editor/ModalDialog'
import UusiInternationalSchoolSuoritus from '../uusioppija/UusiInternationalSchoolSuoritus'
import {modelData, modelItems, modelLookup, modelSet, modelSetData} from '../editor/EditorModel'
import {copySuorituskieli, copyToimipiste, newSuoritusProto} from '../suoritus/Suoritus'
import {suoritusPrototypeKey} from '../internationalschool/internationalschoolSuoritus'


export const UusiInternationalSchoolVuosiluokanSuoritus = ({opiskeluoikeus, resultCallback}) => {
  const suoritusAtom = Atom()
  const suorituskieliAtom = Atom()
  const oppilaitosAtom = Atom(modelData(opiskeluoikeus, 'oppilaitos'))

  const grades = modelItems(opiskeluoikeus, 'suoritukset').map(s => modelData(s, 'koulutusmoduuli.tunniste.koodiarvo'))

  const lisääSuoritus = () => {
    const suoritus = suoritusAtom.get()
    let proto = newSuoritusProto(opiskeluoikeus, suoritusPrototypeKey(suoritus.tyyppi.koodiarvo))
    proto = withKoulutusmoduulinTunniste(proto, suoritus.koulutusmoduuli.tunniste)
    proto = copyToimipiste(modelLookup(opiskeluoikeus, 'suoritukset.0'), proto)
    proto = copySuorituskieli(modelLookup(opiskeluoikeus, 'suoritukset.0'), proto)
    resultCallback(proto)
  }

  return (<div>
    <ModalDialog className="lisaa-suoritus-modal" onDismiss={resultCallback} onSubmit={lisääSuoritus} okTextKey="Lisää">
      <h2><Text name="Suorituksen lisäys"/></h2>
      <UusiInternationalSchoolSuoritus suoritusAtom={suoritusAtom} oppilaitosAtom={oppilaitosAtom} suorituskieliAtom={suorituskieliAtom} without={grades} />
    </ModalDialog>
  </div>)
}

UusiInternationalSchoolVuosiluokanSuoritus.displayName = 'UusiInternationalSchoolVuosiluokanSuoritus'

UusiInternationalSchoolVuosiluokanSuoritus.canAddSuoritus = opiskeluoikeus =>
  modelData(opiskeluoikeus, 'tyyppi.koodiarvo') === 'internationalschool' &&
  modelItems(opiskeluoikeus, 'suoritukset').length < 13

UusiInternationalSchoolVuosiluokanSuoritus.addSuoritusTitle = () => <Text name='lisää vuosiluokan suoritus'/>

const withKoulutusmoduulinTunniste = (suoritusProto, tunniste) => {
  let kmt = modelLookup(suoritusProto, 'koulutusmoduuli.tunniste')
  kmt = modelSetData(kmt, tunniste)
  kmt = L.set(['title'], tunniste.nimi.fi, kmt)
  return modelSet(suoritusProto, kmt, 'koulutusmoduuli.tunniste')
}
