import React from 'react'

export default React.createClass({
  render() {
    const { options, open, selected } = this.state
    return (
      <div className="dropdown">
        <div className={selected ? 'select' : 'select no-selection'} onClick={this.openDropdown}>{selected ? selected : 'valitse'}</div>
        { open ?
          <ul className="options">
            {
              ['valitse'].concat(options).map(o => <li key={o} className="option" onClick={(e) => this.selectOption(e,o)}>{o}</li>)
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
    this.setState({selected: option == 'valitse' ? undefined : option, open: false})
    e.stopPropagation()
  },
  openDropdown(e) {
    this.setState({open: !this.state.open})
    e.stopPropagation()
  },
  closeDropdown() {
    this.setState({open: false})
  },
  componentDidMount() {
    this.props.optionP.onValue(options => this.setState({options}))
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