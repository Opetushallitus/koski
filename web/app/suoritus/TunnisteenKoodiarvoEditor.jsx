import React from 'baret'
import Http from '../util/http'
import { modelData, modelLookup, pushModelValue } from '../editor/EditorModel'
import Dropdown from '../components/Dropdown'

export const TunnisteenKoodiarvoEditor = ({ model }) => {
  if (hideTunnisteenKoodiarvo(model)) return null

  const koulutuksetDiaarinumerolleP = fetchKoulutuksetDiaarille(
    modelData(model, 'perusteenDiaarinumero')
  ).map((obs) => obs.map((k) => k.data))
  const tunnisteModel = modelLookup(model, 'tunniste')
  const updateTunniste = (koulutus) =>
    pushModelValue(tunnisteModel, { data: koulutus })
  const automaticUpdatePossible = (koulutukset) =>
    koulutukset.length === 1 &&
    modelData(tunnisteModel, 'koodiarvo') !== koulutukset[0].koodiarvo

  koulutuksetDiaarinumerolleP.onValue(
    (koulutukset) =>
      model.context.edit &&
      automaticUpdatePossible(koulutukset) &&
      updateTunniste(koulutukset[0])
  )

  return (
    <span>
      {koulutuksetDiaarinumerolleP.map((koulutukset) =>
        koulutukset.length > 1 && model.context.edit ? (
          <TutkintoKoodiDropdown
            options={koulutukset}
            selected={modelData(tunnisteModel)}
            onSelectionChanged={updateTunniste}
          />
        ) : (
          <Koodiarvo model={tunnisteModel} />
        )
      )}
    </span>
  )
}

const TutkintoKoodiDropdown = ({ options, onSelectionChanged, selected }) => (
  <Dropdown
    options={options}
    selected={selected}
    onSelectionChanged={onSelectionChanged}
    keyValue={(k) => k.koodiarvo}
    displayValue={(k) => k.koodiarvo}
  />
)

const hideTunnisteenKoodiarvo = (model) => {
  return (
    model.context.kansalainen ||
    !diaarinumerollinen(model) ||
    model.value.classes.includes('lukionoppiaineidenoppimaarat2019')
  )
}

const Koodiarvo = ({ model }) => <span>{modelData(model, 'koodiarvo')}</span>

const diaarinumerollinen = (model) =>
  model.value.classes.includes('diaarinumerollinen')

const fetchKoulutuksetDiaarille = (diaari) =>
  Http.cachedGet(
    `/koski/api/editor/koodit/koulutukset/koulutus/${encodeURIComponent(
      diaari
    )}`
  )
