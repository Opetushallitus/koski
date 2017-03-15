import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Autocomplete from './Autocomplete.jsx'
import Http from './http'
import {showInternalError} from './location.js'
import {formatISODate} from './date.js'

const Oppilaitos = ({oppilaitosAtom, oppilaitos}) => (<label className='oppilaitos'>Oppilaitos
  <Autocomplete
    resultAtom={oppilaitosAtom}
    fetchItems={value => (value.length >= 1)
                ?  Http.cachedGet('/koski/api/oppilaitos').map(oppilaitokset => oppilaitokset.filter(o => o.nimi.fi.toLowerCase().indexOf(value.toLowerCase()) >= 0))
                : Bacon.once([])
              }
    selected={ oppilaitos }
  />
</label>)

const Tutkinto = ({tutkintoAtom, oppilaitos, tutkinto}) =>{
  return (<label className='tutkinto'>Tutkinto
    <Autocomplete
      resultAtom={tutkintoAtom}
      fetchItems={(value) => (value.length >= 3)
                          ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value).doError(showInternalError)
                          : Bacon.constant([])}
      disabled={!oppilaitos}
      selected={tutkinto}
    />
  </label> )
}

const OpiskeluoikeudenTyyppi = ({opiskeluoikeudenTyyppiAtom, tyypit}) => {
  let onChange = (event) => {
    let tyyppi = tyypit.find(t => t.koodiarvo == event.target.value)
    opiskeluoikeudenTyyppiAtom.set(tyyppi)
  }
  if (tyypit.length == 0) {
    return null
  }
  return (<label className='opiskeluoikeudentyyppi'>Opiskeluoikeus
    <select defaultValue={opiskeluoikeudenTyyppiAtom.map('.koodiarvo')} onChange={ onChange }>
      {
        tyypit.map((tyyppi, i) => <option key={i} value={tyyppi.koodiarvo}>{tyyppi.nimi.fi}</option>)
      }
    </select>
  </label> )
}

export const Opiskeluoikeus = ({opiskeluoikeusAtom}) => {
  const dateAtom = Atom(new Date())
  const oppilaitosAtom = Atom()
  const tutkintoAtom = Atom()
  const opiskeluoikeudenTyyppiAtom = Atom()
  const suorituksetAtom = Atom()
  const tilaAtom = Atom('lasna')

  const opiskeluoikeustyypitP = oppilaitosAtom
    .flatMapLatest((oppilaitos) => (oppilaitos ? Http.cachedGet(`/koski/api/oppilaitos/opiskeluoikeustyypit/${oppilaitos.oid}`) : []))
    .toProperty()

  opiskeluoikeustyypitP.onValue((tyypit) => {
    opiskeluoikeudenTyyppiAtom.set(tyypit.length > 0 ? tyypit[0] : undefined)
  })

  const suorituksetP = Bacon.combineWith(opiskeluoikeudenTyyppiAtom, tutkintoAtom, oppilaitosAtom, (tyyppi, tutkinto, oppilaitos) => {
    if (tutkinto && oppilaitos && tyyppi && tyyppi.koodiarvo == 'ammatillinenkoulutus') {
      return [{
          koulutusmoduuli: {
            tunniste: {
              koodiarvo: tutkinto.tutkintoKoodi,
              koodistoUri: 'koulutus'
            },
            perusteenDiaarinumero: tutkinto.diaarinumero
          },
          toimipiste : oppilaitos,
          tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
          tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'ammatillinentutkinto'}
        }]
    } else if (oppilaitos && tyyppi && tyyppi.koodiarvo == 'perusopetus') {
      return [{
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '201101',
            koodistoUri: 'koulutus'
          }
        },
        toimipiste: oppilaitos,
        tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
        oppimäärä: { koodistoUri: 'perusopetuksenoppimaara', koodiarvo: 'perusopetus'},
        suoritustapa: { koodistoUri: 'perusopetuksensuoritustapa', koodiarvo: 'koulutus'},
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'perusopetuksenoppimaara'}
      }]
    }
  })
  suorituksetP.changes().onValue((suoritukset) => suorituksetAtom.set(suoritukset))
  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))

  const opiskeluoikeusP = Bacon.combineWith(dateAtom, oppilaitosAtom, opiskeluoikeudenTyyppiAtom, suorituksetAtom, tilaAtom, (date, oppilaitos, tyyppi, suoritukset, tila) => {
    console.log(date, oppilaitos, tyyppi, suoritukset, tila)
    return date && oppilaitos && tyyppi && suoritukset && tila && {
      tyyppi: tyyppi,
      oppilaitos: oppilaitos,
      alkamispäivä: formatISODate(date),
      tila: {
        opiskeluoikeusjaksot: [ { alku: formatISODate(date), tila: { 'koodistoUri': 'koskiopiskeluoikeudentila', 'koodiarvo': tila } }]
      },
      suoritukset
    }
  }).log("OO")
  opiskeluoikeusP.changes().onValue((oo) => opiskeluoikeusAtom.set(oo))

  return (<div>
    {
      Bacon.combineWith(oppilaitosAtom, tutkintoAtom, opiskeluoikeustyypitP, opiskeluoikeudenTyyppiAtom, (oppilaitos, tutkinto, tyypit, tyyppi) => <div>
        <Oppilaitos oppilaitosAtom={oppilaitosAtom} oppilaitos={oppilaitos} />
        {
          <OpiskeluoikeudenTyyppi opiskeluoikeudenTyyppiAtom={opiskeluoikeudenTyyppiAtom} tyypit={tyypit}/>
        }
        {
          tyyppi && tyyppi.koodiarvo == 'ammatillinenkoulutus' && <Tutkinto tutkintoAtom={tutkintoAtom} tutkinto={tutkinto} oppilaitos={oppilaitos}/>
        }
      </div>)
    }
  </div>)
}