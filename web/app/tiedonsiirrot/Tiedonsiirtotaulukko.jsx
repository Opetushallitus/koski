import React from 'react'
import * as R from 'ramda'
import { ISO2FinnishDateTime, ISO2FinnishDate } from '../date/date'
import PaginationLink from '../components/PaginationLink'
import Link from '../components/Link'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import { flatMapArray } from '../util/util'

export class Tiedonsiirtotaulukko extends React.Component {
  render() {
    const {
      rivit,
      showError,
      pager,
      selected,
      showSelected = false
    } = this.props

    return (
      <div className="tiedonsiirto-taulukko">
        <table>
          <thead>
            <tr>
              <th className="tila">
                <Text name="Tila" />
              </th>
              <th className="aika">
                <Text name="Aika" />
              </th>
              <th className="hetu">
                <Text name="Henkilötunnus / Syntymäaika" />
              </th>
              <th className="nimi">
                <Text name="Nimi" />
              </th>
              <th className="oppilaitos">
                <Text name="Oppilaitos" />
              </th>
              <th className="tutkinto">
                <Text name="Tutkinto / osaamisala / nimike" />
              </th>
              <th className="virhe">
                <Text name="Virhe" />
              </th>
              <th className="tiedot">
                <Text name="Tiedot" />
              </th>
              {selected && showSelected && <th className="valitse"></th>}
            </tr>
          </thead>
          {rivit.map((oppijaRivi, i) => (
            <Lokiriviryhmä
              oppijaRivi={oppijaRivi}
              i={i}
              showError={showError}
              key={i}
              selected={selected}
              showSelected={showSelected}
            />
          ))}
        </table>
        <PaginationLink pager={pager} />
      </div>
    )
  }
}

class Lokiriviryhmä extends React.Component {
  render() {
    const { oppijaRivi, i, showError, selected, showSelected } = this.props

    const setExpanded = (expanded) => this.setState({ expanded })
    const isExpanded = this.state && this.state.expanded
    const tiedonsiirtoRivit = oppijaRivi.rivit
    const isGroup = tiedonsiirtoRivit.length > 1
    return (
      <tbody className="alternating">
        {flatMapArray(tiedonsiirtoRivit, (rivi, j) => {
          const isParent = j == 0 && isGroup
          const isChild = j > 0 && isGroup
          const isHidden = isChild && !isExpanded
          return isHidden
            ? []
            : [
                <Lokirivi
                  key={rivi.id}
                  row={rivi}
                  isParent={isParent}
                  isChild={isChild}
                  isExpanded={isExpanded}
                  isEven={i % 2 == 1}
                  showError={showError}
                  setExpanded={setExpanded}
                  selected={selected}
                  showSelected={showSelected}
                />
              ]
        })}
      </tbody>
    )
  }
}

class Lokirivi extends React.Component {
  render() {
    const {
      row,
      isParent,
      isChild,
      isExpanded,
      isEven,
      showError,
      setExpanded,
      selected,
      showSelected
    } = this.props
    const dataToBeShown = this.state && this.state.dataToBeShown
    const showData = (data) => this.setState({ dataToBeShown: data })
    const errorDetails = (virheet) =>
      showError ? (
        <div>
          <ul className="tiedonsiirto-errors">
            {virheet.map((virhe, i) => (
              <li key={i}>
                {virhe.key === 'badRequest.validation.jsonSchema' ? (
                  <JsonSchemaErrorMessage virhe={virhe} />
                ) : (
                  virhe.message
                )}
              </li>
            ))}
          </ul>
          <a className="virheen-tiedot" onClick={() => showErrors(virheet)}>
            <Text name="virhe" />
          </a>
        </div>
      ) : (
        <a className="virheen-tiedot" onClick={() => showErrors(virheet)}>
          <Text name="virhe" />
        </a>
      )

    const dataLink = () => (
      <a className="viestin-tiedot" onClick={() => showData(row.inputData)}>
        <Text name="tiedot" />
      </a>
    )

    const showErrors = (virheet) => showData(virheet)
    const sukunimi = (row.oppija && row.oppija.sukunimi) || ''
    const kutsumanimi = (row.oppija && row.oppija.kutsumanimi) || ''
    const nimi = sukunimi + (sukunimi ? ', ' : '') + kutsumanimi
    const className =
      (isParent || isChild ? 'group ' : '') + (isEven ? 'even' : 'odd')

    const select = (value) =>
      selected.modify((prev) =>
        R.includes(value, prev)
          ? R.without([value], prev)
          : R.append(value, prev)
      )

    return (
      <tr className={className} id={'tiedonsiirto-' + row.id}>
        <td className="tila">
          {isParent ? (
            isExpanded ? (
              <a className="collapse" onClick={() => setExpanded(false)}>
                {'-'}
              </a>
            ) : (
              <a className="expand" onClick={() => setExpanded(true)}>
                {'+'}
              </a>
            )
          ) : null}
          {row.virhe.length ? (
            <span className="status fail">{'✕'}</span>
          ) : (
            <span className="status ok">{'✓'}</span>
          )}
        </td>
        <td className="aika">{ISO2FinnishDateTime(row.aika)}</td>
        <td className="hetu">
          {row.oppija &&
            (row.oppija.hetu ||
              (row.oppija.syntymäaika &&
                ISO2FinnishDate(row.oppija.syntymäaika)))}
        </td>
        <td className="nimi">
          {row.oppija && row.oppija.oid ? (
            <Link href={`/koski/oppija/${row.oppija.oid}`}>{nimi}</Link>
          ) : (
            nimi
          )}
        </td>
        <td className="oppilaitos">
          {row.oppilaitos &&
            row.oppilaitos.map((oppilaitos, i) => (
              <Link
                key={i}
                href={
                  '/koski/tiedonsiirrot' +
                  (showError ? '/virheet' : '') +
                  '?oppilaitos=' +
                  oppilaitos.oid
                }
              >
                {oppilaitos && oppilaitos.nimi && t(oppilaitos.nimi)}
              </Link>
            ))}
        </td>
        <td className="tutkinto">
          {row.suoritustiedot.map((suoritus, j) => (
            <ul className="cell-listing" key={j}>
              <li className="koulutusmoduuli">
                {t(suoritus.koulutusmoduuli.nimi)}
              </li>
              {(suoritus.osaamisalat || []).map((osaamisala, k) => (
                <li className="osaamisala" key={k}>
                  {t(osaamisala.nimi)}
                </li>
              ))}
              {(suoritus.tutkintonimike || []).map((nimike, k) => (
                <li className="tutkintonimike" key={k}>
                  {t(nimike.nimi)}
                </li>
              ))}
            </ul>
          ))}
        </td>
        <td className="virhe">
          {row.virhe.length > 0 && <span>{errorDetails(row.virhe)}</span>}
        </td>
        <td className="tiedot">
          {!!row.inputData && dataLink()}
          {dataToBeShown && (
            <LokirivinData details={dataToBeShown} showData={showData} />
          )}
        </td>
        {selected && showSelected && (
          <td className="valitse">
            <input type="checkbox" onClick={() => select(row.id)} />
          </td>
        )}
      </tr>
    )
  }
}

class LokirivinData extends React.Component {
  render() {
    const { details, showData } = this.props
    return (
      <div className="lokirividata-popup">
        <a className="close" onClick={() => showData(false)}>
          <Text name="Sulje" />
        </a>
        <pre>{JSON.stringify(details, null, 2)}</pre>
      </div>
    )
  }
}

class JsonSchemaErrorMessage extends React.Component {
  render() {
    const { virhe } = this.props
    let detail
    const errorType = R.path(['message', 0, 'error', 'errorType'], virhe)
    const path = R.path(['message', 0, 'path'], virhe)
    const pathLast =
      typeof path === 'string' ? R.last(path.split('.')) : undefined
    if (errorType === 'tuntematonKoodi') {
      detail = R.pathOr(errorType, ['message', 0, 'error', 'message'], virhe)
    } else if (errorType && pathLast) {
      detail = errorType + ' ' + pathLast
    } else {
      detail = errorType
    }
    return (
      <span>
        <Text name="Viesti ei ole skeeman mukainen" />
        {detail && ' (' + detail + ')'}
      </span>
    )
  }
}
