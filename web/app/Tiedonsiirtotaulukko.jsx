import React from 'react'
import { ISO2FinnishDateTime } from './date'
import PaginationLink from './PaginationLink.jsx'
import Link from './Link.jsx'
import {t} from './i18n'
import Text from './Text.jsx'

export const Tiedonsiirtotaulukko = React.createClass({
  render() {
    const { rivit, showError, pager } = this.props

    return (<div className="tiedonsiirto-taulukko">
      <table>
        <thead>
        <tr>
          <th className="tila"><Text name="Tila"/></th>
          <th className="aika"><Text name="Aika"/></th>
          <th className="hetu"><Text name="Henkilötunnus"/></th>
          <th className="nimi"><Text name="Nimi"/></th>
          <th className="oppilaitos"><Text name="Oppilaitos"/></th>
          <th className="virhe"><Text name="Virhe"/></th>
          <th className="tiedot"><Text name="Tiedot"/></th>
        </tr>
        </thead>
        {
          rivit.map((oppijaRivi, i) => <Lokiriviryhmä oppijaRivi={oppijaRivi} i={i} showError={showError} key={i}/>)
        }
      </table>
      <PaginationLink pager={pager}/>
    </div>)
  }
})

const Lokiriviryhmä = React.createClass({
  render() {
    let { oppijaRivi, i, showError } = this.props

    const setExpanded = (expanded) => this.setState({expanded})
    const isExpanded = this.state && this.state.expanded
    const tiedonsiirtoRivit = oppijaRivi.rivit
    const isGroup = tiedonsiirtoRivit.length > 1
    return(<tbody className="alternating">
      {
        tiedonsiirtoRivit.flatMap((rivi, j) => {
            const isParent = j == 0 && isGroup
            const isChild = j > 0 && isGroup
            const isHidden = isChild && !isExpanded
            return isHidden
              ? []
              : [<Lokirivi key={rivi.id} row={rivi} isParent={isParent} isChild={isChild} isExpanded={isExpanded} isEven={i % 2 == 1} showError={showError} setExpanded={setExpanded}/>]
          }
        )
      }
    </tbody>)
  }
})

const Lokirivi = React.createClass({
  render() {
    const {row, isParent, isChild, isExpanded, isEven, showError, setExpanded} = this.props
    const dataToBeShown = this.state && this.state.dataToBeShown
    const showData = (data) => this.setState({dataToBeShown: data})
    const errorDetails = (virheet) => showError
      ? (<div>
          <ul className="tiedonsiirto-errors">{
            virheet.map((virhe, i) => <li key={i}>{(virhe.key === 'badRequest.validation.jsonSchema') ? <Text name='Viesti ei ole skeeman mukainen'/> : virhe.message}</li>)
          }</ul>
          <a className="virheen-tiedot" onClick={() => showErrors(virheet)}><Text name="virhe"/></a>
        </div>)
      : <a className="virheen-tiedot" onClick={() => showErrors(virheet)}><Text name="virhe"/></a>

    const dataLink = () => <a className="viestin-tiedot" onClick={() => showData(row.inputData)}><Text name="tiedot"/></a>

    const showErrors = (virheet) => showData(virheet)
    const nimi = row.oppija && ((row.oppija.kutsumanimi || '') + ' ' + (row.oppija.sukunimi || ''))
    const className = ((isParent || isChild) ? 'group ' : '') + (isEven ? 'even' : 'odd')

    return (<tr className={className} id={'tiedonsiirto-' + row.id}>
      <td className="tila">
        {
          isParent
            ? (isExpanded
              ? <a className="collapse" onClick={() => setExpanded(false)}>{'-'}</a>
              : <a className="expand" onClick={() => setExpanded(true)}>{'+'}</a>)
            : null
        }
        {
          row.virhe
            ? <span className="status fail">{'✕'}</span>
            : <span className="status ok">{'✓'}</span>
        }
      </td>
      <td className="aika">{ISO2FinnishDateTime(row.aika)}</td>
      <td className="hetu">{row.oppija && row.oppija.hetu}</td>
      <td className="nimi">{
        (row.oppija && row.oppija.oid)
          ? <Link href={`/koski/oppija/${row.oppija.oid}`}>{nimi}</Link> : nimi
      }</td>
      <td className="oppilaitos">{
        row.oppilaitos && row.oppilaitos.map((oppilaitos, i) =>
          <Link key={i} href={'/koski/tiedonsiirrot' + (showError ? '/virheet' : '') + '?oppilaitos=' + oppilaitos.oid}>{oppilaitos && oppilaitos.nimi && t(oppilaitos.nimi)}</Link>
        )
      }</td>
      <td className="virhe">{row.virhe && <span>{errorDetails(row.virhe)}</span>}</td>
      <td className="tiedot">
        {row.virhe && dataLink()}
        {
          dataToBeShown && <LokirivinData details={dataToBeShown} showData={showData}/>
        }
      </td>
    </tr>)
  }
})

const LokirivinData = React.createClass({
  render() {
    const { details, showData } = this.props
    return (<div className="lokirividata-popup">
      <a className="close" onClick={() => showData(false)}><Text name="Sulje"/></a>
      <pre>{JSON.stringify(details, null, 2)}</pre>
    </div>)
  }
})