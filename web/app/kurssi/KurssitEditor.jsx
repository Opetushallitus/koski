import React from 'baret'
import Atom from 'bacon.atom'
import {KurssiEditor} from './KurssiEditor'
import {modelData, modelItems, modelLookup} from '../editor/EditorModel'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import UusiKurssiPopup from './UusiKurssiPopup'
import {lisääKurssi, osasuoritusCountOk} from './kurssi'
import {newOsasuoritusProto, suorituksenTyyppi} from '../suoritus/Suoritus'
import {isLukio2019ModuuliTaiOpintojakso, koulutusModuuliprototypes, isPaikallinen} from '../suoritus/Koulutusmoduuli'

export const KurssitEditor = ({model, customTitle, customAlternativesCompletionFn, customKurssitSortFn}) => {
  const osasuoritukset = modelLookup(model, 'osasuoritukset')
  if (!osasuoritukset) return null
  let kurssit = modelItems(osasuoritukset)
  if (typeof customKurssitSortFn === 'function') {
    kurssit = kurssit.sort(customKurssitSortFn)
  }

  if (!kurssit.length && !model.context.edit) return null

  const showUusiKurssiAtom = Atom(false)
  const isPreIb = suorituksenTyyppi(model.context.suoritus) === 'preiboppimaara'
  const kurssinSuoritusProto = newOsasuoritusProto(model,
    isPaikallinen(modelLookup(model, 'koulutusmoduuli')) ? 'lukionpaikallisenopintojaksonsuoritus2019' : undefined)
  const kurssiPrototypes = koulutusModuuliprototypes(kurssinSuoritusProto)

  return (
    <span className="kurssit">
      <ul>
        {kurssit.map((kurssi, kurssiIndex) => <KurssiEditor key={kurssiIndex} kurssi={kurssi}/>)}
      </ul>
    {
      model.context.edit && osasuoritusCountOk(osasuoritukset) && (<span className="uusi-kurssi">
        <a onClick={() => showUusiKurssiAtom.set(true)}><Text name={`Lisää ${customTitle || 'kurssi'}`}/></a>
        {
          ift(showUusiKurssiAtom,
            <UusiKurssiPopup
              oppiaineenSuoritus={model}
              resultCallback={(k) => lisääKurssi(k, model, showUusiKurssiAtom, kurssinSuoritusProto)}
              toimipiste={modelData(model.context.toimipiste).oid}
              kurssiPrototypes={isPreIb ? kurssiPrototypes.filter(kurssi => !isLukio2019ModuuliTaiOpintojakso(kurssi)) : kurssiPrototypes}
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
