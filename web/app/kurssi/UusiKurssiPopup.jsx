import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import {accumulateModelState, modelItems, modelLookup, modelValid} from '../editor/EditorModel'
import Text from '../i18n/Text'
import ModalDialog from '../editor/ModalDialog'
import {UusiKurssiDropdown} from './UusiKurssiDropdown'
import {isLukionKurssi, isPaikallinen, koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {t} from '../i18n/i18n'

const propertyFilterForPaikallinen = p => !['koodistoUri'].includes(p.key)
const propertyFilterForLukio = p => !['tunniste'].includes(p.key)
const propertyFilterForModel = model =>
  isPaikallinen(model) ? propertyFilterForPaikallinen
    : isLukionKurssi(model) ? propertyFilterForLukio
    : undefined

export default ({oppiaineenSuoritus, resultCallback, toimipiste, uusiKurssinSuoritus}) => {
  let oppiaine = modelLookup(oppiaineenSuoritus, 'koulutusmoduuli')
  let selectedPrototypeAtom = Atom()
  let selectedAtom = Atom()
  let validP = selectedAtom
  let valtakunnallisetKurssiProtot = koulutusModuuliprototypes(uusiKurssinSuoritus).filter(R.complement(isPaikallinen))
  let paikallinenKurssiProto = koulutusModuuliprototypes(uusiKurssinSuoritus).find(isPaikallinen)
  let kurssiSuoritukset = modelItems(oppiaineenSuoritus, 'osasuoritukset')
  selectedPrototypeAtom.map(proto => isPaikallinen(proto) ? undefined : proto).forEach(proto => selectedAtom.set(proto))

  return (
      <ModalDialog className="uusi-kurssi-modal" onDismiss={resultCallback}
                   onSubmit={() => resultCallback(selectedAtom.get())} validP={validP} okTextKey="Lisää">
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
            if (!isPaikallinen(selectedProto) && !isLukionKurssi(selectedProto)) return null
            let modelP = accumulateModelState(selectedProto)
            modelP.map(model => modelValid(model) ? model : undefined).forEach(model => selectedAtom.set(model)) // set selected atom to non-empty only when valid data
            return modelP.map(model => <PropertiesEditor key="kurssi-props" model={model} propertyFilter={propertyFilterForModel(model)}/>)
          }).toProperty()
        }
      </ModalDialog>
  )
}
