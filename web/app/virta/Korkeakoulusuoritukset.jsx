import React from 'react'
import * as R from 'ramda'
import { Editor } from '../editor/Editor'
import {
  addContext,
  modelData,
  modelItems,
  modelLookup,
  modelSetValue
} from '../editor/EditorModel'
import Text from '../i18n/Text'
import OmatTiedotSuoritustaulukko from '../suoritus/OmatTiedotSuoritustaulukko'
import { Suoritustaulukko } from '../suoritus/Suoritustaulukko'

const Korkeakoulusuoritukset = ({ opiskeluoikeus, showOmatTiedotTaulukko }) => {
  const suoritukset = modelItems(opiskeluoikeus, 'suoritukset')
  const [tutkinnot, opintojaksot] = R.partition(
    (s) => modelData(s, 'tyyppi').koodiarvo !== 'korkeakoulunopintojakso',
    suoritukset
  )
  const modelWithoutTutkinnot = modelSetValue(
    opiskeluoikeus,
    opintojaksot,
    'suoritukset'
  )

  const hasTutkintoja = tutkinnot.length > 0
  const hasOpintojaksoja = opintojaksot.length > 0

  return (
    <div className="suoritukset">
      {tutkinnot.map((t, i) => (
        <Editor key={i} model={t} alwaysUpdate="true" />
      ))}

      {hasOpintojaksoja && (
        <div>
          {hasTutkintoja && (
            <h4>
              <Text name="Opintojaksot" />
            </h4>
          )}
          <IrrallisetOpintojaksot
            opiskeluoikeus={modelWithoutTutkinnot}
            showOmatTiedotTaulukko={showOmatTiedotTaulukko}
          />
        </div>
      )}
    </div>
  )
}

const IrrallisetOpintojaksot = ({ opiskeluoikeus, showOmatTiedotTaulukko }) => {
  const model = addContext(opiskeluoikeus, { suoritus: opiskeluoikeus })
  return showOmatTiedotTaulukko ? (
    <OmatTiedotSuoritustaulukko
      suorituksetModel={modelLookup(model, 'suoritukset')}
    />
  ) : (
    <Suoritustaulukko suorituksetModel={modelLookup(model, 'suoritukset')} />
  )
}

export { Korkeakoulusuoritukset }
