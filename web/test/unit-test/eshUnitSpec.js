import * as assert from 'assert'
import { eshSynteettinenKoodiValidators } from '../../app/esh/europeanschoolofhelsinkiSuoritus'

describe('European School of Helsinki', () => {
  it('Preliminary synteettisten koodistojen validaatio toimii', () => {
    assert.equal(eshSynteettinenKoodiValidators.preliminary('0'), true, '0')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('0.5'), true, '0.5')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('0.6'), true, '0.6')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('0.7'), true, '0.7')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('1.0'), true, '1.0')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('1.5'), true, '1.5')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('2.0'), true, '2.0')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('2.5'), true, '2.5')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('3.0'), true, '3.0')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('3.5'), true, '3.5')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('4.0'), true, '4.0')
    assert.equal(eshSynteettinenKoodiValidators.preliminary('10'), true, '10')
    assert.equal(
      eshSynteettinenKoodiValidators.preliminary('10.0'),
      false,
      '10.0'
    )
  })
  it('Final synteettisten koodistojen validaatio toimii', () => {
    assert.equal(eshSynteettinenKoodiValidators.final('0.10'), true, '0.10')
    assert.equal(eshSynteettinenKoodiValidators.final('1.00'), true, '1.00')
    assert.equal(eshSynteettinenKoodiValidators.final('1.10'), true, '1.10')
    assert.equal(eshSynteettinenKoodiValidators.final('2.00'), true, '2.00')
    assert.equal(eshSynteettinenKoodiValidators.final('2.09'), true, '2.09')
    assert.equal(eshSynteettinenKoodiValidators.final('2.20'), true, '2.20')
    assert.equal(eshSynteettinenKoodiValidators.final('3.57'), true, '3.57')
    assert.equal(eshSynteettinenKoodiValidators.final('9.91'), true, '9.91')
    assert.equal(eshSynteettinenKoodiValidators.final('10'), true, '10')
    assert.equal(eshSynteettinenKoodiValidators.final('0.1'), false, '0.1')
    assert.equal(eshSynteettinenKoodiValidators.final('9.9'), false, '9.9')
    assert.equal(eshSynteettinenKoodiValidators.final('0'), false, '0')
    assert.equal(eshSynteettinenKoodiValidators.final('10.00'), false, '10.00')
  })
  it('Numerical synteettisten koodistojen validaatio toimii', () => {
    assert.equal(eshSynteettinenKoodiValidators.numerical('0'), true, '0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('0.5'), true, '0.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('1.0'), true, '1.0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('1.5'), true, '1.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('2.0'), true, '2.0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('2.5'), true, '2.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('3.0'), true, '3.0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('3.5'), true, '3.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('4.0'), true, '4.0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('4.5'), true, '4.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('5.0'), true, '5.0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('5.5'), true, '5.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('6.0'), true, '6.0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('7.5'), true, '7.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('8.0'), true, '8.0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('8.5'), true, '8.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('9.0'), true, '9.0')
    assert.equal(eshSynteettinenKoodiValidators.numerical('9.5'), true, '9.5')
    assert.equal(eshSynteettinenKoodiValidators.numerical('10'), true, '10')
    assert.equal(
      eshSynteettinenKoodiValidators.numerical('10.0'),
      false,
      '10.0'
    )
  })
})
