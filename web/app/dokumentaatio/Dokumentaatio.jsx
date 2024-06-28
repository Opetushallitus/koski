import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as R from 'ramda'
import Http from '../util/http'
import '../../node_modules/codemirror/mode/javascript/javascript.js'
import { contentWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import { ApiOperations } from './DokumentaatioApiTester'
import Link from '../components/Link'
import Text from '../i18n/Text'

const JsonExampleTable = ({ contents }) => {
  return (
    <table
      className="json"
      dangerouslySetInnerHTML={{ __html: contents }}
    ></table>
  )
}

const JsonExample = ({ category, example }) => {
  const expandedA = Atom(false)

  return (
    <li className="example-item">
      <a className="example-link" onClick={() => expandedA.modify((v) => !v)}>
        {example.description}
      </a>

      <a
        className="example-as-json"
        href={example.link}
        target="_blank"
        rel="noopener noreferrer"
      >
        {'lataa JSON'}
      </a>

      {expandedA.flatMap((v) =>
        v
          ? Http.cachedGet(
              '/koski/api/documentation/categoryExamples/' +
                category +
                '/' +
                example.name +
                '/table.html'
            ).map((c) => <JsonExampleTable contents={c} />)
          : null
      )}
    </li>
  )
}

const naviLink = (
  path,
  textKey,
  location,
  linkClassName,
  isSelected = (p, l) => p === l
) => {
  const className = `${textKey
    .toLowerCase()
    .replace(/ /g, '')} navi-link-container${
    isSelected(path, location) ? ' selected' : ''
  }`
  return (
    <span className={className}>
      <Link href={path} className={linkClassName}>
        <Text name={textKey} />
      </Link>
    </span>
  )
}

const dokumentaatioContentP = (location, contentP) =>
  contentWithLoadingIndicator(contentP).map((content) => ({
    content: (
      <div className="content-area dokumentaatio">
        <nav className="sidebar dokumentaatio-navi">
          {naviLink('/koski/dokumentaatio', 'Yleistä', location, '')}

          {naviLink(
            '/koski/dokumentaatio/tietomalli',
            'Tietomalli',
            location,
            ''
          )}

          {naviLink(
            '/koski/dokumentaatio/koodistot',
            'Koodistot',
            location,
            ''
          )}

          {naviLink(
            '/koski/dokumentaatio/rajapinnat/opintohallintojarjestelmat',
            'Rajapinnat opinto\u00adhallintojärjestelmille',
            location,
            ''
          )}

          {naviLink(
            '/koski/dokumentaatio/rajapinnat/luovutuspalvelu',
            'Rajapinnat viranomaisille (luovutuspalvelu)',
            location,
            ''
          )}

          {naviLink(
            '/koski/dokumentaatio/rajapinnat/palveluvayla-omadata',
            'Palveluväylä- ja omadata-rajapinnat',
            location,
            ''
          )}

          {naviLink(
            '/koski/dokumentaatio/rajapinnat/massaluovutus/koulutuksenjarjestajat',
            'Massaluovutusrajapinnat koulutuksenjärjestäjille',
            location,
            ''
          )}

          {naviLink(
            '/koski/dokumentaatio/rajapinnat/massaluovutus/oph',
            'Rajapinnat Opetushallituksen palveluille',
            location,
            ''
          )}
        </nav>

        <div className="main-content dokumentaatio-content">
          {content.content}
        </div>
      </div>
    ),
    title: content.title
  }))

const htmlSectionsP = () =>
  Http.cachedGet('/koski/api/documentation/sections.html')

export const dokumentaatioYleistäP = () =>
  dokumentaatioContentP(
    '/koski/dokumentaatio',
    htmlSectionsP().map((htmlSections) => ({
      content: (
        <div>
          <section
            dangerouslySetInnerHTML={{ __html: htmlSections.yleista }}
          ></section>
        </div>
      ),
      title: 'Dokumentaatio'
    }))
  )

const infoP = () =>
  Bacon.combineTemplate({
    categories: Http.cachedGet('/koski/api/documentation/categoryNames.json'),
    examples: Http.cachedGet(
      '/koski/api/documentation/categoryExampleMetadata.json'
    ),
    apiOperations: Http.cachedGet(
      '/koski/api/documentation/apiOperations.json'
    ),
    htmlSections: Http.cachedGet('/koski/api/documentation/sections.html'),
    koodistot: Http.cachedGet('/koski/api/documentation/koodistot.json')
  })

export const dokumentaatioTietomalliP = () =>
  dokumentaatioContentP(
    '/koski/dokumentaatio/tietomalli',
    infoP().map(({ categories, examples, htmlSections }) => ({
      content: (
        <div>
          <section
            dangerouslySetInnerHTML={{ __html: htmlSections.tietomalli }}
          ></section>

          <section>
            <div
              dangerouslySetInnerHTML={{
                __html: htmlSections.tietomalli_esimerkit
              }}
            ></div>

            {R.map(
              (c) => (
                <div key={c}>
                  <h4>{c}</h4>

                  <ul className="example-list">
                    {R.addIndex(R.map)(
                      (e, idx) => (
                        <JsonExample key={idx} category={c} example={e} />
                      ),
                      examples[c]
                    )}
                  </ul>
                </div>
              ),
              categories
            )}
          </section>
        </div>
      ),
      title: 'Dokumentaatio - Tietomalli'
    }))
  )

export const dokumentaatioKoodistotP = () =>
  dokumentaatioContentP(
    '/koski/dokumentaatio/koodistot',
    infoP().map(({ koodistot, htmlSections }) => ({
      content: (
        <div>
          <div
            dangerouslySetInnerHTML={{ __html: htmlSections.koodistot }}
          ></div>

          <ul>
            {koodistot.map((koodistoUri) => (
              <li>
                <a
                  href={
                    '/koski/dokumentaatio/koodisto/' + koodistoUri + '/latest'
                  }
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {koodistoUri}
                </a>
              </li>
            ))}
          </ul>
        </div>
      ),
      title: 'Dokumentaatio - Koodistot'
    }))
  )

export const dokumentaatioOpintohallintojärjestelmätP = () =>
  dokumentaatioContentP(
    '/koski/dokumentaatio/rajapinnat/opintohallintojarjestelmat',
    infoP().map(({ apiOperations, htmlSections }) => ({
      content: (
        <div>
          <section
            dangerouslySetInnerHTML={{
              __html: htmlSections.rajapinnat_oppilashallintojarjestelmat
            }}
          ></section>

          <ApiOperations operations={apiOperations} />
        </div>
      ),
      title: 'Dokumentaatio - Rajapinnat '
    }))
  )

export const dokumentaatioLuovutuspalveluP = () =>
  dokumentaatioContentP(
    '/koski/dokumentaatio/rajapinnat/luovutuspalvelu',
    htmlSectionsP().map((htmlSections) => ({
      content: (
        <div>
          <div
            className="markdown-content"
            dangerouslySetInnerHTML={{
              __html: htmlSections.rajapinnat_luovutuspalvelu
            }}
          ></div>
        </div>
      ),
      title: 'Dokumentaatio - Rajapinnat'
    }))
  )

export const dokumentaatioPalveluväyläOmadataP = () =>
  dokumentaatioContentP(
    '/koski/dokumentaatio/rajapinnat/palveluvayla-omadata',
    htmlSectionsP().map((htmlSections) => ({
      content: (
        <div>
          <div
            className="markdown-content"
            dangerouslySetInnerHTML={{
              __html: htmlSections.rajapinnat_palveluvayla_omadata
            }}
          ></div>
        </div>
      ),
      title: 'Dokumentaatio - Rajapinnat'
    }))
  )

export const dokumentaatioKyselytP = (path) => {
  const basePath = '/koski/dokumentaatio/rajapinnat/massaluovutus'
  const sections = {
    koulutuksenjarjestajat: 'massaluovutus_koulutuksenjarjestajat',
    oph: 'massaluovutus_oph'
  }
  const match = Object.keys(sections).find((key) =>
    path.includes(`${basePath}/${key}`)
  )
  if (match) {
    return dokumentaatioContentP(
      `${basePath}/${match}`,
      htmlSectionsP().map((htmlSections) => ({
        content: (
          <div>
            <div
              className="markdown-content"
              dangerouslySetInnerHTML={{
                __html: htmlSections[sections[match]]
              }}
            ></div>
          </div>
        ),
        title: 'Dokumentaatio - Rajapinnat'
      }))
    )
  }
}
