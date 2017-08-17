import React from 'react'
import BaconComponent from './BaconComponent'
import Http from './http'
import Link from './Link.jsx'
import {currentLocation, navigateTo} from './location.js'
import {ISO2FinnishDateTime} from './date.js'
import Text from './Text.jsx'

export default class Versiohistoria extends BaconComponent {
  constructor(props) {
    super(props)
    this.state = this.initialState()
  }
  render() {
    let { opiskeluoikeusOid, oppijaOid } = this.props
    let { showHistory, history } = this.state
    let toggle = (newState) => {
      let newShowHistory = newState === undefined ? !showHistory : newState
      this.setState({showHistory: newShowHistory})
      if (newShowHistory) this.fetchHistory(this.props.opiskeluoikeusOid)
      if (!newShowHistory && this.versionumero()) {
        navigateTo(`/koski/oppija/${oppijaOid}`)
      }
    }
    let selectedVersion = this.versionumero() || history.length
    return (
      <div className={'versiohistoria'+(showHistory?' open':'')}>
        <a onClick={() => toggle()}><Text name="Versiohistoria"/></a>
        {showHistory && (
          <div className="modal">
            <i onClick={() => toggle(false)} className="close-modal fa fa-times fa-2x"></i>
            <ol>{
              history.map((version, i) =>
                (<li key={i} className={version.versionumero == selectedVersion ? 'selected' : ''}>
                  <Link href={`/koski/oppija/${oppijaOid}?opiskeluoikeus=${opiskeluoikeusOid}&versionumero=${version.versionumero}`}>
                    <span className="versionumero">{'v' + version.versionumero}</span>
                    <span className="aikaleima">{ISO2FinnishDateTime(version.aikaleima)}</span>
                  </Link>
                </li>)
              )
            }</ol>
          </div>
        )}
      </div>
    )
  }
  componentWillMount() {
    super.componentWillMount()
    this.propsE.map('.opiskeluoikeusOid').skipDuplicates().onValue((opiskeluoikeusOid) => {
      this.setState(this.initialState())
      if (this.state.showHistory) this.fetchHistory(opiskeluoikeusOid)
    })
    this.propsE.push(this.props)
  }
  fetchHistory(opiskeluoikeusOid) {
    Http.cachedGet(`/koski/api/opiskeluoikeus/historia/${opiskeluoikeusOid}`)
      .takeUntil(this.unmountE)
      .onValue(h => this.setState({history: h}))
  }
  initialState() {
    return { showHistory: !!this.versionumero(), history: [] }
  }
  versionumero() {
    return currentLocation().params.versionumero
  }
}
