import React from 'react'
import BaconComponent from '../components/BaconComponent'
import Http from '../util/http'
import Link from '../components/Link'
import { currentLocation, navigateTo } from '../util/location.js'
import { ISO2FinnishDateTime } from '../date/date.js'
import Text from '../i18n/Text'

export default class Versiohistoria extends BaconComponent {
  constructor(props) {
    super(props)
    this.state = this.initialState()
  }

  render() {
    const { opiskeluoikeusOid, oppijaOid } = this.props
    const { showHistory, history } = this.state
    const toggle = (newState) => {
      const newShowHistory = newState === undefined ? !showHistory : newState
      this.setState({ showHistory: newShowHistory })
      if (newShowHistory) this.fetchHistory(this.props.opiskeluoikeusOid)
      if (!newShowHistory && this.versionumero()) {
        navigateTo(`/koski/oppija/${oppijaOid}`)
      }
    }
    const selectedVersion = String(this.versionumero() || history.length)
    return opiskeluoikeusOid ? (
      <div className={'versiohistoria' + (showHistory ? ' open' : '')}>
        <a onClick={() => toggle()}>
          <Text name="Versiohistoria" />
        </a>
        {showHistory && (
          <div className="modal">
            <a onClick={() => toggle(false)} className="close-modal"></a>
            <ol>
              {history.map((version, i) => (
                <li
                  key={i}
                  className={
                    String(version.versionumero) === selectedVersion
                      ? 'selected'
                      : ''
                  }
                >
                  <Link
                    href={`/koski/oppija/${oppijaOid}?opiskeluoikeus=${opiskeluoikeusOid}&versionumero=${version.versionumero}`}
                  >
                    <span className="versionumero">
                      {'v' + version.versionumero}
                    </span>
                    <span className="aikaleima">
                      {ISO2FinnishDateTime(version.aikaleima)}
                    </span>
                  </Link>
                </li>
              ))}
            </ol>
          </div>
        )}
      </div>
    ) : null
  }

  UNSAFE_componentWillMount() {
    super.UNSAFE_componentWillMount()
    this.propsE
      .map('.opiskeluoikeusOid')
      .skipDuplicates()
      .onValue((opiskeluoikeusOid) => {
        this.setState(this.initialState())
        if (this.state.showHistory) this.fetchHistory(opiskeluoikeusOid)
      })
    this.propsE.push(this.props)
  }

  fetchHistory(opiskeluoikeusOid) {
    Http.cachedGet(`/koski/api/opiskeluoikeus/historia/${opiskeluoikeusOid}`)
      .takeUntil(this.unmountE)
      .onValue((h) => this.setState({ history: h }))
  }

  initialState() {
    return { showHistory: !!this.versionumero(), history: [] }
  }

  versionumero() {
    return currentLocation().params.versionumero
  }
}
