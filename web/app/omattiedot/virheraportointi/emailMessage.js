import { t } from '../../i18n/i18n'

const VirheraporttiMessage = (() => {
  const placeholder = () => `***${t('Kirjoita viestisi tähän')}***`

  const spacer = () => '———————————————————————————————'

  const brief = () =>
    t(
      'Allaoleva teksti on luotu automaattisesti Opintopolun tiedoista. Koulu tarvitsee näitä tietoja pystyäkseen käsittelemään kysymystäsi.'
    )

  const details = (nimi, oppijaoid) =>
    [`${t('Nimi')}: ${nimi}`, `${t('Oppijanumero')}: ${oppijaoid}`]
      .filter((v) => !!v)
      .join('\n')

  return {
    placeholder,
    spacer,
    brief,
    details
  }
})()

export { VirheraporttiMessage }
