import * as A from 'fp-ts/Array'
import * as string from 'fp-ts/string'
import React, { useMemo } from 'react'
import { tExists, tTemplate } from '../../i18n/i18n'
import { common, CommonProps } from '../CommonProps'
import { ValidationError } from './validator'
import { useTestId } from '../../appstate/useTestId'

export type FieldErrorsProps = CommonProps<{
  errors?: ValidationError[]
  localErrors?: ValidationError[]
}>

/**
 * Virhetyypit, jotka näytetään pelkkänä kentän punaisena tilana ilman
 * tekstiriviä.
 *
 * Vanhassa käyttöliittymässä valtaosa kenttävirheistä ei tuota tekstiä
 * lainkaan: modelErrorMessages (editor/EditorModel.ts) suodattaa pois virheet
 * joilla ei ole message-kenttää, ja esimerkiksi valitsematta jäänyt pakollinen
 * pudotusvalikko näkyy vain vaaleanpunaisena kenttänä. Teksti on varattu
 * virheille, joilla on oikeasti jotain kerrottavaa.
 *
 * V2 teki päinvastoin: jokainen ValidationError käännettiin tekstiksi ja
 * renderöitiin omalle rivilleen. Koska nämä virheet syntyvät ja katoavat
 * kirjoittaessa, rivi kasvoi ja kutistui jatkuvasti ja siirsi koko
 * alapuolisen sivun.
 *
 * Alla listatut virheet eivät kerro mitään, mitä punainen kenttä ei jo kerro.
 * Virheet ovat edelleen voimassa: lomake on virheellinen eikä tallennu.
 * Listaa voi laajentaa, jos muitakin mekaanisia virheitä halutaan hiljentää -
 * huolehdi vain, että kentän editori välittää hasErrorsin kontrolliinsa asti,
 * ettei virhe jää kokonaan näkymättömiin.
 */
const VAIN_KENTÄN_VÄRINÄ: ReadonlySet<ValidationError['type']> = new Set([
  'emptyString',
  'emptyValue',
  'invalidDate'
])

const näytetäänTekstinä = (error: ValidationError): boolean =>
  !VAIN_KENTÄN_VÄRINÄ.has(error.type)

export const FieldErrors: React.FC<FieldErrorsProps> = (props) => {
  const errors: ValidationError[] = useMemo(
    () => [...(props.localErrors || []), ...(props.errors || [])],
    [props.localErrors, props.errors]
  )

  const messages = useMemo(
    () =>
      A.uniq(string.Eq)(
        errors.filter(näytetäänTekstinä).map(fieldErrorMessage)
      ),
    [errors]
  )

  const testId = useTestId('errors')

  return A.isNonEmpty(messages) ? (
    <ul {...common(props, ['FieldErrors'])} data-testid={testId}>
      {messages.map((message, index) => (
        <li key={index}>{message}</li>
      ))}
    </ul>
  ) : null
}

export const fieldErrorMessage = (error: ValidationError): string => {
  const messageKey = `validation:${error.type}`
  return tExists(messageKey)
    ? tTemplate(messageKey, error)
    : tTemplate('validation:other', { details: error })
}
