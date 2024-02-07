import React from 'baret'
import Atom from 'bacon.atom'
import * as R from 'ramda'
import {
  accumulateModelState,
  modelItems,
  modelLookup,
  modelValid,
  modelSetValue
} from '../editor/EditorModel'
import Text from '../i18n/Text'
import ModalDialog from '../editor/ModalDialog'
import { UusiKurssiDropdown } from './UusiKurssiDropdown'
import {
  isIBKurssi,
  isIBOppiaine,
  isLukio2019ModuuliTaiOpintojakso,
  isLukionKurssi,
  isPaikallinen,
  isLukioonValmistavanKoulutuksenKurssi
} from '../suoritus/Koulutusmoduuli'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { t } from '../i18n/i18n'

const propertyFilterForPaikallinen = (p) => !['koodistoUri'].includes(p.key)
const propertyFilterForLukio = (p) => !['tunniste'].includes(p.key)
const propertyFilterForModel = (model) =>
  isPaikallinen(model) || isIBKurssi(model)
    ? propertyFilterForPaikallinen
    : isLukionKurssi(model) || isLukio2019ModuuliTaiOpintojakso(model)
      ? propertyFilterForLukio
      : undefined

export default ({
  oppiaineenSuoritus,
  resultCallback,
  toimipiste,
  kurssiPrototypes,
  customTitle,
  customAlternativesCompletionFn
}) => {
  const oppiaine = modelLookup(oppiaineenSuoritus, 'koulutusmoduuli')
  const selectedPrototypeAtom = Atom()
  const selectedAtom = Atom()
  const validP = selectedAtom
  const valtakunnallisetKurssiProtot = kurssiPrototypes.filter(
    R.complement(isPaikallinen)
  )
  const paikallinenKurssiProto = kurssiPrototypes.find(
    isIBOppiaine(oppiaine) ? isIBKurssi : isPaikallinen
  )
  const kurssiSuoritukset = modelItems(oppiaineenSuoritus, 'osasuoritukset')
  selectedPrototypeAtom
    .map((proto) => (isPaikallinen(proto) ? undefined : proto))
    .forEach((proto) => selectedAtom.set(proto))

  return (
    <ModalDialog
      className="uusi-kurssi-modal"
      onDismiss={resultCallback}
      onSubmit={() => resultCallback(selectedAtom.get())}
      validP={validP}
      okTextKey="Lisää"
    >
      <h2>
        <Text name={`Lisää ${customTitle || 'kurssi'}`} />
      </h2>
      <span className="kurssi">
        <UusiKurssiDropdown
          suoritukset={kurssiSuoritukset}
          oppiaine={oppiaine}
          valtakunnallisetKurssiProtot={valtakunnallisetKurssiProtot}
          paikallinenKurssiProto={paikallinenKurssiProto}
          selected={selectedPrototypeAtom}
          resultCallback={(x) => selectedPrototypeAtom.set(x)}
          organisaatioOid={toimipiste}
          placeholder={t(`Lisää ${customTitle || 'kurssi'}`)}
          customAlternativesCompletionFn={customAlternativesCompletionFn}
        />
      </span>
      {
        // TODO: check placeholders from i18n
        selectedPrototypeAtom
          .flatMap((selectedProto) => {
            if (!validKurssi(selectedProto)) return null
            const suorituksenKoulutusmoduuli = isPaikallinen(selectedProto)
              ? modelSetValue(paikallinenKurssiProto, selectedProto.value)
              : selectedProto
            const modelP = accumulateModelState(suorituksenKoulutusmoduuli)
            modelP
              .map((model) => (modelValid(model) ? model : undefined))
              .forEach((model) => selectedAtom.set(model)) // set selected atom to non-empty only when valid data
            return modelP.map((model) => (
              <PropertiesEditor
                key="kurssi-props"
                model={model}
                propertyFilter={propertyFilterForModel(model)}
              />
            ))
          })
          .toProperty()
      }
    </ModalDialog>
  )
}

const validKurssi = (proto) =>
  isPaikallinen(proto) ||
  isLukionKurssi(proto) ||
  isIBKurssi(proto) ||
  isLukio2019ModuuliTaiOpintojakso(proto) ||
  isLukioonValmistavanKoulutuksenKurssi(proto)
