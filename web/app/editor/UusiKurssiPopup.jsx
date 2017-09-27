import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import {accumulateModelState, modelData, modelItems, modelLookup, modelValid} from './EditorModel'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import {UusiKurssiDropdown} from './UusiKurssiDropdown.jsx'
import {isPaikallinen, koulutusModuuliprototypes} from './Koulutusmoduuli'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {t} from '../i18n'

export default ({oppiaineenSuoritus, resultCallback, toimipiste, uusiKurssinSuoritus}) => {
  let oppiaine = modelLookup(oppiaineenSuoritus, 'koulutusmoduuli')
  let selectedPrototypeAtom = Atom()
  let selectedAtom = Atom()
  let validP = selectedAtom
  let päätasonSuoritus = uusiKurssinSuoritus.context.suoritus
  let valtakunnallisetKurssiProtot = filterProtos(päätasonSuoritus, koulutusModuuliprototypes(uusiKurssinSuoritus).filter(R.complement(isPaikallinen)))
  let paikallinenKurssiProto = koulutusModuuliprototypes(uusiKurssinSuoritus).find(isPaikallinen)
  let kurssiSuoritukset = modelItems(oppiaineenSuoritus, 'osasuoritukset')
  selectedPrototypeAtom.map(proto => isPaikallinen(proto) ? undefined : proto).forEach(proto => selectedAtom.set(proto))

  return (<ModalDialog className="uusi-kurssi-modal" onDismiss={resultCallback} onSubmit={() => resultCallback(selectedAtom.get())} validP={validP} okTextKey="Lisää">
    <h2><Text name="Lisää kurssi"/></h2>
    <span className="kurssi"><UusiKurssiDropdown suoritukset={kurssiSuoritukset}
                                                 oppiaine={oppiaine}
                                                 kurssinSuoritus={uusiKurssinSuoritus}
                                                 valtakunnallisetKurssiProtot={valtakunnallisetKurssiProtot}
                                                 paikallinenKurssiProto={paikallinenKurssiProto}
                                                 selected={selectedPrototypeAtom}
                                                 resultCallback={(x) => selectedPrototypeAtom.set(x)}
                                                 organisaatioOid={toimipiste}
                                                 placeholder={t('Lisää kurssi')}/></span>
    { // TODO: check placeholders from i18n
      selectedPrototypeAtom.flatMap(selectedProto => {
        if (!isPaikallinen(selectedProto)) return null
        let modelP = accumulateModelState(selectedProto)
        modelP.map(model => modelValid(model) ? model : undefined).forEach(model => selectedAtom.set(model)) // set selected atom to non-empty only when valid data
        return modelP.map(model => <PropertiesEditor model={model} propertyFilter={p => !['koodistoUri'].includes(p.key)}/>)
      }).toProperty()
    }
  </ModalDialog>)
}

const filterProtos = (päätasonSuoritus, protos) => {
  if (päätasonSuoritus.value.classes.includes('aikuistenperusopetuksenoppimaaransuoritus')) {
    let diaari = modelData(päätasonSuoritus, 'koulutusmoduuli.perusteenDiaarinumero')
    return protos.filter(proto => {
      switch (diaari) {
        case 'OPH-1280-2017': return proto.value.classes.includes('valtakunnallinenaikuistenperusopetuksenpaattovaiheenkurssi2017')
        case '19/011/2015': return proto.value.classes.includes('valtakunnallinenaikuistenperusopetuksenkurssi2015')
        default: return true
      }
    })
  } else {
    return protos
  }
}
