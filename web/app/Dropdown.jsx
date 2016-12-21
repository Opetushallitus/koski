import React from 'react'

export default React.createClass({
  render() {
    const { options, open, selected } = this.state
    return (
      <div className="dropdown" onBlur={this.handleBlur} tabIndex="0" ref={el => this.dropdown = el}>
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
    e.stopPropagation()
  },
  openDropdown() {
    this.setState({open: !this.state.open})
  },
  handleBlur() {
    this.setState({open: false})
  },
  componentDidMount() {
    this.props.optionsP.onValue(options => this.setState({options, selected: options.find(o => o.key == this.props.selected)}))
  },
  getInitialState() {
    return {
      options: [],
      open: false,
      selected: undefined
    }
  }
})