import React from 'react'
import Oboe from 'oboe'
import R from 'ramda'
import Bacon from 'baconjs'
import delays from './delays'
import Text from './Text.jsx'
import Atom from 'bacon.atom'
import Link from './Link.jsx'

class ValidointiTaulukko extends React.Component {

  constructor(props) {
    super(props)
    this.state = { expandedJsonKeys: [], expandedIdsKeys: []}
  }


  render() {
    let { validationStatus } = this.props
    let { expandedJsonKeys, expandedIdsKeys, message } = this.state

    return (
      <div>
        <div id="message">{message}</div>
        <table>
          <thead>
          <tr><th className="virhetyyppi"><Text name="Virhetyyppi"/></th><th className="virheteksti"><Text name="Virheteksti"/></th><th className="lukumäärä"><Text name="Lukumäärä"/></th></tr>
          </thead>
          <tbody>
          { validationStatus.map(({errors, oids, key}) => {
            let jsonExpanded = expandedJsonKeys.indexOf(key) >= 0
            let idsExpanded = expandedIdsKeys.indexOf(key) >= 0
            return (<tr key={key}>
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
      </div>)
  }

  componentDidMount() {
    document.addEventListener('keyup', this.showSelection)
  }

  componentWillUnmount() {
    document.removeEventListener('keyup', this.showSelection)
  }

  showSelection(e) {
    if (e.keyCode == 27) { // esc
      this.setState({ message: null })
    } else if (e.keyCode == 67) { // C
      let {validationStatus} = this.props
      if (!window.getSelection().focusNode || !window.getSelection().anchorNode) return
      let elementIndex = (el) => Array.prototype.indexOf.call(el.parentElement.children, el)
      var startIndex = elementIndex(window.getSelection().focusNode.parentElement.closest('tr'))
      var endIndex = elementIndex(window.getSelection().anchorNode.parentElement.closest('tr'))
      if (startIndex < 0 || endIndex < 0) return
      if (endIndex < startIndex) {
        var x = endIndex
        endIndex = startIndex
        startIndex = x
      }
      let selectedRows = validationStatus.slice(startIndex, endIndex + 1)
      let selectedIds = selectedRows.flatMap((row) => row.ids)
      var messageElem = document.getElementById('message')
      messageElem.textContent='(' + selectedIds.map((id) => '\'' + id + '\'').join(', ') + ')'
      window.getSelection().selectAllChildren(messageElem)
    }
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
          : <button onClick={() => startedAtom.set(true)}><Text name="Aloita validointi"/></button>
        }
        <ValidointiTaulukko validationStatus={validationStatus}/>
      </div>
    </div>),
    title: ''
  }))

  latestQuery = query
  return latestContent
}
