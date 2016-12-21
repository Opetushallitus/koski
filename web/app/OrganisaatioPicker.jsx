import React from 'react'
import Bacon from 'baconjs'
import Http from './http'

export default React.createClass({
  render() {
    let { organisaatiot = [], open, loading } = this.state
    let { onSelectionChanged, selectedOrg } = this.props
    let selectOrg = (org) => { this.setState({open: false}); onSelectionChanged(org) }
    let renderTree = (orgs) => orgs.map((org, i) =>
      <li key={i}><a className="nimi" onClick={ (e) => { selectOrg(org); e.preventDefault() }}>{org.nimi.fi}</a>
        <ul className="aliorganisaatiot">
          { renderTree(org.children) }
        </ul>
      </li>
    )

    return (
      <div className="organisaatio" tabIndex="0">
        <div className="organisaatio-selection" onClick={ () => this.setState({open:!open}) }>{ selectedOrg.nimi ? selectedOrg.nimi : 'kaikki'}</div>
        { open &&
        <div className="organisaatio-popup">
          <input type="text" placeholder="hae" ref="hakuboksi" defaultValue={this.state.searchString} onChange={e => {
            if (e.target.value.length >= 3 || e.target.value.length == 0) this.searchStringBus.push(e.target.value)
          }}/>
          <button className="button kaikki" onClick={() => { this.searchStringBus.push(''); selectOrg(null)}}>kaikki</button>
          <div className="scroll-container">
            <ul className={loading ? 'organisaatiot loading' : 'organisaatiot'}>
              { renderTree(organisaatiot) }
            </ul>
          </div>
        </div>
        }
      </div>
    )
  },
  componentDidUpdate() {
    if (this.state.open) {
      if (this.state.searchString === undefined) {
        this.searchStringBus.push('') // trigger initial AJAX load when opened for the first time
      }
      this.refs.hakuboksi.focus()
    }
  },
  componentWillMount() {
    this.searchStringBus = Bacon.Bus()
    this.searchStringBus
      .onValue((searchString) => this.setState({searchString, loading: true}))
    this.searchStringBus.flatMapLatest((searchString) => Http.get('/koski/api/organisaatio/hierarkia?query=' + searchString)).map((organisaatiot) => ({ organisaatiot, loading: false }))
      .onValue((result) => this.setState(result))
  },
  getInitialState() {
    return { open: false }
  },
  componentDidMount() {
    window.addEventListener('click', this.handleClickOutside, false)
  },
  componentWillUnmount() {
    window.removeEventListener('click', this.handleClickOutside, false)
  },
  handleClickOutside(e) {
    !e.target.closest('.organisaatio') && this.setState({open: false})
  }
})