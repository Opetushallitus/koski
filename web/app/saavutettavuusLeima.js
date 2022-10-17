import { t, lang } from './i18n/i18n'

const saavutettavuusImages = {
  fi: '/koski/images/Saavutettavuus_leima_2016_FI.jpg',
  sv: '/koski/images/Saavutettavuus_leima_2016_SV.jpg',
  en: '/koski/images/Saavutettavuus_leima_2016_EN.jpg'
}

export const patchSaavutettavuusLeima = () =>
  waitForFooter((footerLogos) => {
    const alt = t('Saavutettavuus huomioitu')
    const url =
      'https://wiki.eduuni.fi/display/OPHPALV/Saavutettavuus+on+huomioitu'
    const imgSrc = saavutettavuusImages[lang]

    const img = document.createElement('img')
    img.style.width = '154px'
    img.setAttribute('alt', alt)
    img.setAttribute('src', imgSrc)

    const a = document.createElement('a')
    a.setAttribute('noclass', 'footer-item')
    a.setAttribute('href', url)
    a.setAttribute('title', alt)
    a.appendChild(img)

    const logoDiv = document.createElement('div')
    logoDiv.setAttribute('class', 'footer-item')
    logoDiv.setAttribute('id', 'footer-logos')
    logoDiv.appendChild(a)

    footerLogos.parentNode.appendChild(logoDiv)
  })

// Stop after 50 retries (5 seconds) to avoid infinite loop if running without oppija-raamit.
const waitForFooter = (callback, retries = 50) =>
  setTimeout(() => {
    const element = document.querySelector('footer#footer #footer-logos')
    if (element) {
      callback(element)
      return
    }

    if (retries > 0) {
      waitForFooter(callback, (retries = retries - 1))
    } else {
      console.warn(
        'Footer from oppija-raamit was not found. This is OK if really without oppija-raamit.'
      )
    }
  }, 100)
