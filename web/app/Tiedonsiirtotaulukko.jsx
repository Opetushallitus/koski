import React from 'react'
import fecha from 'fecha'

export const Tiedonsiirtotaulukko = React.createClass({
  render() {
    const { rivit, showError } = this.props
    const showDataForRow = this.state && this.state.showDataForRow
    return (<div>
      <table>
        <thead>
        <tr>
          <th className="tila">Tila</th>
          <th className="aika">Aika</th>
          <th className="hetu">Henkilötunnus</th>
          <th className="nimi">Nimi</th>
          <th className="oppilaitos">Oppilaitos</th>
          <th className="virhe">{showError ? 'Virhe' : 'Tiedot'}</th>
          <th className="tiedot"></th>
        </tr>
        </thead>
        <tbody>
        {
          rivit.flatMap((oppijaRivi, i) => {
            const tiedonsiirtoRivit = oppijaRivi.rivit
            const isGroup = tiedonsiirtoRivit.length > 1
            const isExpanded = this.state && this.state.expandedRow == tiedonsiirtoRivit[0]
            return tiedonsiirtoRivit.flatMap((rivi, j) => {
                const isParent = j == 0 && isGroup
                const isChild = j > 0 && isGroup
                const isHidden = isChild && !isExpanded
                return isHidden
                  ? []
                  : [<Lokirivi key={i + '-' + j} row={rivi} isParent={isParent} isChild={isChild} isExpanded={isExpanded} isEven={i % 2 == 1} parentComponent={this} showError={showError}/>]
              }
            )
          })
        }
        </tbody>
      </table>
      {
        showDataForRow && <LokirivinData details={showDataForRow} parent={this}/>
      }
    </div>)
  }
})

const Lokirivi = React.createClass({
  render() {
    const extractName = (oppilaitokset) =>
      oppilaitokset && oppilaitokset.map((oppilaitos) => oppilaitos && oppilaitos.nimi && oppilaitos.nimi.fi).join(', ')

    const {row, isParent, isChild, isExpanded, isEven, parentComponent, showError} = this.props

    const errorDetails = (virheet) => { return showError ?
      <div>
        <ul className="tiedonsiirto-errors">{
          virheet.map((virhe, i) => <li key={i}>{(virhe.key === 'badRequest.validation.jsonSchema') ? 'Viesti ei ole skeeman mukainen' : virhe.message}</li>)
        }</ul>
        <a className="virheen-tiedot" onClick={() => showErrors(virheet)}>virhe</a>
      </div> : <a className="virheen-tiedot" onClick={() => showErrors(virheet)}>virhe</a>}

    const dataLink = () => <a className="viestin-tiedot" onClick={showData}>tiedot</a>

    const showErrors = (virheet) => parentComponent.setState({showDataForRow: virheet})
    const showData = () => parentComponent.setState({showDataForRow: row.inputData})
    const nimi = row.oppija && ((row.oppija.kutsumanimi || '') + ' ' + (row.oppija.sukunimi || ''))
    const className = ((isParent || isChild) ? 'group ' : '') + (isEven ? 'even' : 'odd')

    return (<tr className={className}>
      <td className="tila">
        {
          isParent
            ? (isExpanded
              ? <a className="collapse" onClick={() => parentComponent.setState({expandedRow: null})}>-</a>
              : <a className="expand" onClick={() => parentComponent.setState({expandedRow: row})}>+</a>)
            : null
        }
        {
          row.virhe
            ? <span className="status fail">✕</span>
            : <span className="status ok">✓</span>
        }
      </td>
      <td className="aika">{fecha.format(fecha.parse(row.aika, 'YYYY-MM-DDThh:mm'), 'D.M.YYYY h:mm')}</td>
      <td className="hetu">{row.oppija && row.oppija.hetu}</td>
      <td className="nimi">{
        (row.oppija && row.oppija.oid)
          ? <a href={`/koski/oppija/${row.oppija.oid}`}>{nimi}</a> : nimi
      }</td>
      <td className="oppilaitos">{extractName(row.oppilaitos)}</td>
      <td className="virhe">{row.virhe && <span>{errorDetails(row.virhe)}</span>}</td>
      <td className="tiedot">{row.virhe && dataLink()}</td>
    </tr>)
  }
})

const LokirivinData = React.createClass({
  render() {
    const { details, parent } = this.props
    console.log('showing details', details)
    return (<div className="lokirividata-popup">
      <a className="close" onClick={() => parent.setState({showDataForRow: null})}>Sulje</a>
      <pre>{JSON.stringify(details, null, 2)}</pre>
    </div>)
  }
})