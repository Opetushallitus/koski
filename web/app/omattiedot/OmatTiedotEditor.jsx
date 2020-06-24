import React from 'baret'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import {addContext, modelData} from '../editor/EditorModel.js'
import {näytettäväPäätasonSuoritusTitle} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import {modelItems, modelTitle} from '../editor/EditorModel'
import {OpiskeluoikeudenTila} from './fragments/OpiskeluoikeudenTila'
import ChevronUpIcon from '../icons/ChevronUpIcon'
import ChevronDownIcon from '../icons/ChevronDownIcon'
import {OmatTiedotOpiskeluoikeus} from './OmatTiedotOpiskeluoikeus'
import Checkbox from '../components/Checkbox'
import Text from '../i18n/Text'
import Http from '../util/http'
import {showError} from '../util/location'


export const selectedModelsAtom = Atom([])

export const OmatTiedotEditor = ({model}) => {
  const oppijaOid = modelData(model, 'henkilö.oid')
  const oppilaitokset = modelItems(model, 'opiskeluoikeudet')
  return (
    <div className="oppilaitos-list">
      {oppilaitokset.map((oppilaitos, oppilaitosIndex) => (
        <Oppilaitokset
          key={oppilaitosIndex}
          oppilaitos={oppilaitos}
          oppijaOid={oppijaOid}
        />))}
      {selectedModelsAtom.map(selectedModels => (
        <SuoritusjakoButton
          selectedModels={selectedModels}
        />))}
    </div>
  )
}


const SuoritusjakoButton = ({selectedModels}) => {
  const isPending = Atom(false)

  const onSuccess = () => {
    isPending.set(false)
  }

  const onError = (res) => {
    isPending.set(false)
    res?.jsonMessage
      ?.filter(m => m.key === 'unprocessableEntity.liianMontaSuoritusjakoa')
      .map(e => {
        showError(e) // TODO: Näytä virhe käyttäjälle tms
      })
  }

  const jaettavaSuoritus = (model) => {
    const data = modelData(model)
    if (model.value.classes.includes('opiskeluoikeus')) {
      // Malli on kokonainen opiskeluoikeus: palautetaan sellaisenaan
      return data
    } else {
      throw new Error('Tämäntyyppisen suorituksen jako ei tuettu') // TODO
    }
  }

  const jaettavatSuoritukset = (models) => {
    return models.map(model => jaettavaSuoritus(model))
  }

  const createSuoritusjako = () => {
    isPending.set(true)
    const url = '/koski/api/suoritusjakoV2/create'
    const request = jaettavatSuoritukset(selectedModels)
    const response = Http.post(url, request)
    response.onValue(onSuccess)
    response.onError(onError)
  }

  return (
    <div className='create-suoritusjako__button'>
      <button
        className='koski-button'
        disabled={R.isEmpty(selectedModels) || isPending}
        onClick={createSuoritusjako}
      >
        <Text name='Jaa valitsemasi opinnot'/>
      </button>
    </div>
  )
}

const Oppilaitokset = ({oppilaitos, oppijaOid}) => {
  return (
    <div className='oppilaitos-container'>
      <h2 className='oppilaitos-title'>{modelTitle(oppilaitos, 'oppilaitos')}</h2>
      <ul className='opiskeluoikeudet-list'>
        {modelItems(oppilaitos, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) => (
            <li className='opiskeluoikeus-row' key={opiskeluoikeusIndex}>
              <div className='opiskeluoikeus-checkbox-container'>
                {selectedModelsAtom.map(selectedModels => (
                  <Checkbox
                    id={`opiskeluoikeus-check-${opiskeluoikeus.modelId}`}
                    checked={R.contains(opiskeluoikeus, selectedModels)}
                    onChange={
                      e => selectedModelsAtom.modify(atom =>
                        e.target.checked
                          ? R.append(opiskeluoikeus, atom)
                          : R.without([opiskeluoikeus], atom)
                      )
                    }
                    listStylePosition='inside'
                  />
                ))}
              </div>
              <Opiskeluoikeus opiskeluoikeus={opiskeluoikeus} oppijaOid={oppijaOid}/>
          </li>
        ))}
      </ul>
    </div>
  )
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
    this.setState(prevState => ({expanded: !prevState.expanded}))
  }

  render() {
    const {opiskeluoikeus, oppijaOid} = this.props
    const {expanded} = this.state
    const hasAlkamispäivä = !!modelData(opiskeluoikeus, 'alkamispäivä')

    return (
      <div className='opiskeluoikeus-container'>
        <button
          className={`opiskeluoikeus-button ${expanded ? 'opiskeluoikeus-button--selected' : ''}`}
          aria-pressed={expanded}
          onClick={this.toggleExpand}
        >
          <div className='opiskeluoikeus-button-content'>
            <div className='opiskeluoikeus-title'>
              <h3>
                {näytettäväPäätasonSuoritusTitle(opiskeluoikeus)}
                {hasAlkamispäivä && <OpiskeluoikeudenTila opiskeluoikeus={opiskeluoikeus}/>}
              </h3>
            </div>
            <div className='opiskeluoikeus-expand-icon'>
              {expanded
                ? <ChevronUpIcon/>
                : <ChevronDownIcon/>}
            </div>
          </div>
        </button>
        {expanded && <OmatTiedotOpiskeluoikeus model={addContext(opiskeluoikeus, {oppijaOid: oppijaOid})}/>}
      </div>
    )
  }
}
