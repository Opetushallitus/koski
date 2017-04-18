import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from './http'
import {navigateToOppija, navigateTo, showError} from './location'
import delays from './delays'
const searchStringAtom = Atom('')
const oppijaHakuE = searchStringAtom.changes()

const acceptableQuery = (q) => q.length >= 3

const hakuTulosE = oppijaHakuE.debounce(delays().delay(500))
  .flatMapLatest(query => (acceptableQuery(query) ? Http.get(`/koski/api/henkilo/search?query=${query}`) : Bacon.once({henkilöt: []})).map((response) => ({ response, query })))

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
                    <a href={`/koski/oppija/${o.oid}`} onClick={(e) => navigateToOppija(o, e)}>{o.sukunimi}, {o.etunimet} ({o.hetu})</a>
                  </li>))
              }
            </ul>)
          }
          if (query.length > 2) {
            let url = '/koski/uusioppija#' + query
            return (<div className='no-results'>
              Ei hakutuloksia
              { response.canAddNew &&
                (<a href={url} className="lisaa-oppija" onClick={(e) => navigateTo(url, e)}>
                  Lisää uusi opiskelija
                </a>)
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
