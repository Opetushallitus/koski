import React from 'react'
import {showInternalError, currentLocation} from './location.js'
import Http from './http'
import { ISO2FinnishDateTime } from './date.js'
import Link from './Link.jsx'

export default React.createClass({
  render() {
    let { opiskeluOikeusId, oppijaOid } = this.props
    let { showHistory, history } = this.state
    let toggle = () => {
      this.setState({showHistory: !showHistory})
      this.fetchHistory()
    }
    let selectedVersion = this.versionumero() || history.length
    return (<div className="versiohistoria">
      <a className="versiohistoria" onClick={toggle}>versiohistoria</a>
      {
        showHistory && (<table><tbody>{
          history.map((version) =>
            <tr key={version.versionumero} className={version.versionumero == selectedVersion ? 'selected' : ''}>
              <td className="versionumero">{version.versionumero}</td>
              <td className="aikaleima"><Link href={`/koski/oppija/${oppijaOid}?opiskeluoikeus=${opiskeluOikeusId}&versionumero=${version.versionumero}`}>{ISO2FinnishDateTime(version.aikaleima)}</Link></td>
            </tr>
          )
        }</tbody></table>)
      }
    </div>)
  },
  getInitialState() {
    return { showHistory: !!this.versionumero(), history: [] }
  },
  componentWillMount() {
    this.fetchHistory()
  },
  fetchHistory() {
    if (this.state.showHistory && !this.historyP) {
      this.historyP = Http.cachedGet(`/koski/api/opiskeluoikeus/historia/${this.props.opiskeluOikeusId}`).doError(showInternalError)
      this.historyP.onValue(h => this.setState({history: h}))
    }
  },
  versionumero() {
    return currentLocation().params.versionumero
  }
})
