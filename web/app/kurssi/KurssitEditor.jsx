import React from 'baret'
import Atom from 'bacon.atom'
import {KurssiEditor} from './KurssiEditor'
import {wrapOptional} from '../editor/EditorModel'
import {
  contextualizeSubModel,
  ensureArrayKey,
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  oneOfPrototypes,
  pushModel
} from '../editor/EditorModel'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import UusiKurssiPopup from './UusiKurssiPopup'

export const KurssitEditor = ({model}) => {
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
  return (
    <span className="kurssit"><ul>{
      kurssit.map((kurssi, kurssiIndex) =>
        <KurssiEditor key={kurssiIndex} kurssi={kurssi}/>
      )
    }</ul>
    {
      model.context.edit && (<span className="uusi-kurssi">
        <a onClick={() => showUusiKurssiAtom.set(true)}><Text name="Lisää kurssi"/></a>
        {
          ift(showUusiKurssiAtom,
            <UusiKurssiPopup
              oppiaineenSuoritus={model}
              resultCallback={lisääKurssi}
              toimipiste={modelData(model.context.toimipiste).oid}
              uusiKurssinSuoritus={kurssinSuoritusProto}
            />
          )
        }
      </span>)
    }
    </span>
  )
}

let createKurssinSuoritus = (osasuoritukset) => {
  osasuoritukset = wrapOptional(osasuoritukset)
  let newItemIndex = modelItems(osasuoritukset).length
  let oppiaineenSuoritusProto = contextualizeSubModel(osasuoritukset.arrayPrototype, osasuoritukset, newItemIndex)
  let options = oneOfPrototypes(oppiaineenSuoritusProto)
  oppiaineenSuoritusProto = options[0]
  return contextualizeSubModel(oppiaineenSuoritusProto, osasuoritukset, newItemIndex)
}

