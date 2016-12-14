import React from 'react'

export default React.createClass({
  render() {
    const { options, open, selected } = this.state
    return (
      <div className="dropdown" onFocus={e => this.handleFocus(e)} onBlur={e => this.handleBlur(e)} tabIndex="0" ref={el => this.dropdown = el}>
        <div className={selected ? 'select' : 'select no-selection'} onClick={this.openDropdown} >{selected ? selected.value : 'valitse'}</div>
        { open ?
          <ul className="options">
            {
              [{ value: 'ei valintaa' }].concat(options).map(o => <li key={o.key || o.value} className="option" onClick={(e) => this.selectOption(e,o)}>{o.value}</li>)
            }
          </ul>
          : null
        }
      </div>
    )
  },
  handleClickOutside() {
    this.setState({ open: false })
  },
  selectOption(e, option) {
    const selected = option.key ? option : undefined
    this.setState({selected: selected, open: false}, () => this.props.onSelectionChanged(selected))
    this.dropdown.blur()
    e.stopPropagation()
  },
  openDropdown(e) {
    this.setState({open: !this.state.open})
    e.stopPropagation()
  },
  closeDropdown() {
    !this.state.active && this.setState({open: false})
  },
  handleFocus(e) {
    this.setState({active:true})
  },
  handleBlur(e) {
    this.setState({open: false, active: false})
  },
  componentDidMount() {
    this.props.optionsP.onValue(options => this.setState({options, selected: options.find(o => o.key == this.props.selected)}))
    window.addEventListener('click', this.closeDropdown, false)
  },
  componentWillUnmount() {
    window.removeEventListener('click', this.closeDropdown, false)
  },
  getInitialState() {
    return {
      options: [],
      open: false,
      selected: undefined
    }
  }
})