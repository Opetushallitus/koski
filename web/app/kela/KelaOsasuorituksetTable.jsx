import React from 'baret'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import {
  BooleanView,
  DateView,
  KeyValueTable,
  NumberView
} from './KeyValueTable'
import { t } from '../i18n/i18n'

export const KelaOsasuorituksetTable = ({
  osasuoritukset,
  path,
  nested,
  piilotaArviointiSarakkeet
}) => {
  const currentPath = path + '.osasuoritukset'
  const laajuudenYksikko = findLaajuudenYksikkoTakeFirst(osasuoritukset)
  return (
    <table
      className={pathToClassnames(currentPath) + (nested ? ' nested' : '')}
    >
      <thead></thead>
      <tbody>
        <tr>
          <th className="osasuoritukset-title">{'Osasuoritukset'}</th>
          <th className="arviointi">
            {t(
              'Laajuus' + ((laajuudenYksikko && ` (${laajuudenYksikko})`) || '')
            )}
          </th>
          {!piilotaArviointiSarakkeet && (
            <>
              <th className="arviointi">{t('Arviointipäivä')}</th>
              <th className="arviointi">{t('Hyväksytty')}</th>
            </>
          )}
        </tr>
      </tbody>
      {osasuoritukset.map((osasuoritus, index) => (
        <ExpandableOsasuoritus
          key={index}
          osasuoritus={osasuoritus}
          path={currentPath}
          piilotaArviointiSarakkeet={piilotaArviointiSarakkeet}
        />
      ))}
    </table>
  )
}

const ExpandableOsasuoritus = ({
  osasuoritus,
  path,
  piilotaArviointiSarakkeet
}) => {
  const expandedAtom = Atom(R.length(osasuoritus.osasuoritukset || []) > 0)
  const laajuus =
    osasuoritus.koulutusmoduuli.laajuus?.arvo ||
    laskeLaajuusOsasuorituksista(osasuoritus)
  const arviointi = R.last(osasuoritus.arviointi || []) || {}
  const properties = R.omit(
    ['osasuoritukset', 'arviointi', 'koulutusmoduuli'],
    osasuoritus
  )
  const isExpandable = !R.isEmpty(properties) || osasuoritus.osasuoritukset

  return (
    <tbody>
      {expandedAtom.map((expanded) => (
        <>
          <tr
            className={
              pathToClassnames(path) +
              ' title' +
              (isExpandable ? ' pointer' : '')
            }
            onClick={() => expandedAtom.set(!expandedAtom.get())}
          >
            <td>
              <span className="expand-button">
                {isExpandable ? (expanded ? '' : '') : ''}
              </span>{' '}
              <span className="suorituksen-nimi">
                {suorituksenNimi(osasuoritus.koulutusmoduuli)}
              </span>
            </td>
            <td className="laajuus">
              <NumberView value={laajuus} />
            </td>
            {!piilotaArviointiSarakkeet && (
              <>
                <td className="arviointi">
                  <DateView value={arviointi.päivä} />
                </td>
                <td className="arviointi">
                  {R.isNil(arviointi.hyväksytty) ? (
                    <span>{''}</span>
                  ) : (
                    <BooleanView value={arviointi.hyväksytty} />
                  )}
                </td>
              </>
            )}
          </tr>
          {expanded && isExpandable && (
            <tr>
              <td className="expanded">
                <KeyValueTable object={properties} path={path} />
                {osasuoritus.osasuoritukset && (
                  <KelaOsasuorituksetTable
                    osasuoritukset={osasuoritus.osasuoritukset}
                    path={path}
                    nested={true}
                  />
                )}
              </td>
            </tr>
          )}
        </>
      ))}
    </tbody>
  )
}

const suorituksenNimi = (koulutusmoduuli) => {
  const koodiarvo = t(koulutusmoduuli.tunniste.nimi)
  const kieli = t(koulutusmoduuli.kieli?.nimi || {})
  const oppimaara = t(koulutusmoduuli.oppimäärä?.nimi || {})

  return [koodiarvo, kieli, oppimaara].filter(R.identity).join(', ')
}

export const findLaajuudenYksikkoTakeFirst = (osasuoritukset) => {
  return R.head(
    R.uniq(osasuoritukset.map(findLaajuudenYksikko).filter(R.identity))
  )
}

const findLaajuudenYksikko = (osasuoritus) => {
  const laajuus = osasuoritus.koulutusmoduuli.laajuus || {}
  const laajuudenYksikkoOsasuorituksista = R.head(
    (osasuoritus.osasuoritukset || [])
      .map(findLaajuudenYksikko)
      .filter(R.identity)
  )

  if (laajuus.yksikkö) {
    return t(laajuus.yksikkö.nimi)
  } else {
    return laajuudenYksikkoOsasuorituksista
  }
}

export const laskeLaajuusOsasuorituksista = (osasuoritus) => {
  const osasuoritukset = osasuoritus.osasuoritukset || []
  const hyvaksytytOsasuoritukset = osasuoritukset.filter(isHyvaksytty)
  return R.sum(hyvaksytytOsasuoritukset.map(osasuorituksenLaajuus))
}

const isHyvaksytty = (osasuoritus) => {
  const mahdollinenArviointi = R.last(osasuoritus.arviointi || []) || {}
  return mahdollinenArviointi.hyväksytty
}

const osasuorituksenLaajuus = (osasuoritus) => {
  const mahdollinenLaajuus = osasuoritus.koulutusmoduuli.laajuus || {}
  return mahdollinenLaajuus.arvo || 0
}

export const pathToClassnames = (path) => path.split('.').join(' ')
