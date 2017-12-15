import React from 'react'
import Oboe from 'oboe'
import R from 'ramda'
import Bacon from 'baconjs'
import delays from '../util/delays'
import Text from '../i18n/Text'
import Atom from 'bacon.atom'
import Link from '../components/Link'
import {userP} from '../util/user'
class ValidointiTaulukko extends React.Component {

  constructor(props) {
    super(props)
    this.state = { expandedJsonKeys: [], expandedIdsKeys: [], selectedRows: []}
  }

  render() {
    let { validationStatus } = this.props
    let { expandedJsonKeys, expandedIdsKeys } = this.state

    return (
      <div className="validointi-taulukko" onMouseUp={this.updateSelection.bind(this)} >
        <table>
          <thead>
          <tr><th className="virhetyyppi"><Text name="Virhetyyppi"/></th><th className="virheteksti"><Text name="Virheteksti"/></th><th className="lukumäärä"><Text name="Lukumäärä"/></th></tr>
          </thead>
          <tbody>
          { validationStatus.map(({errors, oids, key}) => {
            let jsonExpanded = expandedJsonKeys.indexOf(key) >= 0
            let idsExpanded = expandedIdsKeys.indexOf(key) >= 0
            return (<tr className="row" key={key}>
              <td className="virhetyyppi">{
                errors.length
                  ? errors.map((error, i) => <div key={i}>{error.key}</div>)
                  : <Text name='Virheetön'/>
              }</td>
              <td className="virheteksti">{errors.map((error, i) => {
                let errorMessage = typeof error.message == 'string'
                  ? <div>{error.message}</div>
                  : (jsonExpanded ? <pre className="json"><code onClick={() => this.setState({expandedJsonKeys: expandedJsonKeys.filter((k) => k != key)})}>{JSON.stringify(error.message, null, 2)}</code></pre> : <a onClick={() => this.setState({ expandedJsonKeys: expandedJsonKeys.concat(key) })}><Text name="Näytä JSON"/></a>)
                return <span key={i}>{errorMessage}</span>
              })}</td>
              <td className="lukumäärä">{
                idsExpanded
                  ? <div>
                    <a onClick={() => this.setState({expandedIdsKeys: expandedIdsKeys.filter((k) => k != key)})}><Text name="Yhteensä"/>{' ' + oids.length}</a>
                    <ul className="oids">
                    { oids.map((oid, i) => <li key={i}><Link href={ '/koski/oppija/' + oid.henkilöOid }>{oid.opiskeluoikeusOid}</Link></li>)}
                    </ul></div>
                  : <a onClick={() => this.setState({ expandedIdsKeys: expandedIdsKeys.concat(key)})}>{oids.length}</a>
              }
              </td>
            </tr>)
          })}
          </tbody>
        </table>
        {
          this.state.selectedRows.length ? (<div className="oids">
              <button className="show-oids" onClick={() => this.setState({showOids: !this.state.showOids})}>{'Näytä oidit'}</button>
              { this.state.showOids && <div className="oid-list">{ '(' + this.state.selectedRows.flatMap((row) => row.oids).map(oids => oids.opiskeluoikeusOid).map((id) => '\'' + id + '\'').join(', ') + ')' }</div>}
            </div>)
            : null
        }
      </div>)
  }

  updateSelection() {
    const getSelectedRows = () => {
      let {validationStatus} = this.props
      if (!window.getSelection().focusNode || !window.getSelection().anchorNode) return []
      let elementIndex = (el) => el ? Array.prototype.indexOf.call(el.parentElement.children, el) : -1
      let closestRow = (el) => el.closest ? el.closest('tr.row') : closestRow(el.parentElement)
      var startIndex = elementIndex(closestRow(window.getSelection().focusNode))
      var endIndex = elementIndex(closestRow(window.getSelection().anchorNode))
      if (startIndex < 0 || endIndex < 0) return []
      if (endIndex < startIndex) {
        var x = endIndex
        endIndex = startIndex
        startIndex = x
      }
      return validationStatus.slice(startIndex, endIndex + 1)
    }
    userP.filter('.hasGlobalReadAccess').forEach(() => {
      this.setState({selectedRows: getSelectedRows(), showOids: false})
    })
  }
}

let latestQuery = undefined
let latestContent = undefined

export const validointiContentP = (query) => {
  if (query == latestQuery) return latestContent
  let startedAtom = Atom(false)
  let oboeBus = Bacon.Bus()
  startedAtom.changes().filter(R.identity).onValue(() =>
    Oboe('/koski/api/opiskeluoikeus/validate' + query)
      .node('{errors opiskeluoikeusOid}', (x) => oboeBus.push(x))
      .done(() => oboeBus.end())
      .fail((e) => oboeBus.error(e))
  )

  var keyCounter = 0
  let oidsOf = ({ henkilöOid, opiskeluoikeusOid }) => ({henkilöOid, opiskeluoikeusOid})
  let validationStatusP = oboeBus.scan([], (grouped, validationResult) => {
    for (var i in grouped) {
      if (R.equals(grouped[i].errors, validationResult.errors)) {
        grouped[i].oids.push(oidsOf(validationResult))
        return grouped
      }
    }
    grouped.push({
      errors: validationResult.errors,
      key: ++keyCounter,
      oids: [oidsOf(validationResult)]
    })
    return grouped
  }).throttle(delays().delay(1000)).map(R.sortBy((row) => -row.oids.length))

  let validationFinishedP = validationStatusP.filter(false).mapEnd(true).startWith(false)

  latestContent = Bacon.combineWith(validationStatusP, startedAtom, validationFinishedP, (validationStatus, started, finished) => ({
    content: (<div className='content-area validaatio'>
      <div className="main-content">
        <h2><Text name="Tiedon validointi"/></h2>
        { started
          ? (finished
            ? <Text name="Kaikki opiskeluoikeudet validoitu."/>
            : <Text name="Odota, tietoja validoidaan."/>)
          : <button className="aloita" onClick={() => startedAtom.set(true)}><Text name="Aloita validointi"/></button>
        }
        <ValidointiTaulukko validationStatus={validationStatus}/>
      </div>
    </div>),
    title: ''
  }))

  latestQuery = query
  return latestContent
}
