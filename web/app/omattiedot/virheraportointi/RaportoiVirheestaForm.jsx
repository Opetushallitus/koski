import React from 'react'
import {PuuttuvatTiedot} from './PuuttuvatTiedot'
import Text from '../../i18n/Text'

const OppilaitosOption = ({oppilaitos}) => (
  <li className='oppilaitos-options__option'>
    <input
      type='radio'
      id='PLACEHOLDER'
      name='oppilaitos'
    />

    <label htmlFor='PLACEHOLDER'>
      <Text name={'PLACEHOLDER'}/>
    </label>
  </li>
)

export class RaportoiVirheestäForm extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      hasAcceptedDisclaimer: false
    }
  }

  render() {
    return (
      <div className='raportoi-virheestä-form'>
        <div className='puuttuvat-tiedot form-section' data-indent={0}>
          <PuuttuvatTiedot/>

          <input
            type='checkbox'
            id='puuttuvat-tiedot__checkbox'
            onChange={event => this.setState({hasAcceptedDisclaimer: event.target.checked})}
          />
          <label htmlFor='puuttuvat-tiedot__checkbox'>
            <Text name={'Opintojeni kuuluisi yllämainitun perusteella löytyä Koski-palvelusta'}/>{'*'}
          </label>
        </div>

        {this.state.hasAcceptedDisclaimer && (
          <div className='oppilaitos-options form-section' data-indent={1}>
            <h3><Text name={'Voit tiedustella asiaa oppilaitokseltasi.'}/></h3>
            <ul>
              <OppilaitosOption/>
            </ul>
          </div>
        )}
      </div>
    )
  }
}
