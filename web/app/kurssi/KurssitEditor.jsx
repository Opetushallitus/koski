import React from 'baret'
import Atom from 'bacon.atom'
import { KurssiEditor } from './KurssiEditor'
import { modelData, modelItems, modelLookup } from '../editor/EditorModel'
import Text from '../i18n/Text'
import { ift } from '../util/util'
import UusiKurssiPopup from './UusiKurssiPopup'
import { lisääKurssi, osasuoritusCountOk } from './kurssi'
import { newOsasuoritusProtos, suorituksenTyyppi } from '../suoritus/Suoritus'
import {
  isLukio2019ModuuliTaiOpintojakso,
  koulutusModuuliprototypes,
  isPaikallinen,
  isLukio2019Oppiaine
} from '../suoritus/Koulutusmoduuli'

export const KurssitEditor = ({
  model,
  customTitle,
  customAlternativesCompletionFn,
  customKurssitSortFn
}) => {
  const osasuoritukset = modelLookup(model, 'osasuoritukset')
  if (!osasuoritukset) return null
  let kurssit = modelItems(osasuoritukset)
  if (typeof customKurssitSortFn === 'function') {
    kurssit = kurssit.sort(customKurssitSortFn)
  }

  if (!kurssit.length && !model.context.edit) return null

  const showUusiKurssiAtom = Atom(false)
  const isPreIb = suorituksenTyyppi(model.context.suoritus) === 'preiboppimaara'

  const osasuoritusProtos = newOsasuoritusProtos(model)

  const lukionOppiaine = isLukio2019Oppiaine(
    modelLookup(model, 'koulutusmoduuli')
  )

  const kurssiPrototypes =
    lukionOppiaine && !isPreIb
      ? osasuoritusProtos
          .map((osasuoritusProto) =>
            koulutusModuuliprototypes(osasuoritusProto)
          )
          .flat()
      : koulutusModuuliprototypes(osasuoritusProtos[0])

  const sopivaOsasuorituksenPrototyyppi = (k) => {
    if (lukionOppiaine && !isPreIb) {
      if (isPaikallinen(k)) {
        return osasuoritusProtos.find((proto) =>
          isPaikallinen(modelLookup(proto, 'koulutusmoduuli'))
        )
      } else {
        return osasuoritusProtos.find(
          (proto) => !isPaikallinen(modelLookup(proto, 'koulutusmoduuli'))
        )
      }
    } else {
      return osasuoritusProtos[0]
    }
  }

  return (
    <span className="kurssit">
      <ul>
        {kurssit.map((kurssi, kurssiIndex) => (
          <KurssiEditor key={kurssiIndex} kurssi={kurssi} />
        ))}
      </ul>
      {model.context.edit && osasuoritusCountOk(osasuoritukset) && (
        <span className="uusi-kurssi">
          <a onClick={() => showUusiKurssiAtom.set(true)}>
            <Text name={`Lisää ${customTitle || 'kurssi'}`} />
          </a>
          {ift(
            showUusiKurssiAtom,
            <UusiKurssiPopup
              oppiaineenSuoritus={model}
              resultCallback={(k) =>
                lisääKurssi(
                  k,
                  model,
                  showUusiKurssiAtom,
                  sopivaOsasuorituksenPrototyyppi(k)
                )
              }
              toimipiste={modelData(model.context.toimipiste).oid}
              kurssiPrototypes={
                isPreIb
                  ? kurssiPrototypes.filter(
                      (kurssi) => !isLukio2019ModuuliTaiOpintojakso(kurssi)
                    )
                  : kurssiPrototypes
              }
              customTitle={customTitle}
              customAlternativesCompletionFn={customAlternativesCompletionFn}
            />
          )}
        </span>
      )}
    </span>
  )
}
