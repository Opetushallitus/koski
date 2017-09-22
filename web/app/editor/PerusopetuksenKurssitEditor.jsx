import React from 'baret'
import Atom from 'bacon.atom'
import {KurssiEditor} from './KurssiEditor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {
  contextualizeSubModel, ensureArrayKey, modelData, modelItems, modelLookup, modelSet, oneOfPrototypes,
  pushModel
} from './EditorModel'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import {ift} from '../util'
import {UusiKurssiDropdown} from './UusiKurssiDropdown.jsx'

export const PerusopetuksenKurssitEditor = ({model}) => {
  let osasuoritukset = modelLookup(model, 'osasuoritukset')
  if (!osasuoritukset) return null
  let kurssit = modelItems(osasuoritukset)
  let kurssinSuoritusProto = createKurssinSuoritus(osasuoritukset)
  let showUusiKurssiAtom = Atom(false)
  let lisääKurssi = (kurssi) => {
    if (kurssi) {
      var suoritusUudellaKurssilla = modelSet(kurssinSuoritusProto, kurssi, 'koulutusmoduuli')
      ensureArrayKey(suoritusUudellaKurssilla)
      pushModel(suoritusUudellaKurssilla, model.context.changeBus)
    }
    showUusiKurssiAtom.set(false)
  }

  if (!kurssit.length && !model.context.edit) return null
  return (<tr className="kurssit"><td colSpan="4">
    <ul className="kurssit">{
      kurssit.map((kurssi, kurssiIndex) =>
        <KurssiEditor key={kurssiIndex} kurssi={kurssi}/>
      )
    }</ul>
    {
      model.context.edit && <a className="uusi-kurssi" onClick={() => showUusiKurssiAtom.set(true)}><Text name="Lisää kurssi"/></a>
    }
    {
      ift(showUusiKurssiAtom, <UusiPerusopetuksenKurssiPopup resultCallback={lisääKurssi} toimipiste={modelData(model.context.toimipiste).oid} uusiKurssinSuoritus={kurssinSuoritusProto} />)
    }
  </td></tr>)
}

const UusiPerusopetuksenKurssiPopup = ({resultCallback, toimipiste, uusiKurssinSuoritus}) => {
  let selectedAtom = Atom()
  let validP = selectedAtom
  return (<ModalDialog className="uusi-kurssi-modal" onDismiss={resultCallback} onSubmit={() => resultCallback(selectedAtom.get())} validP={validP} okTextKey="Lisää">
    <h2><Text name="Lisää kurssi"/></h2>
    <span className="kurssi"><UusiKurssiDropdown kurssinSuoritus={uusiKurssinSuoritus}
                                                 selected={selectedAtom}
                                                 resultCallback={(x) => selectedAtom.set(x)}
                                                 organisaatioOid={toimipiste}
                                                 placeholder="Lisää kurssi"/></span>
  </ModalDialog>)
}

let createKurssinSuoritus = (osasuoritukset) => {
  osasuoritukset = wrapOptional({model: osasuoritukset})
  let newItemIndex = modelItems(osasuoritukset).length
  let oppiaineenSuoritusProto = contextualizeSubModel(osasuoritukset.arrayPrototype, osasuoritukset, newItemIndex)
  let options = oneOfPrototypes(oppiaineenSuoritusProto)
  oppiaineenSuoritusProto = options[0]
  return contextualizeSubModel(oppiaineenSuoritusProto, osasuoritukset, newItemIndex)
}

