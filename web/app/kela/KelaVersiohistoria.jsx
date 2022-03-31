import React from 'baret'
import Atom from 'bacon.atom'
import Http from '../util/http'
import {ISO2FinnishDateTime} from '../date/date'
import Text from '../i18n/Text'
import Link from '../components/Link'

export const KelaVersiohistoria = ({opiskeluoikeus, oppijaOid}) => {
  const opiskeluoikeudenOid = opiskeluoikeus.oid
  const versionumero = opiskeluoikeus.versionumero
  if (!(opiskeluoikeudenOid || versionumero)) return null

  const historyObs = Http.cachedGet(`/koski/api/luovutuspalvelu/kela/versiohistoria/${opiskeluoikeudenOid}`)
  const showHistoryAtom = Atom(false)

  return (
    <div className='versiohistoria'>
      {historyObs.combine(showHistoryAtom, (history, showHistory) => {
        return (<>
          <span onClick={() => showHistoryAtom.set(!showHistory)}><Text name='Versiohistoria'/></span>
          {showHistory && (
            <div className='kela-modal'>
              <div className='kela-modal-content'>
              <button onClick={() => showHistoryAtom.set(false)} className='kela-content-close'/>
              <ol>{history.map((version, i) => (
                  <li key={i} className={versionumero === version.versionumero ? 'selected' : ''}>
                    <Link href={`/koski/kela/versiohistoria/${oppijaOid}/${opiskeluoikeudenOid}/${version.versionumero}`}>
                      <span className="versionumero">{version.versionumero + ' '}</span>
                      <span className="aikaleima">{ISO2FinnishDateTime(version.aikaleima)}</span>
                    </Link>
                  </li>)
              )}
              </ol>
              </div>
            </div>
          )}
        </>)}
      )}
    </div>
  )
}

KelaVersiohistoria.displayName = 'KelaVersiohistoria'

export const PalaaVersiohistoriastaLink = ({henkilo}) => {
  const hetu = henkilo.hetu
  const showReturnLink = window.location.href.includes('kela/versiohistoria/')

  return (
    <div className='palaa-versiohistoriasta'>
      {showReturnLink && (
        <Link href={`/koski/kela/${hetu}`}>
          <Text name='Palaa versiohistoriasta yleisnäkymään'/>
        </Link>
      )}
    </div>
  )
}

PalaaVersiohistoriastaLink.displayName = 'PalaaVersiohistoriastaLink'
