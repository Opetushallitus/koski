import React from 'react'
import * as R from 'ramda'
import {t} from '../i18n/i18n'

export const FootnoteHint = ({title = '', hint = '*'}) =>
  <sup className='footnote-hint' title={t(title)}>{` ${hint}`}</sup>

FootnoteHint.displayName = 'FootnoteHint'

export const FootnoteDescriptions = ({data}) =>
  <p className='selitteet'>{data.map(d => `${d.hint} = ${R.toLower(t(d.title))}`).join(', ')}</p>

FootnoteDescriptions.displayName = 'FootnoteDescriptions'
