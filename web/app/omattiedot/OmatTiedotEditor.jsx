import React from 'baret'
import {addContext, modelData} from '../editor/EditorModel'
import {näytettäväPäätasonSuoritusTitle} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import {modelItems, modelTitle} from '../editor/EditorModel'
import {OpiskeluoikeudenTila} from './fragments/OpiskeluoikeudenTila'
import ChevronUpIcon from '../icons/ChevronUpIcon'
import ChevronDownIcon from '../icons/ChevronDownIcon'
import {OmatTiedotOpiskeluoikeus} from './OmatTiedotOpiskeluoikeus'


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
    </div>
  )
}


const Oppilaitokset = ({oppilaitos, oppijaOid}) => {
  return (
    <div className='oppilaitos-container'>
      <h2 className='oppilaitos-title'>{modelTitle(oppilaitos, 'oppilaitos')}</h2>
      <ul className='opiskeluoikeudet-list'>
        {modelItems(oppilaitos, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) => (
          <li key={opiskeluoikeusIndex}>
            <Opiskeluoikeus opiskeluoikeus={opiskeluoikeus} oppijaOid={oppijaOid} />
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
        <button className={`opiskeluoikeus-button ${expanded ? 'opiskeluoikeus-button--selected' : ''}`} aria-pressed={expanded} onClick={this.toggleExpand}>
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
