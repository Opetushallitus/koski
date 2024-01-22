/**
 * Osaamismerkkikuva
 *
 * @see `fi.oph.koski.servlet.Osaamismerkkikuva`
 */
export type Osaamismerkkikuva = {
  $class: 'fi.oph.koski.servlet.Osaamismerkkikuva'
  mimetype: string
  base64data: string
}

export const Osaamismerkkikuva = (o: {
  mimetype: string
  base64data: string
}): Osaamismerkkikuva => ({
  $class: 'fi.oph.koski.servlet.Osaamismerkkikuva',
  ...o
})

Osaamismerkkikuva.className = 'fi.oph.koski.servlet.Osaamismerkkikuva' as const

export const isOsaamismerkkikuva = (a: any): a is Osaamismerkkikuva =>
  a?.$class === 'fi.oph.koski.servlet.Osaamismerkkikuva'
