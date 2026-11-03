import React from '../util/baret'
import Bacon from 'baconjs'
import Atom from '../util/bacon.atom'
import * as R from 'ramda'
import Http from '../util/http'
import { contentWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import { ApiOperations } from './DokumentaatioApiTester'
import Link from '../components/Link'
import { replaceLocationSilently } from '../util/router'
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

const massaluovutusBasePath = '/koski/dokumentaatio/rajapinnat/massaluovutus'

const massaluovutusPages = [
  {
    path: '',
    title: 'Massaluovutusrajapinnat',
    section: 'massaluovutus_yleista'
  },
  {
    path: '/koulutuksenjarjestajat',
    title: 'Koulutuksenjärjestäjät',
    section: 'massaluovutus_koulutuksenjarjestajat'
  },
  { path: '/raportit', title: 'Raportit', section: 'massaluovutus_raportit' },
  {
    path: '/valpas',
    title: 'Oppivelvollisuuden valvonta',
    section: 'massaluovutus_valpas'
  }
]

// Avaa ankkurin osoittaman kyselyosion avattavat lohkot ja vierittää siihen, jotta jaettu
// linkki (esim. #tuvaSuoritustiedot) näyttää parametrit ja esimerkin suoraan.
const revealAnchor = (hash) => {
  const target = hash && document.getElementById(hash.slice(1))
  if (!target) return false
  const section = target.closest('.query-doc') || target
  section.querySelectorAll('details').forEach((details) => {
    details.open = true
  })
  target.scrollIntoView()
  return true
}

// Sisältö ladataan asynkronisesti, joten selaimen oma hash-vieritys ehtii ajaa ennen kuin
// kohde on olemassa. Hash käsitellään, kun markdown-sisältö on liitetty DOM:iin.
const revealAnchorOnMount = (element) => {
  if (element) {
    revealAnchor(window.location.hash)
  }
}

// Sivun sisäiset ankkurilinkit käsitellään itse: hash-muutos popstate-tapahtumana
// ajaisi reitityksen uudelleen ja piirtäisi sivun uusiksi vierityksen alta.
const scrollToAnchor = (event) => {
  const link = event.target.closest('a[href^="#"]')
  if (!link) return
  const href = link.getAttribute('href')
  if (revealAnchor(href)) {
    event.preventDefault()
    replaceLocationSilently(href)
  }
}

// Yleiset ohjeet on ryhmän päälinkki, muut sivut sen alle sisennettyinä
const massaluovutusNavi = (location) =>
  massaluovutusPages.map((page, index) => {
    const path = massaluovutusBasePath + page.path
    const selected = path === location
    return (
      <React.Fragment key={path}>
        <span
          className={`navi-link-container${
            index > 0 ? ' navi-sublink-container' : ''
          }${selected ? ' selected' : ''}`}
        >
          <Link href={path}>
            <Text name={page.title} />
          </Link>
        </span>
      </React.Fragment>
    )
  })

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

          {naviLink('/koski/dokumentaatio/valpas', 'Valpas', location, '')}

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
            '/koski/dokumentaatio/rajapinnat/oauth2/omadata',
            'OAuth2-omadata-rajapinnat',
            location,
            ''
          )}

          {naviLink(
            '/koski/dokumentaatio/rajapinnat/massaluovutus/oph',
            'Rajapinnat Opetushallituksen palveluille',
            location,
            ''
          )}

          {massaluovutusNavi(location)}
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
  const page = massaluovutusPages.find(
    (p) => massaluovutusBasePath + p.path === path
  )
  const ophPath = `${massaluovutusBasePath}/oph`
  const section = page
    ? page.section
    : path === ophPath
      ? 'massaluovutus_oph'
      : undefined
  if (!section) {
    return undefined
  }
  return dokumentaatioContentP(
    path,
    htmlSectionsP().map((htmlSections) => ({
      content: (
        <div>
          <div
            className="markdown-content"
            ref={revealAnchorOnMount}
            onClick={scrollToAnchor}
            dangerouslySetInnerHTML={{
              __html: htmlSections[section]
            }}
          ></div>
        </div>
      ),
      title: 'Dokumentaatio - Rajapinnat'
    }))
  )
}

export const dokumentaatioValpasP = () =>
  dokumentaatioContentP(
    '/koski/dokumentaatio/valpas',
    htmlSectionsP().map((htmlSections) => ({
      content: (
        <div>
          <div
            className="markdown-content"
            dangerouslySetInnerHTML={{ __html: htmlSections.valpas }}
          ></div>
        </div>
      ),
      title: 'Dokumentaatio - Valpas'
    }))
  )

export const dokumentaatioOmadataOAuth2P = (path) => {
  const basePath = '/koski/dokumentaatio/rajapinnat/oauth2'
  const sections = {
    omadata: 'oauth2_omadata'
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
