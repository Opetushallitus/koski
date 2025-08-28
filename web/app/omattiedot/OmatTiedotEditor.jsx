import React from 'baret'
import {
  addContext,
  modelData,
  modelItems,
  modelTitle
} from '../editor/EditorModel'
import {
  näytettäväPäätasonSuoritusTitle,
  OpiskeluoikeudenId
} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import { OpiskeluoikeudenTila } from './fragments/OpiskeluoikeudenTila'
import ChevronUpIcon from '../icons/ChevronUpIcon'
import ChevronDownIcon from '../icons/ChevronDownIcon'
import { OmatTiedotOpiskeluoikeus } from './OmatTiedotOpiskeluoikeus'
import { useKansalainenUiAdapter } from '../components-v2/interoperability/useUiAdapter'
import { t } from '../i18n/i18n'
import { TestIdLayer, TestIdRoot } from '../appstate/useTestId'
import { groupBy } from 'fp-ts/lib/ReadonlyNonEmptyArray'

export const OmatTiedotEditor = ({ model }) => {
  const oppijaOid = modelData(model, 'henkilö.oid')
  const oppilaitokset = modelItems(model, 'opiskeluoikeudet')
  const uiAdapter = useKansalainenUiAdapter(model)

  const osaamismerkit = oppilaitokset.flatMap((oppilaitos) =>
    modelItems(oppilaitos, 'opiskeluoikeudet').filter(
      onOsaamismerkinOpiskeluoikeus
    )
  )

  const oppilaitoksettomatOpiskeluoikeudet = Object.entries(
    groupBy((a) => modelTitle(a, 'suoritukset.0.tyyppi'))(
      oppilaitokset.flatMap((oppilaitos) =>
        modelItems(oppilaitos, 'opiskeluoikeudet').filter(
          onOppilaitoksetonOpiskeluoikeus
        )
      )
    )
  )

  return (
    <div className="oppilaitos-list">
      {!uiAdapter.isLoadingV2 && (
        <>
          {oppilaitokset.map((oppilaitos, oppilaitosIndex) => (
            <Oppilaitokset
              key={oppilaitosIndex}
              oppilaitos={oppilaitos}
              oppijaOid={oppijaOid}
              uiAdapter={uiAdapter}
            />
          ))}
          <TestIdRoot id="oppilaitoksettomat-opiskeluoikeudet">
            {oppilaitoksettomatOpiskeluoikeudet.map(
              ([title, opiskeluoikeudet], index) => (
                <div className="oppilaitos-container" key={index}>
                  <h2 className="oppilaitos-title">{title}</h2>
                  <ul className="opiskeluoikeudet-list">
                    {opiskeluoikeudet.map(
                      (opiskeluoikeus, opiskeluoikeusIndex) => (
                        <li key={opiskeluoikeusIndex}>
                          <TestIdLayer id={opiskeluoikeusIndex}>
                            <div className="opiskeluoikeus-container">
                              <OpiskeluoikeusEditor
                                opiskeluoikeus={opiskeluoikeus}
                                oppijaOid={oppijaOid}
                                uiAdapter={uiAdapter}
                                testId={opiskeluoikeusIndex}
                              />
                            </div>
                          </TestIdLayer>
                        </li>
                      )
                    )}
                  </ul>
                </div>
              )
            )}
          </TestIdRoot>
          {osaamismerkit.length > 0 ? (
            <TestIdRoot id="osaamismerkit">
              <div className="oppilaitos-container">
                <h2 className="oppilaitos-title">{t('Osaamismerkit')}</h2>
                <ul className="opiskeluoikeudet-list">
                  {osaamismerkit.map((opiskeluoikeus, opiskeluoikeusIndex) => (
                    <li key={opiskeluoikeusIndex}>
                      <TestIdLayer id={opiskeluoikeusIndex}>
                        <div className="opiskeluoikeus-container">
                          <Osaamismerkki
                            key={opiskeluoikeusIndex}
                            opiskeluoikeus={opiskeluoikeus}
                            opiskeluoikeusIndex={opiskeluoikeusIndex}
                            oppijaOid={oppijaOid}
                            uiAdapter={uiAdapter}
                          />
                        </div>
                      </TestIdLayer>
                    </li>
                  ))}
                </ul>
              </div>
            </TestIdRoot>
          ) : null}
        </>
      )}
    </div>
  )
}

const onOsaamismerkinOpiskeluoikeus = (opiskeluoikeus) => {
  const suoritukset = modelItems(opiskeluoikeus, 'suoritukset')
  const suoritus = suoritukset[0]
  const suoritustyyppi = suoritus && modelData(suoritus, 'tyyppi.koodiarvo')

  return suoritustyyppi === 'vstosaamismerkki'
}

const onOppilaitoksetonOpiskeluoikeus = (opiskeluoikeus) => {
  const oppilaitosnumero = modelData(
    opiskeluoikeus,
    'oppilaitos.oppilaitosnumero.koodiarvo'
  )
  const koulutustoimijaOid = modelData(opiskeluoikeus, 'koulutustoimija.oid')
  return (
    !oppilaitosnumero?.match(/\d+/) &&
    koulutustoimijaOid == '1.2.246.562.10.48587687889' // Opetushallitus
  )
}

const Oppilaitokset = ({ oppilaitos, oppijaOid, uiAdapter }) => {
  const opiskeluoikeudet = modelItems(oppilaitos, 'opiskeluoikeudet').filter(
    (oo) =>
      !onOsaamismerkinOpiskeluoikeus(oo) && !onOppilaitoksetonOpiskeluoikeus(oo)
  )
  return opiskeluoikeudet.length > 0 ? (
    <TestIdRoot id="oo">
      <div className="oppilaitos-container">
        <h2 className="oppilaitos-title">
          {modelTitle(oppilaitos, 'oppilaitos')}
        </h2>
        <ul className="opiskeluoikeudet-list">
          {opiskeluoikeudet.map((opiskeluoikeus, opiskeluoikeusIndex) => (
            <OpiskeluoikeusEditor
              key={opiskeluoikeusIndex}
              opiskeluoikeus={opiskeluoikeus}
              oppijaOid={oppijaOid}
              uiAdapter={uiAdapter}
              testId={opiskeluoikeusIndex}
            />
          ))}
        </ul>
      </div>
    </TestIdRoot>
  ) : null
}

const OpiskeluoikeusEditor = ({
  opiskeluoikeus,
  oppijaOid,
  uiAdapter,
  testId
}) => {
  const Editor = uiAdapter.getOpiskeluoikeusEditor(opiskeluoikeus)
  return (
    <TestIdLayer id={testId}>
      {Editor ? (
        <li key={testId}>
          <div className="opiskeluoikeus-container">
            <Editor key={testId} />
          </div>
        </li>
      ) : (
        <li key={testId}>
          <Opiskeluoikeus
            opiskeluoikeus={opiskeluoikeus}
            oppijaOid={oppijaOid}
          />
        </li>
      )}
    </TestIdLayer>
  )
}

const Osaamismerkki = ({ opiskeluoikeus, opiskeluoikeusIndex, uiAdapter }) => {
  const Editor = uiAdapter.getOpiskeluoikeusEditor(opiskeluoikeus)

  return Editor ? <Editor key={opiskeluoikeusIndex} /> : null
}

class Opiskeluoikeus extends React.Component {
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
    const { opiskeluoikeus, oppijaOid } = this.props
    const { expanded } = this.state
    const hasAlkamispäivä = !!modelData(opiskeluoikeus, 'alkamispäivä')

    return (
      <div className="opiskeluoikeus-container">
        <button
          className={`opiskeluoikeus-button ${
            expanded ? 'opiskeluoikeus-button--selected' : ''
          }`}
          aria-pressed={expanded}
          onClick={this.toggleExpand}
        >
          <div className="opiskeluoikeus-button-content">
            <div className="opiskeluoikeus-title">
              <h3>
                {näytettäväPäätasonSuoritusTitle(opiskeluoikeus)}
                {hasAlkamispäivä && (
                  <OpiskeluoikeudenTila opiskeluoikeus={opiskeluoikeus} />
                )}
              </h3>
              {
                <span>
                  <OpiskeluoikeudenId opiskeluoikeus={opiskeluoikeus} />
                </span>
              }
            </div>
            <div className="opiskeluoikeus-expand-icon">
              {expanded ? <ChevronUpIcon /> : <ChevronDownIcon />}
            </div>
          </div>
        </button>
        {expanded && (
          <OmatTiedotOpiskeluoikeus
            model={addContext(opiskeluoikeus, { oppijaOid })}
          />
        )}
      </div>
    )
  }
}
