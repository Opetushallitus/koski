import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from './http'
import {navigateToOppija, showError} from './location'
import delays from './delays'
import Link from './Link.jsx'

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

export const OppijaHaku = () => (
  <div className={searchInProgressP.map((searching) => searching ? 'oppija-haku searching' : 'oppija-haku')}>
    <div>
      <h3>Hae tai lisää opiskelija</h3>
      <input type="text" value={searchStringAtom} id='search-query' placeholder='henkilötunnus, nimi tai oppijanumero' onChange={(e) => searchStringAtom.set(e.target.value)}></input>

    </div>
    <div className='hakutulokset'>
      {
        oppijatP.map(({response, query}) => {
          if (response.henkilöt.length > 0) {
            return (<ul>{
              response.henkilöt.map((o, i) =>
                  (<li key={i}>
                    <Link href={`/koski/oppija/${o.oid}`}>{o.sukunimi}, {o.etunimet}{o.hetu && ' (' + o.hetu + ')'}</Link>
                  </li>))
              }
            </ul>)
          }
          if (query.length > 2) {
            let url = '/koski/uusioppija#' + query
            return (<div className='no-results'>
              Ei hakutuloksia
              { response.canAddNew &&
                (<Link href={url} className="lisaa-oppija">
                  Lisää uusi opiskelija
                </Link>)
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
