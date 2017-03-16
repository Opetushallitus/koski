import React from 'baret'
import Bacon from 'baconjs'
import Http from './http'
import {navigateToOppija, navigateTo} from './location'
import {showError} from './location'
import {validateHetu} from './hetu'

const oppijaHakuE = new Bacon.Bus()

const acceptableQuery = (q) => q.length >= 3

const hakuTulosE = oppijaHakuE.debounce(500)
  .flatMapLatest(q => (acceptableQuery(q) ? Http.get(`/koski/api/henkilo/search?query=${q}`) : Bacon.once([])).map((oppijat) => ({ results: oppijat, query: q })))

hakuTulosE.onError(showError)

const oppijatP = Bacon.update(
  { query: '', results: [] },
  hakuTulosE.skipErrors(), ((current, hakutulos) => hakutulos)
)

const searchInProgressP = oppijaHakuE.filter(acceptableQuery).awaiting(oppijatP.mapError().changes()).throttle(200)

const hetuP = oppijatP.map(({query}) => query)
const hetuValidP = hetuP.map((hetu) => validateHetu(hetu).length == 0)
const canAddP = searchInProgressP.not().and(hetuValidP).and(oppijatP.map(({results}) => results.length == 0))
const uusiOppijaUrlP = canAddP.and(hetuP.map(hetu => '/koski/uusioppija/' + hetu))

export const OppijaHaku = () => (
  <div className={searchInProgressP.map((searching) => searching ? 'oppija-haku searching' : 'oppija-haku')}>
    <div>
      <h3>Hae tai lisää opiskelija</h3>
      <input type="text" id='search-query' placeholder='henkilötunnus, nimi tai oppijanumero' onInput={(e) => oppijaHakuE.push(e.target.value)}></input>
      {
        // TODO: observable embedding for attributes didn't work here in PhantomJS
        Bacon.combineWith(uusiOppijaUrlP, canAddP, (url, canAdd) => (
          <a href={url || ''}
             className={canAdd ? 'lisaa-oppija' : 'lisaa-oppija disabled'}
             onClick={canAdd && ((e) => navigateTo(url, e))}>
            Lisää opiskelija
          </a>
        ))
      }
    </div>
    <div className='hakutulokset'>
      {
        oppijatP.map(oppijat => oppijat.results.length > 0
          ? (<ul> {
              oppijat.results.map((o, i) =>
                <li key={i}><a href={`/koski/oppija/${o.oid}`} onClick={(e) => navigateToOppija(o, e)}>{o.sukunimi}, {o.etunimet} ({o.hetu})</a> </li>
              )
            } </ul>)
          : oppijat.query.length > 2
            ? <div className='no-results'>Ei hakutuloksia</div>
            : null
        )
      }
    </div>
  </div>
)
