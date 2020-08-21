import React from 'baret'
import * as R from 'ramda'
import {TabulatedSuorituksetView} from './KelaSuoritus'
import {yearFromIsoDateString} from '../date/date'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {DateView, KeyValueTable} from './KeyValueTable'


export const KelaHenkilo = ({henkilo}) => {
  return (
    <div className='kela henkilo'>
      <h2 className='henkilotiedot'>
        {`${henkilo.sukunimi}, ${henkilo.etunimet} (${henkilo.hetu || 'ei hetua'})`}
      </h2>
      <span className='oppijanumero'>
        <Text name={'Oppijanumero'}/>
        {': '}
        {henkilo.oid || ''}
      </span>
    </div>
  )
}

export const KelaOpiskeluoikeus = ({opiskeluoikeus}) => {
  const removeFromTableView = ['suoritukset', 'alkamispäivä', 'päättymispäivä', 'oid', 'versionumero', 'arvioituPäättymispäivä', 'oppilaitos', 'koulutustoimija', 'tyyppi', 'aikaleima']
  return (
    <div className='kela opiskeluoikeus'>
      <OpiskeluoikeusOtsikko opiskeluoikeus={opiskeluoikeus}/>
      <div className='kela opiskeluoikeus content'>
        <Voimassaoloaika opiskeluoikeus={opiskeluoikeus}/>
        <KeyValueTable object={R.omit(removeFromTableView, opiskeluoikeus)} path={'opiskeluoikeus'}/>
        <TabulatedSuorituksetView suoritukset={opiskeluoikeus.suoritukset} path={'opiskeluoikeus.suoritukset'}/>
      </div>
    </div>
  )
}

export const OpiskeluoikeusOtsikko = ({opiskeluoikeus}) => {
  const oppilaitoksenNimi = opiskeluoikeus.oppilaitos && t(opiskeluoikeus.oppilaitos.nimi || {})
  return (
    <h3 className='otsikko'>
      <span>
        {oppilaitoksenNimi + ' ' + opiskeluoikeudenTilaString(opiskeluoikeus)}
      </span>
      <span className='oid'>
        <Text name={'Opiskeluoikeuden oid'} />{': '}{opiskeluoikeus.oid}
      </span>
    </h3>
  )
}

export const opiskeluoikeudenTilaString = opiskeluoikeus => {
  const alkamispaiva =  opiskeluoikeus.alkamispäivä && yearFromIsoDateString(opiskeluoikeus.alkamispäivä) || ''
  const paattymispaiva =  opiskeluoikeus.päättymispäivä && yearFromIsoDateString(opiskeluoikeus.päättymispäivä) || ''
  const viimeisinTila = R.last(opiskeluoikeus.tila.opiskeluoikeusjaksot || [])
  const viimeisimmänTilanNimi = (viimeisinTila && t(viimeisinTila.tila.nimi)) || ''
  return `(${alkamispaiva} - ${paattymispaiva}, ${viimeisimmänTilanNimi})`
}

const Voimassaoloaika = ({opiskeluoikeus}) => {
  const {isArvioituPäättymispäivä, päättymispäivä}= paattymispaiva(opiskeluoikeus)

  return (
    <div className='opiskeluoikeuden-voimassaoloaika'>
      <Text name="Opiskeluoikeuden voimassaoloaika"/>{': '}
      <DateView value={opiskeluoikeus.alkamispäivä}/>
      {' - '}
      <DateView value={päättymispäivä}/>
      {isArvioituPäättymispäivä && <span>{' (' + t('arvioitu') + ')'}</span>}
    </div>
  )
}

const paattymispaiva = opiskeluoikeus => {
  if (opiskeluoikeus.päättymispäivä) {
    return {isArvioituPäättymispäivä: false,päättymispäivä: opiskeluoikeus.päättymispäivä}
  } else {
    return opiskeluoikeus.arvioituPäättymispäivä
      ? {isArvioituPäättymispäivä: true, päättymispäivä: opiskeluoikeus.arvioituPäättymispäivä}
      : {isArvioituPäättymispäivä: false, päättymispäivä: undefined}
  }
}
