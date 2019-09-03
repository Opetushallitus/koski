import React from 'baret'
import Atom from 'bacon.atom'
import {KurssiEditor} from './KurssiEditor'
import {
  modelData,
  modelItems,
  modelLookup
} from '../editor/EditorModel'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import UusiKurssiPopup from './UusiKurssiPopup'
import {osasuoritusCanBeAdded, lisääKurssi} from './kurssi'
import {newOsasuoritusProto} from '../suoritus/Suoritus'

export const KurssitEditor = ({model, customTitle, customAlternativesCompletionFn, customKurssitSortFn}) => {
  const osasuoritukset = modelLookup(model, 'osasuoritukset')
  if (!osasuoritukset) return null
  let kurssit = modelItems(osasuoritukset)
  if (typeof customKurssitSortFn === 'function') {
    kurssit = kurssit.sort(customKurssitSortFn)
  }
  const kurssinSuoritusProtot = newOsasuoritusProto(model)
  const showUusiKurssiAtom = Atom(false)

  if (!kurssit.length && !model.context.edit) return null

  return (
    <span className="kurssit">
      <ul>
        {kurssit.map((kurssi, kurssiIndex) => <KurssiEditor key={kurssiIndex} kurssi={kurssi}/>)}
      </ul>
    {
      model.context.edit && osasuoritusCanBeAdded(osasuoritukset) && (<span className="uusi-kurssi">
        <a onClick={() => showUusiKurssiAtom.set(true)}><Text name={`Lisää ${customTitle || 'kurssi'}`}/></a>
        {
          ift(showUusiKurssiAtom,
            <UusiKurssiPopup
              oppiaineenSuoritus={model}
              resultCallback={(k) => lisääKurssi(k, model, showUusiKurssiAtom, kurssinSuoritusProtot)}
              toimipiste={modelData(model.context.toimipiste).oid}
              uusiKurssinSuoritus={kurssinSuoritusProtot}
              customTitle={customTitle}
              customAlternativesCompletionFn={customAlternativesCompletionFn}
            />
          )
        }
      </span>)
    }
    </span>
  )
}
