import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from './http'
import {navigateToOppija, navigateTo} from './location'
import {showError} from './location'

const searchStringAtom = Atom('')
const oppijaHakuE = searchStringAtom.changes()

const acceptableQuery = (q) => q.length >= 3

const hakuTulosE = oppijaHakuE.debounce(500)
  .flatMapLatest(query => (acceptableQuery(query) ? Http.get(`/koski/api/henkilo/search?query=${query}`) : Bacon.once({henkilöt: []})).map((results) => ({ results, query })))

hakuTulosE.onError(showError)

const oppijatP = Bacon.update(
  { query: '', results: { henkilöt: []} },
  hakuTulosE.skipErrors(), ((current, hakutulos) => hakutulos)
)

const searchInProgressP = oppijaHakuE.awaiting(hakuTulosE.mapError()).throttle(200)

export const OppijaHaku = () => (
  <div className={searchInProgressP.map((searching) => searching ? 'oppija-haku searching' : 'oppija-haku')}>
    <div>
      <h3>Hae tai lisää opiskelija</h3>
      <input type="text" value={searchStringAtom} id='search-query' placeholder='henkilötunnus, nimi tai oppijanumero' onChange={(e) => searchStringAtom.set(e.target.value)}></input>

    </div>
    <div className='hakutulokset'>
      {
        oppijatP.map(response => {
          if (response.results.henkilöt.length > 0) {
            return <ul>{
              response.results.henkilöt.map((o, i) =>
                  <li key={i}>
                    <a href={`/koski/oppija/${o.oid}`} onClick={(e) => navigateToOppija(o, e)}>{o.sukunimi}, {o.etunimet} ({o.hetu})</a>
                  </li>)
              }
            </ul>
          }
          if (response.query.length > 2) {
            let url = '/koski/uusioppija/' + response.query
            return <div className='no-results'>
              Ei hakutuloksia
              { response.results.canAddNew &&
                <a href={url} className="lisaa-oppija" onClick={(e) => navigateTo(url, e)}>
                  Lisää uusi opiskelija
                </a>
              }

            </div>
          }
        })
      }
    </div>
  </div>
)
