import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from './http'
import {showError, navigateTo} from './location'
import delays from './delays'
import Link from './Link.jsx'
import Highlight from 'react-highlighter'

const searchStringAtom = Atom('')
const oppijaHakuE = searchStringAtom.changes()

const acceptableQuery = (q) => q.length >= 3

const hakuTulosE = oppijaHakuE.debounce(delays().delay(500))
  .flatMapLatest(query => (acceptableQuery(query) ? Http.get(`/koski/api/henkilo/search?query=${query}`, { willHandleErrors: true }) : Bacon.once({henkilöt: []})).map((response) => ({ response, query })))

hakuTulosE.onError(showError)

const oppijatP = Bacon.update(
  { query: '', response: { henkilöt: []} },
  hakuTulosE.skipErrors(), ((current, hakutulos) => hakutulos)
)

const searchInProgressP = oppijaHakuE.awaiting(hakuTulosE.mapError()).throttle(delays().delay(200))

export const OppijaHaku = () => {
  let selectedIndexAtom = Atom(-1)

  let onKeyDown = (options) => (e) => {
    let keyHandlers = {
      ArrowUp: () => {
        selectedIndexAtom.modify((i) => Math.max(i - 1, -1))
      },
      ArrowDown: () => {
        selectedIndexAtom.modify((i) => Math.min(i + 1, options.length -1))
      },
      Enter: () => {
        let option = options[selectedIndexAtom.get()]
        option && navigateTo(option.href)
      }
    }
    if (e.getModifierState('Shift') || e.getModifierState('Alt') || e.getModifierState('Meta') || e.getModifierState('Control')) return
    let handler = keyHandlers[e.key]
    if (handler) {
      e.preventDefault()
      handler()
    }
  }

  let optionsP = Bacon.combineWith(oppijatP, selectedIndexAtom, ({response, query}, selectedIndex) => {
    if (response.henkilöt.length > 0) {
      return response.henkilöt.map((o, i) => {
        let href = `/koski/oppija/${o.oid}`
        let oppija = o.sukunimi +', '+ o.etunimet + (o.hetu ? ' (' + o.hetu + ')' : '')
        return {href, element: (<li className={i == selectedIndex ? 'selected' : ''} key={i}>
          <Link href={href}><Highlight search={query}>{oppija}</Highlight></Link>
        </li>)}
      })
    } else if (response.canAddNew) {
      let hetuQuery = response.hetu ? (encodeURIComponent('hetu') + '=' + encodeURIComponent(response.hetu)) : ''
      let oidQuery = response.oid ? (encodeURIComponent('oid') + '=' + encodeURIComponent(response.oid)) : ''
      let href = '/koski/uusioppija#' + hetuQuery + ((hetuQuery && oidQuery) ? '&' : '') + (oidQuery ? oidQuery : '')
      return [{href, element: (<Link baret-lift className={selectedIndexAtom.map(i => 'lisaa-oppija' +(i == 0 ? ' selected' : ''))} href={href}>
        Lisää uusi opiskelija
      </Link>)}]
    } else {
      return []
    }
  })

  return (<div className={searchInProgressP.map((searching) => searching ? 'oppija-haku searching' : 'oppija-haku')}>
      <div>
        <h3>Hae tai lisää opiskelija</h3>
        <input type="text" value={searchStringAtom} id='search-query' placeholder='henkilötunnus, nimi tai oppijanumero' onChange={(e) => searchStringAtom.set(e.target.value)} onKeyDown={optionsP.map(onKeyDown)} autoFocus></input>
      </div>
      <div className='hakutulokset'>
        {
          Bacon.combineWith(oppijatP, optionsP, ({response, query}, options) => {
            if (response.henkilöt.length > 0) {
              return (<ul>{
                options.map(option => option.element)
              }
              </ul>)
            }
            if (query.length > 2) {
              return (<div className='no-results'>
                Ei hakutuloksia
                {
                  options.map(option => option.element)
                }
                {
                  response.error &&
                  <div className="error">{response.error}</div>
                }

              </div>)
            }
          })
        }
      </div>
    </div>
  )
}
