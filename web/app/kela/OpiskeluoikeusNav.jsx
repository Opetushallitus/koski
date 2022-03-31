import React from 'baret'
import Atom from 'bacon.atom'
import {t} from '../i18n/i18n'
import {KelaOpiskeluoikeus, opiskeluoikeudenTilaString} from './KelaOppija'


export const OpiskeluoikeusTabs = ({opiskeluoikeudet, henkilo}) => {
  const selectedIndexAtom = Atom(0)

  return (
    <div className='opiskeluoikeus-tabs'>
      {selectedIndexAtom.map(selectedIndex => (
        <>
        <ul>
          {
            opiskeluoikeudet.map((opiskeluoikeus, index) => {
              const oppilaitoksenNimi = opiskeluoikeus.oppilaitos && t(opiskeluoikeus.oppilaitos.nimi || {})

              return (
                <li onClick={() => selectedIndexAtom.set(index)} className={index === selectedIndex ? 'selected' : ''} key={index}>
                  <span className='opiskeluoikeuden-tyyppi'>{t(opiskeluoikeus.tyyppi.nimi || {})}</span>
                  <span>{oppilaitoksenNimi} {opiskeluoikeudenTilaString(opiskeluoikeus)}</span>
                  <ul>
                    {opiskeluoikeus.suoritukset.map((suoritus, idx) => {
                      const suorituksenTyyppi = t(suoritus.tyyppi.nimi || {})
                      return suorituksenTyyppi
                        ? <li key={idx}>{suorituksenTyyppi}</li>
                        : null
                    })}
                  </ul>
                </li>
              )
            })
          }
        </ul>
        <KelaOpiskeluoikeus opiskeluoikeus={opiskeluoikeudet[selectedIndex]} henkilo={henkilo}/>
        </>
      ))}
    </div>
  )
}

OpiskeluoikeusTabs.displayName = 'OpiskeluoikeusTabs'
