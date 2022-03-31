import React from 'baret'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import {DateView, KeyValueTable} from './KeyValueTable'
import {findLaajuudenYksikkoTakeFirst, KelaOsasuorituksetTable, laskeLaajuusOsasuorituksista} from './KelaOsasuorituksetTable'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'

export const TabulatedSuorituksetView = ({suoritukset, path}) => {
  const indexAtom = Atom(0)
  return (
    <div className='kela opiskeluoikeus suoritukset'>
      {indexAtom.map(selectedIndex => (
        <>
          <SuoritusTabs suoritukset={suoritukset}
                        selectedIndex={selectedIndex}
                        setCurrentIndex={(index) => indexAtom.set(index)}
          />
          <SuoritusView suoritus={suoritukset[selectedIndex]} path={path}/>
        </>
      ))}
    </div>
  )
}

TabulatedSuorituksetView.displayName = 'TabulatedSuorituksetView'

const SuoritusTabs = ({suoritukset, selectedIndex, setCurrentIndex}) => {
  return (
    <div className='tabs'>
      <ul>
        {suoritukset.map((suoritus, index) => (
            <li onClick={() => setCurrentIndex(index)}
                className={'tab' + (index === selectedIndex ? ' selected' : '')}
                key={index}
            >
              <span>{tabName(suoritus)}</span>
            </li>
          )
        )}
      </ul>
    </div>
  )
}

SuoritusTabs.displayName = 'SuoritusTabs'

const SuoritusView = ({suoritus, path}) => {
  const properties = R.omit(['osasuoritukset', 'vahvistus', 'koulutusmoduuli'], suoritus)
  const osasuoritukset = suoritus.osasuoritukset
  const piilotaArviointiSarakkeet = ['diatutkintovaihe', 'diavalmistavavaihe'].includes(suoritus.tyyppi.koodiarvo)
  const piilotaVahvistus = suoritus.tyyppi.koodiarvo === 'lukionaineopinnot'
  return (
    <>
      <KeyValueTable object={properties} path={path}/>
      {!piilotaVahvistus && <SuorituksenVahvistus vahvistus={suoritus.vahvistus}/>}
      {osasuoritukset && <>
        <OsasuoritustenYhteislaajuus osasuoritukset={osasuoritukset} />
        <KelaOsasuorituksetTable osasuoritukset={osasuoritukset}
                                                  piilotaArviointiSarakkeet={piilotaArviointiSarakkeet}
                                                  path={path}/>
      </>}
    </>
  )
}

SuoritusView.displayName = 'SuoritusView'

const SuorituksenVahvistus = ({vahvistus}) => (
  <div className={'suoritus vahvistus' + (vahvistus ? ' valmis' : ' kesken')}>
    <span className='status'>{t(vahvistus ? 'Suoritus valmis' : 'Suoritus kesken').toUpperCase()}</span>
    {vahvistus && (
      <span>
        {' '}
        <Text name={'Vahvistus'}/>
        {': '}
        <DateView value={vahvistus.päivä}/>
      </span>
    )}
  </div>
)

SuorituksenVahvistus.displayName = 'SuorituksenVahvistus'

const OsasuoritustenYhteislaajuus = ({osasuoritukset}) => {
  const laajuudenYksikko = findLaajuudenYksikkoTakeFirst(osasuoritukset)
  const yhteislaajuus = R.sum(osasuoritukset.map(osasuoritus => {
    return osasuoritus.koulutusmoduuli.laajuus?.arvo || laskeLaajuusOsasuorituksista(osasuoritus)
  }))

  return (
    <div className='suoritus yhteislaajuus'>
      <span className='yhteislaajuus'>{t('Yhteislaajuus')}</span>
      <span>{`${yhteislaajuus} ${laajuudenYksikko ? laajuudenYksikko : ''}`}</span>
    </div>
  )
}

OsasuoritustenYhteislaajuus.displayName = 'OsasuoritustenYhteislaajuus'

const tabName = suoritus => {
  const tunnisteenNimi = suoritus.koulutusmoduuli.tunniste && t(suoritus.koulutusmoduuli.tunniste.nimi) || undefined
  const tyyppi = suoritus.tyyppi.nimi && t(suoritus.tyyppi.nimi) || undefined
  return tunnisteenNimi || tyyppi || ''
}
