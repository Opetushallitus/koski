import React from 'baret'
import Bacon from 'baconjs'
import {
  addContext,
  modelData,
  modelItems,
  modelLookup,
  modelSetValues,
  modelTitle,
  pushModel,
  modelEmpty
} from '../editor/EditorModel'
import { TogglableEditor } from '../editor/TogglableEditor'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { OpiskeluoikeudenTilaEditor } from './OpiskeluoikeudenTilaEditor'
import Versiohistoria from './Versiohistoria'
import { ExpandablePropertiesEditor } from '../editor/ExpandablePropertiesEditor'
import { Editor } from '../editor/Editor'
import { suorituksenTyyppi, suoritusTitle } from '../suoritus/Suoritus'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'
import {
  assignTabNames,
  suoritusTabIndex,
  SuoritusTabs,
  urlForTab
} from '../suoritus/SuoritusTabs'
import { Korkeakoulusuoritukset } from '../virta/Korkeakoulusuoritukset'
import { OpiskeluoikeudenTila } from '../omattiedot/fragments/OpiskeluoikeudenTila'
import { ArrayEditor } from '../editor/ArrayEditor'
import { VirtaVirheetPopup } from '../virta/VirtaVirheetPopup.jsx'
import { currentLocation, navigateTo } from '../util/location.js'
import Atom from 'bacon.atom'

export const excludedProperties = [
  'suoritukset',
  'alkamispäivä',
  'arvioituPäättymispäivä',
  'päättymispäivä',
  'oppilaitos',
  'lisätiedot',
  'synteettinen',
  'virtaVirheet'
]

export const OpiskeluoikeusEditor = ({ model }) => {
  return (
    <TogglableEditor
      aria-label="Muokkaa opiskeluoikeutta"
      model={addContext(model, { opiskeluoikeus: model })}
      renderChild={(mdl, editLink) => {
        const katsomassaVirtaVirheitä = Atom(false)

        const context = mdl.context

        const alkuChangeBus = Bacon.Bus()
        alkuChangeBus.onValue((v) => {
          const value = v[0].value
          pushModel(
            modelSetValues(model, {
              alkamispäivä: value,
              'tila.opiskeluoikeusjaksot.0.alku': value
            })
          )
        })

        const hasOppilaitos = !!modelData(mdl, 'oppilaitos')
        const hasAlkamispäivä = !!modelData(mdl, 'alkamispäivä')
        const isSyntheticOpiskeluoikeus = !!modelData(model, 'synteettinen')

        const virtaVirheetEnabled =
          currentLocation().params.diagnostiikka === 'true'

        return (
          <div className="opiskeluoikeus">
            <h3>
              <span className="otsikkotiedot">
                {hasOppilaitos && (
                  <span className="oppilaitos">
                    {modelTitle(mdl, 'oppilaitos')}
                  </span>
                )}
                {hasOppilaitos && <span>{', '}</span>}
                <span
                  className="koulutus"
                  style={
                    hasOppilaitos ? { textTransform: 'lowercase' } : undefined
                  }
                >
                  {näytettäväPäätasonSuoritusTitle(mdl)}
                </span>
                {hasAlkamispäivä && (
                  <OpiskeluoikeudenTila opiskeluoikeus={mdl} />
                )}
              </span>
              <Versiohistoria
                opiskeluoikeusOid={modelData(mdl, 'oid')}
                oppijaOid={context.oppijaOid}
              />
              <OpiskeluoikeudenId opiskeluoikeus={mdl} />
            </h3>
            {modelData(model, 'virtaVirheet') &&
              modelData(model, 'virtaVirheet').length > 0 &&
              virtaVirheetEnabled && (
                <a
                  className="virta-virheet"
                  onClick={() => katsomassaVirtaVirheitä.modify((x) => !x)}
                >
                  {'Virta-Virheet'}
                </a>
              )}
            {katsomassaVirtaVirheitä.map(
              (katsomassa) =>
                katsomassa && (
                  <VirtaVirheetPopup
                    virheet={modelData(model, 'virtaVirheet')}
                    onDismiss={() => katsomassaVirtaVirheitä.modify((x) => !x)}
                  />
                )
            )}
            <div
              className={
                mdl.context.edit
                  ? 'opiskeluoikeus-content editing'
                  : 'opiskeluoikeus-content'
              }
            >
              {!isSyntheticOpiskeluoikeus && (
                <OpiskeluoikeudenTiedot
                  opiskeluoikeus={mdl}
                  editLink={editLink}
                  alkuChangeBus={alkuChangeBus}
                />
              )}
              <Suoritukset opiskeluoikeus={mdl} />
            </div>
          </div>
        )
      }}
    />
  )
}

const OpiskeluoikeudenTiedot = ({
  opiskeluoikeus,
  editLink,
  alkuChangeBus
}) => (
  <div className="opiskeluoikeuden-tiedot">
    {editLink}
    {modelData(opiskeluoikeus, 'alkamispäivä') && (
      <OpiskeluoikeudenVoimassaoloaika opiskeluoikeus={opiskeluoikeus} />
    )}
    <PropertiesEditor
      model={opiskeluoikeus}
      propertyFilter={(p) =>
        opiskeluoikeusPropertyFilter(opiskeluoikeus, p) && esiopetusFilter(p)
      }
      showAnyway={showEsiopetusKoulutustoimija(opiskeluoikeus)}
      propertyEditable={(p) =>
        p.key === 'koulutustoimija' ? false : p.editable
      }
      getValueEditor={(prop, getDefault) => {
        switch (prop.key) {
          case 'tila':
            return (
              <OpiskeluoikeudenTilaEditor
                model={opiskeluoikeus}
                alkuChangeBus={alkuChangeBus}
              />
            )
          case 'organisaatiohistoria':
            return (
              <ArrayEditor
                model={addContext(prop.model, { edit: false })}
                reverse={true}
              />
            )
          default:
            return getDefault()
        }
      }}
    />
    {modelLookup(opiskeluoikeus, 'lisätiedot') && (
      <ExpandablePropertiesEditor
        model={opiskeluoikeus}
        propertyName="lisätiedot"
        propertyFilter={(prop) =>
          opiskeluoikeus.context.edit || modelData(prop.model) !== false
        }
      />
    )}
  </div>
)

const showEsiopetusKoulutustoimija = (opiskeluoikeus) => (property) =>
  property.key === 'koulutustoimija' &&
  modelData(opiskeluoikeus, 'järjestämismuoto')

const esiopetusFilter = (property) =>
  !(property.key === 'järjestämismuoto' && modelEmpty(property.model))

const opiskeluoikeusPropertyFilter = (opiskeluoikeus, property) =>
  !excludedProperties.includes(property.key) &&
  (opiskeluoikeus.context.edit || modelData(property.model) !== false)

export const OpiskeluoikeudenId = ({ opiskeluoikeus }) => {
  const selectAllText = (e) => {
    e.stopPropagation()
    const el = e.target
    const range = document.createRange()
    range.selectNodeContents(el)
    const sel = window.getSelection()
    sel.removeAllRanges()
    sel.addRange(range)
  }
  const opiskeluoikeusOid = modelData(opiskeluoikeus, 'oid')
  return opiskeluoikeusOid ? (
    <span className="id">
      <Text name="Opiskeluoikeuden oid" />
      {': '}
      <span className="value" onClick={selectAllText}>
        {opiskeluoikeusOid}
      </span>
    </span>
  ) : null
}

export const OpiskeluoikeudenVoimassaoloaika = ({ opiskeluoikeus }) => {
  const päättymispäiväProperty =
    modelData(opiskeluoikeus, 'arvioituPäättymispäivä') &&
    !modelData(opiskeluoikeus, 'päättymispäivä')
      ? 'arvioituPäättymispäivä'
      : 'päättymispäivä'
  return (
    <div className="alku-loppu opiskeluoikeuden-voimassaoloaika">
      <Text name="Opiskeluoikeuden voimassaoloaika" />
      {': '}
      <span className="alkamispäivä">
        <Editor
          model={addContext(opiskeluoikeus, { edit: false })}
          path="alkamispäivä"
        />
      </span>
      {' — '}
      <span className="päättymispäivä">
        <Editor
          model={addContext(opiskeluoikeus, { edit: false })}
          path={päättymispäiväProperty}
        />
      </span>{' '}
      {päättymispäiväProperty === 'arvioituPäättymispäivä' && (
        <Text name="(arvioitu)" />
      )}
    </div>
  )
}

const Suoritukset = ({ opiskeluoikeus }) => {
  const opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo

  return (
    <div className="suoritukset">
      {opiskeluoikeusTyyppi === 'korkeakoulutus' ? (
        <Korkeakoulusuoritukset opiskeluoikeus={opiskeluoikeus} />
      ) : (
        <TabulatedSuoritukset model={opiskeluoikeus} />
      )}
    </div>
  )
}

const TabulatedSuoritukset = ({ model }) => {
  const suoritukset = modelItems(model, 'suoritukset')
  assignTabNames(suoritukset)

  const index = suoritusTabIndex(suoritukset)
  if (index < 0 || index >= suoritukset.length) {
    navigateTo(urlForTab(suoritukset, index))
    return null
  }
  const valittuSuoritus = suoritukset[index]

  return (
    <div className="suoritukset">
      <h4>
        <Text name="Suoritukset" />
      </h4>
      <SuoritusTabs model={model} suoritukset={suoritukset} />
      <Editor
        key={valittuSuoritus.tabName}
        model={valittuSuoritus}
        alwaysUpdate="true"
      />
    </div>
  )
}

const isPerusopetuksenOppiaineenOppimäärä = (suoritus) =>
  [
    'perusopetuksenoppiaineenoppimaara',
    'nuortenperusopetuksenoppiaineenoppimaara'
  ].includes(suorituksenTyyppi(suoritus))

const isLukionOppiaineenOppimäärä = (suoritus) =>
  suorituksenTyyppi(suoritus) === 'lukionoppiaineenoppimaara'

const isKorkeakoulututkinto = (suoritus) =>
  ['muukorkeakoulunsuoritus', 'korkeakoulututkinto'].includes(
    suorituksenTyyppi(suoritus)
  )
export const isOpintojakso = (suoritus) =>
  suorituksenTyyppi(suoritus) === 'korkeakoulunopintojakso'
const isPerusopetuksenVuosiluokka = (suoritus) =>
  suorituksenTyyppi(suoritus) === 'perusopetuksenvuosiluokka'

// Duplicates the logic from src/main/scala/fi/oph/koski/luovutuspalvelu/SuomiFiService.scala#suorituksenNimi
export const näytettäväPäätasonSuoritusTitle = (opiskeluoikeus) => {
  const suoritukset = modelItems(opiskeluoikeus, 'suoritukset')
  const sisältääKorkeakoulututkinnon = suoritukset.some(isKorkeakoulututkinto)
  const kaikkiOpintojaksoja =
    suoritukset.length > 1 && suoritukset.every(isOpintojakso)
  const kaikkiPerusopetuksenVuosiluokkia = suoritukset.every(
    isPerusopetuksenVuosiluokka
  )
  const sisältääPerusopetuksenOppiaineenOppimäärän = suoritukset.some(
    isPerusopetuksenOppiaineenOppimäärä
  )
  const sisältääLukionOppiaineenOppimäärän = suoritukset.some(
    isLukionOppiaineenOppimäärä
  )

  if (kaikkiOpintojaksoja) {
    return `${suoritukset.length} ${t('opintojaksoa')}`
  } else if (sisältääPerusopetuksenOppiaineenOppimäärän) {
    return t('Perusopetuksen oppiaineen oppimäärä')
  } else if (sisältääLukionOppiaineenOppimäärän) {
    return t('Lukion oppiaineen oppimäärä')
  } else if (kaikkiPerusopetuksenVuosiluokkia) {
    return t('Perusopetus')
  } else if (sisältääKorkeakoulututkinnon) {
    return suoritusTitle(suoritukset.find(isKorkeakoulututkinto))
  } else {
    return suoritusTitle(suoritukset[0])
  }
}
