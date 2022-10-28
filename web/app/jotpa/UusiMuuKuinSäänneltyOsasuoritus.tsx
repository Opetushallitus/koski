import Atom from 'bacon.atom'
import React from 'baret'
import { elementWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import DropDown from '../components/Dropdown'
import {
  modelData,
  modelLookup,
  modelSet,
  modelSetTitle,
  modelSetValues,
  pushModel
} from '../editor/EditorModel'
import ModalDialog from '../editor/ModalDialog'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import { koulutusModuuliprototypes } from '../suoritus/Koulutusmoduuli'
import { ift } from '../util/util'

import { EditorModel, ObjectModel } from '../types/EditorModels'
import { OsasuoritusEditorModel } from '../types/OsasuoritusEditorModel'
import {
  OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
} from '../types/VapaaSivistystyo'
import {
  deleteOrganizationalPreference,
  getOrganizationalPreferences
} from '../virkailija/organizationalPreferences'

export type UusiMuuKuinSäänneltyOsasuoritusProps = {
  suoritusPrototypes: OsasuoritusEditorModel[]
  setExpanded: any
  suoritukset: ObjectModel[]
}

export const UusiMuuKuinSäänneltyOsasuoritus = ({
  suoritusPrototypes,
  setExpanded
}: UusiMuuKuinSäänneltyOsasuoritusProps) => {
  const findSuoritusPrototyyppi = (tyyppi: string) =>
    suoritusPrototypes.find((s) => s.value.classes.includes(tyyppi))

  const osasuoritus = findSuoritusPrototyyppi(
    'muunkuinsaannellynkoulutuksenosasuoritus'
  )

  return osasuoritus ? (
    <LisääPaikallinen
      suoritusPrototype={osasuoritus}
      setExpanded={setExpanded}
    />
  ) : null
}

type LisääPaikallinenProps = {
  suoritusPrototype: OsasuoritusEditorModel
  setExpanded: (suoritus: EditorModel) => (expanded: boolean) => void
}

const LisääPaikallinen = ({
  suoritusPrototype,
  setExpanded
}: LisääPaikallinenProps) => {
  type TallennettuSuoritus = // TODO: Tähän oikea tyypitys

      | OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
      | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus

  const showModal = Atom(false)
  const inputState = Atom('')
  const validP = inputState.map(Boolean)

  const closeModal = () => showModal.set(false)
  const updateInputState = (event: React.ChangeEvent<HTMLInputElement>) =>
    inputState.set(event.target.value)

  const koulutusmoduuliPrototype = koulutusModuuliprototypes(
    suoritusPrototype
  )[0] as OsasuoritusEditorModel

  const addNewSuoritus = (storedSuoritus: TallennettuSuoritus) => {
    const input = inputState.get()
    const updateValues = {
      'kuvaus.fi': { data: storedSuoritus ? storedSuoritus.kuvaus.fi : input },
      'tunniste.nimi.fi': {
        data: storedSuoritus ? storedSuoritus.tunniste.nimi.fi : input
      },
      'tunniste.koodiarvo': {
        data: storedSuoritus ? storedSuoritus.tunniste.koodiarvo : input
      }
    }
    const koulutusmoduuli = modelSetTitle(
      modelSetValues(koulutusmoduuliPrototype, updateValues),
      storedSuoritus ? storedSuoritus.tunniste.nimi.fi : input
    )
    const suoritus = modelSet(
      suoritusPrototype,
      koulutusmoduuli,
      'koulutusmoduuli'
    )
    pushModel(suoritus)
    setExpanded(suoritus)(true)
    showModal.set(false)
  }

  const päätasonSuoritus = modelData(
    suoritusPrototype.context.opiskeluoikeus,
    'suoritukset'
  )[0]

  const organisaatioOid = päätasonSuoritus.toimipiste.oid
  const key = modelLookup(suoritusPrototype, 'koulutusmoduuli')?.value
    .classes[0]

  const setOptions = (suoritukset: EditorModel[]) => {
    const tallennetutSuoritukset = suoritukset.map((suoritus) => {
      return {
        kuvaus: modelData(suoritus, 'kuvaus'),
        tunniste: modelData(suoritus, 'tunniste')
      }
    })
    options.set(tallennetutSuoritukset)
  }

  const options = Atom<TallennettuSuoritus[]>([])
  getOrganizationalPreferences(organisaatioOid, key).onValue(
    (value: EditorModel[]) => {
      setOptions(value)
    }
  )

  const newOsasuoritus = {
    kuvaus: { fi: '' },
    tunniste: { nimi: { fi: '' }, koodiarvo: '' },
    uusi: true
  }

  const poistaPaikallinenOsasuoritus = (osasuoritus: TallennettuSuoritus) => {
    const avain = osasuoritus.tunniste.koodiarvo
    const tyyppi = koulutusmoduuliPrototype.value.classes[0]
    deleteOrganizationalPreference(organisaatioOid, tyyppi, avain).onValue(
      setOptions
    )
  }

  const lisääText = t('Lisää osasuoritus')
  const lisääTitle = t('Osasuorituksen lisäys')

  return (
    <div className={'lisaa-uusi-suoritus paikallinen'}>
      <span className="lisaa-paikallinen-suoritus">
        {elementWithLoadingIndicator(
          options.map('.length').map(
            // @ts-expect-error DropDown
            <DropDown
              options={options}
              keyValue={(option) =>
                option.uusi ? 'uusi' : 'lisää ' + option.tunniste.koodiarvo
              }
              displayValue={(option) =>
                option.uusi ? t('Lisää uusi') : option.tunniste.nimi.fi
              }
              selectionText={lisääText}
              isRemovable={() => true}
              newItem={newOsasuoritus}
              removeText={t(
                'Poista osasuoritus. Poistaminen ei vaikuta olemassa oleviin suorituksiin.'
              )}
              // @ts-expect-error DropDown
              onSelectionChanged={(option) =>
                option.uusi ? showModal.set(true) : addNewSuoritus(option)
              }
              onRemoval={poistaPaikallinenOsasuoritus}
            />
          )
        )}
        {ift(
          showModal, // @ts-expect-error ModalDialog
          <ModalDialog
            className="lisaa-paikallinen-vst-suoritus-modal"
            onDismiss={closeModal}
            onSubmit={addNewSuoritus}
            okTextKey={lisääText}
            validP={validP}
          >
            <h2>
              {/* @ts-expect-error Text */}
              <Text name={lisääTitle} />
            </h2>
            <label>
              {/* @ts-expect-error Text */}
              <Text name={t('Opintokokonaisuuden nimi')} />
              <input
                className="paikallinen-koulutusmoduuli-nimi"
                type="text"
                autoFocus={true}
                onChange={updateInputState}
              />
            </label>
          </ModalDialog>
        )}
      </span>
    </div>
  )
}
