import React from 'react'
import BaconComponent from './BaconComponent'

export default BaconComponent({
  render() {
    const { options, open, selected, selectionIndex } = this.state
    const { keyValue, displayValue } = this.props
    return (
      <div id={this.props.id} className="dropdown" tabIndex="0" ref={el => this.dropdown = el} onBlur={this.handleOnBlur} onKeyDown={this.onKeyDown}>
        <div className={selected ? 'select' : 'select no-selection'} onClick={this.toggleOpen} >{selected ? displayValue(selected) : 'valitse'}<span className="toggle-open"/></div>
        { open ?
          <ul className="options">
            {
              [{ value: 'ei valintaa' }].concat(options).map((o,i) => <li key={keyValue(o) || displayValue(o)} className={i == selectionIndex ? 'option selected' : 'option'} onClick={() => this.selectOption(o)} onMouseOver={() => this.handleMouseOver(o)}>{displayValue(o)}</li>)
            }
          </ul>
          : null
        }
      </div>
    )
  },
  handleOnBlur() {
    this.setState({open: false})
  },
  selectOption(option) {
    const selected = this.props.keyValue(option) ? option : undefined
    this.setState({selected: selected, open: false, selectionIndex: 0}, () => this.props.onSelectionChanged(selected))
  },
  toggleOpen() {
    this.setState({open: !this.state.open})
  },
  componentWillMount() {
    this.propsE.merge(this.unmountE).onValue(() => {
      window.removeEventListener('click', this.handleClickOutside, false)
    })
    this.propsE.onValue(props => {
      props.optionsP.takeUntil(this.unmountE).onValue(options => this.setState({options, selected: options.find(o => this.props.keyValue(o) == props.selected)}))
      window.addEventListener('click', this.handleClickOutside, false)
    })
  },
  getDefaultProps() {
    return {
      keyValue: option => option.key,
      displayValue: option => option.value
    }
  },
  handleClickOutside(e) {
    const dropdown = e.target.closest('.dropdown')
    const clickedInside = dropdown && dropdown.getAttribute('id') == this.props.id
    !clickedInside && this.setState({open: false})
  },
  handleMouseOver(o) {
    const { options } = this.state
    const index = options.findIndex(option => this.props.keyValue(option) == this.props.keyValue(o))
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
      e.stopPropagation()
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