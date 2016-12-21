import React from 'react'

export default React.createClass({
  render() {
    const { options, open, selected } = this.state
    return (
      <div id={this.props.id} className="dropdown" tabIndex="0" ref={el => this.dropdown = el}>
        <div className={selected ? 'select' : 'select no-selection'} onClick={this.openDropdown} >{selected ? selected.value : 'valitse'}</div>
        { open ?
          <ul className="options">
            {
              [{ value: 'ei valintaa' }].concat(options).map(o => <li key={o.key || o.value} className="option" onClick={e => this.selectOption(e,o)}>{o.value}</li>)
            }
          </ul>
          : null
        }
      </div>
    )
  },
  selectOption(e, option) {
    const selected = option.key ? option : undefined
    this.setState({selected: selected, open: false}, () => this.props.onSelectionChanged(selected))
    this.dropdown.blur()
  },
  openDropdown(e) {
    this.setState({open: !this.state.open})
  },
  componentDidMount() {
    this.props.optionsP.onValue(options => this.setState({options, selected: options.find(o => o.key == this.props.selected)}))
    window.addEventListener('click', this.handleClickOutside, false)
  },
  componentWillUnmount() {
    window.removeEventListener('click', this.handleClickOutside, false)
  },
  handleClickOutside(e) {
    const dropdown = e.target.closest('.dropdown')
    const clickedInside = dropdown && dropdown.getAttribute('id') == this.props.id
    !clickedInside && this.setState({open: false})
  },
  getInitialState() {
    return {
      options: [],
      open: false,
      selected: undefined
    }
  }
})