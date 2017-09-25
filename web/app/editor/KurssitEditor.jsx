import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import {KurssiEditor} from './KurssiEditor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {
  contextualizeSubModel,
  ensureArrayKey,
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  oneOfPrototypes,
  pushModel
} from './EditorModel'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import {ift} from '../util'
import {UusiKurssiDropdown} from './UusiKurssiDropdown.jsx'
import {isPaikallinen, koulutusModuuliprototypes} from './Koulutusmoduuli'

export const KurssitEditor = ({model}) => {
  let osasuoritukset = modelLookup(model, 'osasuoritukset')
  let oppiaine = modelLookup(model, 'koulutusmoduuli')
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
  return (
    <span className="kurssit"><ul>{
      kurssit.map((kurssi, kurssiIndex) =>
        <KurssiEditor key={kurssiIndex} kurssi={kurssi}/>
      )
    }</ul>
    {
      model.context.edit && <a className="uusi-kurssi" onClick={() => showUusiKurssiAtom.set(true)}><Text name="Lisää kurssi"/></a>
    }
    {
      ift(showUusiKurssiAtom, <UusiKurssiPopup oppiaine={oppiaine} resultCallback={lisääKurssi} toimipiste={modelData(model.context.toimipiste).oid} uusiKurssinSuoritus={kurssinSuoritusProto} />)
    }
    </span>
  )
}

const UusiKurssiPopup = ({oppiaine, resultCallback, toimipiste, uusiKurssinSuoritus}) => {
  let selectedAtom = Atom()
  let validP = selectedAtom
  let päätasonSuoritus = uusiKurssinSuoritus.context.suoritus
  let valtakunnallisetKurssiProtot = filterProtos(päätasonSuoritus, koulutusModuuliprototypes(uusiKurssinSuoritus).filter(R.complement(isPaikallinen)))
  let paikallinenKurssiProto = koulutusModuuliprototypes(uusiKurssinSuoritus).find(isPaikallinen)

  return (<ModalDialog className="uusi-kurssi-modal" onDismiss={resultCallback} onSubmit={() => resultCallback(selectedAtom.get())} validP={validP} okTextKey="Lisää">
    <h2><Text name="Lisää kurssi"/></h2>
    <span className="kurssi"><UusiKurssiDropdown oppiaine={oppiaine}
                                                 kurssinSuoritus={uusiKurssinSuoritus}
                                                 valtakunnallisetKurssiProtot={valtakunnallisetKurssiProtot}
                                                 paikallinenKurssiProto={paikallinenKurssiProto}
                                                 selected={selectedAtom}
                                                 resultCallback={(x) => selectedAtom.set(x)}
                                                 organisaatioOid={toimipiste}
                                                 placeholder="Lisää kurssi"/></span>
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

let createKurssinSuoritus = (osasuoritukset) => {
  osasuoritukset = wrapOptional({model: osasuoritukset})
  let newItemIndex = modelItems(osasuoritukset).length
  let oppiaineenSuoritusProto = contextualizeSubModel(osasuoritukset.arrayPrototype, osasuoritukset, newItemIndex)
  let options = oneOfPrototypes(oppiaineenSuoritusProto)
  oppiaineenSuoritusProto = options[0]
  return contextualizeSubModel(oppiaineenSuoritusProto, osasuoritukset, newItemIndex)
}

