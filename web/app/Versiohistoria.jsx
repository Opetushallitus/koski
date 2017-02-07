import React from 'react'
import BaconComponent from './BaconComponent'
import Http from './http'
import Link from './Link.jsx'

import {showInternalError, currentLocation} from './location.js'
import { ISO2FinnishDateTime } from './date.js'

export default BaconComponent({
  render() {
    let { opiskeluOikeusId, oppijaOid } = this.props
    let { showHistory, history } = this.state
    let toggle = () => {
      let newShowHistory = !showHistory
      this.setState({showHistory: newShowHistory})
      if (newShowHistory) this.fetchHistory()
    }
    let selectedVersion = this.versionumero() || history.length
    return (<div className="versiohistoria">
      <a onClick={toggle}>versiohistoria</a>
      {
        showHistory && (<table><tbody>{
          history.map((version, i) =>
            <tr key={i} className={version.versionumero == selectedVersion ? 'selected' : ''}>
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
    if (this.state.showHistory) this.fetchHistory()
  },
  fetchHistory() {
    if (!this.historyP) {
      this.historyP = Http.cachedGet(`/koski/api/opiskeluoikeus/historia/${this.props.opiskeluOikeusId}`).doError(showInternalError)
      this.historyP.takeUntil(this.unmountE).onValue(h => this.setState({history: h}))
    }
  },
  versionumero() {
    return currentLocation().params.versionumero
  }
})
