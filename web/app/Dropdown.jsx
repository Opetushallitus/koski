import React from 'react'

export default React.createClass({
  render() {
    const { options, open, selected, selectionIndex } = this.state
    return (
      <div id={this.props.id} className="dropdown" tabIndex="0" ref={el => this.dropdown = el} onKeyDown={this.onKeyDown}>
        <div className={selected ? 'select' : 'select no-selection'} onClick={this.toggleOpen} >{selected ? selected.value : 'valitse'}</div>
        { open ?
          <ul className="options">
            {
              [{ value: 'ei valintaa' }].concat(options).map((o,i) => <li key={o.key || o.value} className={i == selectionIndex ? 'option selected' : 'option'} onClick={e => this.selectOption(o)} onMouseOver={e => this.handleMouseOver(o)}>{o.value}</li>)
            }
          </ul>
          : null
        }
      </div>
    )
  },
  selectOption(option) {
    const selected = option.key ? option : undefined
    this.setState({selected: selected, open: false, selectionIndex: 0}, () => this.props.onSelectionChanged(selected))
  },
  toggleOpen() {
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
  handleMouseOver(o) {
    const { options } = this.state
    const index = options.findIndex(option => option.key == o.key)
    this.setState({selectionIndex: index + 1})
  },
  getInitialState() {
    return {
      options: [],
      open: false,
      selected: undefined,
      selectionIndex: 0
    }
  },
  onKeyDown(e) {
    let handler = this.keyHandlers[e.key]
    if(handler) {
      handler.call(this, e)
    }
  },
  keyHandlers: {
    ArrowUp() {
      let {selectionIndex} = this.state
      selectionIndex = selectionIndex === 0 ? 0 : selectionIndex - 1
      this.setState({selectionIndex: selectionIndex})
    },
    ArrowDown(e) {
      e.preventDefault()
      if(this.state.open) {
        let {selectionIndex, options} = this.state
         selectionIndex = selectionIndex === options.length ? selectionIndex : selectionIndex + 1
         this.setState({selectionIndex: selectionIndex})
      } else {
        this.setState({open: true})
      }
    },
    Escape() {
      this.setState({open: false})
    },
    Enter(e) {
      e.preventDefault()
      let {selectionIndex, options} = this.state
      this.selectOption(selectionIndex == 0 ? {value: 'ei valintaa'} : options[selectionIndex - 1])
    }
  }
})