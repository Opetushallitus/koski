import React from 'baret'
import * as R from 'ramda'
import Text from '../i18n/Text'
import {formatFinnishDate, parseISODate} from '../date/date'
import {pathToClassnames} from './KelaOsasuorituksetTable'
import {t} from '../i18n/i18n'


export const KeyValueTable = ({object, path}) => {
  return (
    <table>
      <tbody className={pathToClassnames(path) + ' properties key-value-table'}>
      {iterator(object, path).map(rowInTable)}
      </tbody>
    </table>
  )
}

KeyValueTable.displayName = 'KeyValueTable'

const rowInTable = ({key, value, path}, index) => {
  const ReactComponentForDisplayingValue = spesificComponent(key, value) || componentForType[R.type(value)]

  return (
    <tr className={pathToClassnames(path)} key={index}>
      <td className='label'>
        {t(formatLabel(key))}
      </td>
      <td className='value'>
        <ReactComponentForDisplayingValue value={value} path={path}/>
      </td>
    </tr>
  )
}

export const NumberView = ({value}) => <span>{value == 0 ? '' : value}</span>
export const BooleanView = ({value}) => <Text name={value ? 'kyllä' : 'ei'}/>
export const DateView = ({value}) => <span>{ value && formatFinnishDate(parseISODate(value)) || ''}</span>

NumberView.displayName = 'NumberView'
BooleanView.displayName = 'BooleanView'
DateView.displayName = 'DateView'

export const TextView = ({value}) => {
  return R.type(value) === 'Object' //assume its LocalizedString
    ? <span>{t(value)}</span>
    : <Text name={value}/>
}

TextView.displayName = 'TextView'

const ArrayView = ({value}) => {
  return (
    <ul>
      {value.map((val, i) => {
        const ReactComponent = spesificComponent('key', val) || componentForType[R.type(val)]
        return (
          <li>
            <ReactComponent key={i} value={val}/>
          </li>
        )
      })}
    </ul>
  )
}

ArrayView.displayName = 'ArrayView'

const ObjectView = ({value, path}) => {
  const hideKeys = ['alku', 'loppu', 'koodistoVersio', 'koodistoUri', 'lyhytNimi']
  const optionalAikajaksoInline = isAikajakso(value) && <AikajaksoInline value={value}/>
  return (
    <table>
      <tbody>
        {optionalAikajaksoInline}
        {iterator(R.omit(hideKeys, value), path).map(rowInTable)}
      </tbody>
    </table>
  )
}

ObjectView.displayName = 'ObjectView'

const iterator = (obj, currentPath) => {
  return Object.entries(obj).map(([key, value]) => ({key, value, path: currentPath + '.' + key}))
}

const spesificComponent = (key, value) => {
  if (R.includes(key, ['oppilaitos', 'toimipiste', 'koulutustoimija'])) {
    return OrganisaationNimi
  }
  if (key === 'tunniste' || isKoodistoviite(value)) {
    return Koodistoviite
  }
  if (isLocalizedString(value)) {
    return TextView
  }
  if (key === 'laajuus') {
    return Laajuus
  }
  if (key === 'osaamisala') {
    return OsaamisalaJaksotInline
  }
  if (key === 'tila') {
    return Tilat
  }
  return undefined
}

const componentForType = {
  'Object': ObjectView,
  'Array': ArrayView,
  'Boolean': BooleanView,
  'String': TextView,
  'Number': NumberView
}

const OrganisaationNimi = ({value}) => {
  const nimi = value && t(value.nimi || {}) || ''
  return <span>{nimi}</span>
}

OrganisaationNimi.displayName = 'OrganisaationNimi'

const Koodistoviite = ({value}) => {
  const nimi = t(value.nimi || {}) || value.koodiarvo || ''
  return (
    <span>
      {nimi}
    </span>
  )
}

Koodistoviite.displayName = 'Koodistoviite'

const Laajuus = ({value}) => (
  <>
    <span>{value.arvo}</span>
    {' '}
    <Koodistoviite value={value.yksikkö}/>
  </>
)

Laajuus.displayName = 'Laajuus'

const Tilat = ({ value }) => {
  const jaksot = [].concat(value.opiskeluoikeusjaksot).reverse()
  const today = new Date()
  const activeIndex = jaksot.findIndex(j => parseISODate(j.alku) <= today)
  return (
    <ul className="array">
      {
        jaksot.map((item, i) => {
          return (
            <li key={i}>
              <div
                className={'opiskeluoikeusjakso' + (i === activeIndex ? ' active' : '')}>
                <label className="date">
                  <DateView value={item.alku}/>
                </label>
                <label className="tila">
                  <TextView value={item.tila.nimi}/>
                </label>
                <label className="rahoitusmuoto">
                  {item.opintojenRahoitus && <span>{'('}<Koodistoviite value={item.opintojenRahoitus}/>{')'}</span>}
                </label>
              </div>
            </li>
          )
        })
      }
    </ul>
  )
}

Tilat.displayName = 'Tilat'

const OsaamisalaJaksotInline = ({value}) => {
  const osaamisalaJaksot = value
  return (
    <ul>
      {osaamisalaJaksot.map((jakso, index) => (
        <li key={index}>
          <AikajaksoInline value={value}/>
          <Koodistoviite value={jakso.osaamisala}/>
        </li>
      ))}
    </ul>
  )
}

OsaamisalaJaksotInline.displayName = 'OsaamisalaJaksotInline'

const AikajaksoInline = ({value}) => {
  return (
    <tr>
      <td>
      <DateView value={value.alku}/>
      {value.alku && ' - '}
      <DateView value={value.loppu}/>
      </td>
    </tr>
  )
}

AikajaksoInline.displayName = 'AikajaksoInline'

const isAikajakso = obj => obj.alku || obj.loppu

const isKoodistoviite = obj => Object.keys(obj).includes('koodiarvo')

const isLocalizedString = obj => !R.isEmpty(R.intersection(['fi', 'en', 'sv'], Object.keys(obj)))

const formatLabel = label => capitalizeFirstChar(splitToWords(label))
const splitToWords = str => str.split(/([A-ZÄÖÅ][a-zäöå]+)/g).map(R.toLower).join(' ').trim()
const capitalizeFirstChar = str => str.charAt(0).toUpperCase() + str.slice(1)
