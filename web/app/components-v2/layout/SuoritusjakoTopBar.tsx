import React from 'react'

import { t, lang, setLang } from '../../i18n/i18n'

export const SuoritusjakoTopBar = () => {
  return (
    <header id="topbar" className="suoritusjako">
      <div className="topbar-content-wrapper">
        <img
          className="opintopolku-logo"
          src="/koski/images/oma-opintopolku_ikoni.svg"
        />
        <h1>
          <a href="/oma-opintopolku/">
            <span>{t('Oma Opintopolku', false, lang)}</span>
          </a>
        </h1>
        <ChangeLang />
      </div>
    </header>
  )
}
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
