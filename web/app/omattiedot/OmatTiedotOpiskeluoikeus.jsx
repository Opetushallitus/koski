import React from 'baret'
import Bacon from 'baconjs'
import {
  addContext,
  modelData,
  modelItems,
  modelLookup,
  modelProperties
} from '../editor/EditorModel'
import {
  excludedProperties,
  OpiskeluoikeudenVoimassaoloaika
} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { OpiskeluoikeudenTilaEditor } from '../opiskeluoikeus/OpiskeluoikeudenTilaEditor'
import { assignTabNames } from '../suoritus/SuoritusTabs'
import { Korkeakoulusuoritukset } from '../virta/Korkeakoulusuoritukset'
import Text from '../i18n/Text'
import { Editor } from '../editor/Editor'
import { suorituksenTyyppi, suoritusTitle } from '../suoritus/Suoritus'
import { focusWithoutScrolling } from '../util/util'
import { SuostumuksenPeruutusPopup } from './SuostumuksenPeruutusPopup'
import Atom from 'bacon.atom'
import Http from '../util/http'

export class OmatTiedotOpiskeluoikeus extends React.Component {
  componentDidMount() {
    focusWithoutScrolling(this.opiskeluoikeusContent)
  }

  render() {
    const { model } = this.props
    const mdl = addContext(model, { opiskeluoikeus: model })
    const isSyntheticOpiskeluoikeus = !!modelData(mdl, 'synteettinen')

    const suostumusPeruutettavaaTyyppiä = modelLookup(mdl, 'suoritukset.0')
      ? !!modelLookup(mdl, 'suoritukset.0').value.classes.includes(
          'suostumusperuttavissaopiskeluoikeudelta'
        )
      : false

    return (
      <div className="opiskeluoikeus">
        {suostumusPeruutettavaaTyyppiä && (
          <OpiskeluoikeudenSuostumuksenPeruminen opiskeluoikeus={mdl} />
        )}
        <div
          className="opiskeluoikeus-content"
          ref={(elm) => (this.opiskeluoikeusContent = elm)}
          tabIndex="-1"
        >
          {!isSyntheticOpiskeluoikeus && (
            <OpiskeluoikeudenTiedot opiskeluoikeus={mdl} />
          )}
          <Suoritukset opiskeluoikeus={mdl} />
        </div>
      </div>
    )
  }
}

const OpiskeluoikeudenSuostumuksenPeruminen = ({ opiskeluoikeus }) => {
  const peruuttamassaSuostumustaAtom = Atom(false)
  const suostumuksenPerumisenInfoAtom = Atom(false)
  const suoritusjakoTehty = Atom(true)

  const opiskeluoikeusOid = modelData(opiskeluoikeus, 'oid')
  Http.post(
    `/koski/api/opiskeluoikeus/suostumuksenperuutus/suoritusjakoTehty/${opiskeluoikeusOid}`,
    {},
    { errorMapper: (e) => (e.httpStatus === 401 ? null : new Bacon.Error(e)) }
  ).onValue((v) => suoritusjakoTehty.modify(() => v.tehty))

  return (
    <div className="suostumuksen-peruuttaminen">
      <b>
        <Text
          className="bold"
          name="Tämän opiskeluoikeuden tiedot näytetään antamasi suostumuksen perusteella."
        />
      </b>
      <span className="infobox">
        <span
          className="info-icon"
          onClick={() => suostumuksenPerumisenInfoAtom.modify((x) => !x)}
          onMouseEnter={() => suostumuksenPerumisenInfoAtom.modify(() => true)}
          onMouseLeave={() => suostumuksenPerumisenInfoAtom.modify(() => false)}
        />
      </span>
      {suoritusjakoTehty.map(
        (v) =>
          !v && (
            <a
              className="peru-suostumus-linkki"
              onClick={() => peruuttamassaSuostumustaAtom.modify((x) => !x)}
            >
              {'Peruuta suostumus'}
            </a>
          )
      )}
      {peruuttamassaSuostumustaAtom.map(
        (peruuttamassa) =>
          peruuttamassa && (
            <SuostumuksenPeruutusPopup
              opiskeluoikeusOid={opiskeluoikeusOid}
              onDismiss={() => peruuttamassaSuostumustaAtom.modify((x) => !x)}
            />
          )
      )}
      {suostumuksenPerumisenInfoAtom.map(
        (info) =>
          info && (
            <div
              className={'suostumuksen-perumisen-info modal'}
              role="dialog"
              aria-modal={true}
              aria-describedby="modal-main-content"
            >
              <div className="modal-content">
                <div className="modal-main-content">
                  <Text name="tooltip:Suostumuksen selitys" />
                </div>
              </div>
            </div>
          )
      )}
    </div>
  )
}

const OpiskeluoikeudenTiedot = ({ opiskeluoikeus }) => {
  const additionalInformation = modelLookup(opiskeluoikeus, 'lisätiedot')
  const additionalInfoPropertyFilter = (prop) => !!modelData(prop.model)
  const omatTiedotExcludedProperties = excludedProperties.concat([
    'ostettu',
    'organisaatiohistoria'
  ])

  return (
    <div className="opiskeluoikeuden-tiedot">
      {modelData(opiskeluoikeus, 'alkamispäivä') && (
        <OpiskeluoikeudenVoimassaoloaika opiskeluoikeus={opiskeluoikeus} />
      )}
      <PropertiesEditor
        model={opiskeluoikeus}
        propertyFilter={(p) => !omatTiedotExcludedProperties.includes(p.key)}
        getValueEditor={(prop, getDefault) => {
          switch (prop.key) {
            case 'tila':
              return <OpiskeluoikeudenTilaEditor model={opiskeluoikeus} />
            default:
              return getDefault()
          }
        }}
        className="kansalainen"
      />
      {additionalInformation &&
        modelProperties(additionalInformation).filter(
          additionalInfoPropertyFilter
        ).length > 0 && (
          <OpiskeluoikeudenLisätiedot
            model={opiskeluoikeus}
            propertyFilter={additionalInfoPropertyFilter}
          />
        )}
    </div>
  )
}

class OpiskeluoikeudenLisätiedot extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      expanded: false
    }
    this.toggleExpand = this.toggleExpand.bind(this)
  }

  toggleExpand() {
    this.setState((prevState) => ({ expanded: !prevState.expanded }))
  }

  render() {
    const { model, propertyFilter } = this.props
    const { expanded } = this.state

    return (
      <div className="expandable-container lisätiedot">
        <button
          className={`inline-link-button ${expanded ? 'open' : ''}`}
          onClick={this.toggleExpand}
          aria-pressed={expanded}
        >
          <Text name="Lisätiedot" />
        </button>
        {expanded && (
          <div className="value">
            <PropertiesEditor
              model={modelLookup(model, 'lisätiedot')}
              propertyFilter={propertyFilter}
              className="kansalainen"
            />
          </div>
        )}
      </div>
    )
  }
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

class TabulatedSuoritukset extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      selectedTabIndex: 0
    }
    this.onTabChange = this.onTabChange.bind(this)
  }

  onTabChange(newTabIndex) {
    this.setState({ selectedTabIndex: newTabIndex })
  }

  render() {
    const { model } = this.props
    const { selectedTabIndex } = this.state

    const suoritukset = modelItems(model, 'suoritukset')
    assignTabNames(suoritukset)

    const valittuSuoritus = suoritukset[selectedTabIndex]

    return (
      <div className="suoritukset">
        <SuoritusTabs
          selectedTabIndex={selectedTabIndex}
          suoritukset={suoritukset}
          onChange={this.onTabChange}
        />
        <Editor
          key={valittuSuoritus.tabName}
          model={valittuSuoritus}
          alwaysUpdate="true"
        />
      </div>
    )
  }
}

const SuoritusTabs = ({ selectedTabIndex, suoritukset, onChange }) => {
  const tabTitle = (suoritusModel) =>
    suorituksenTyyppi(suoritusModel) == 'perusopetuksenoppimaara' ? (
      <Text name="Päättötodistus" />
    ) : (
      suoritusTitle(suoritusModel)
    )
  return (
    <div className="suoritus-tabs" role="tablist" aria-label="Suoritukset">
      <ul>
        {suoritukset.map((suoritus, i) => {
          const selected = selectedTabIndex === i
          const titleEditor = tabTitle(suoritus)
          const onClick = () => onChange(i)
          return (
            <li
              className={selected ? 'tab selected' : 'tab'}
              key={i}
              role="tab"
              aria-label={`${selected ? 'Valittu ' : ''}${
                typeof titleEditor === 'string'
                  ? titleEditor
                  : `Suoritus ${i + 1}`
              }`}
            >
              {selected ? (
                titleEditor
              ) : (
                <button className="inline-link-button" onClick={onClick}>
                  {' '}
                  {titleEditor}{' '}
                </button>
              )}
            </li>
          )
        })}
      </ul>
    </div>
  )
}
