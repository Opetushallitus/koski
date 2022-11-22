import React from 'baret'
import { lang, setLang } from '../i18n/i18n'

export const ChangeLang = () => (
  <div className="change-lang">
    {lang !== 'fi' ? (
      <button
        id={'change-lang-fi'}
        onClick={() => setLang('fi')}
        title={'Suomeksi'}
      >
        {'Suomi'}
      </button>
    ) : null}

    {lang !== 'sv' ? (
      <button
        id={'change-lang-sv'}
        onClick={() => setLang('sv')}
        title={'På svenska'}
      >
        {'Svenska'}
      </button>
    ) : null}

    {lang !== 'en' ? (
      <button
        id={'change-lang-en'}
        onClick={() => setLang('en')}
        title={'In English'}
      >
        {'English'}
      </button>
    ) : null}
  </div>
)
