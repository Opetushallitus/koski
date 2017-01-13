import React from 'react'
import {showInternalError, currentLocation} from './location.js'
import Http from './http'
import { ISO2FinnishDateTime } from './date.js'
import Link from './Link.jsx'

export default React.createClass({
  render() {
    let { opiskeluOikeusId, oppijaOid } = this.props
    let { showHistory, history } = this.state || { showHistory: false }
    let toggle = () => {
      this.setState({showHistory: !showHistory})
      if (!this.historyP) {
        this.historyP = Http.cachedGet(`/koski/api/opiskeluoikeus/historia/${opiskeluOikeusId}`).doError(showInternalError)
        this.historyP.onValue(h => this.setState({history: h}))
      }
    }
    let versions = (this.state || {}).history || []
    let selectedVersion = currentLocation().params.versionumero || versions.length
    return (<div className="versiohistoria">
      <a className="versiohistoria" onClick={toggle}>versiohistoria</a>
      {
        showHistory && (<table> {
          (versions).map((version) =>
            <tr key={version.versionumero} className={version.versionumero == selectedVersion ? 'selected' : ''}>
              <td className="versionumero">{version.versionumero}</td>
              <td className="aikaleima"><Link href={`/koski/oppija/${oppijaOid}?opiskeluoikeus=${opiskeluOikeusId}&versionumero=${version.versionumero}`}>{ISO2FinnishDateTime(version.aikaleima)}</Link></td>
            </tr>
          )
        }
        </table>)
      }
    </div>)
  }
})
