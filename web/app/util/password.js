// avoid similar-looking characters (like letter O and digit zero)
const PasswordCharacters = 'ABCDEFGHJKLMNPRSTUVWXYZ2345689'
const PasswordLength = 10

export function generateRandomPassword() {
  const randomValues = new Uint32Array(PasswordLength)
  if (window.crypto && window.crypto.getRandomValues) {
    window.crypto.getRandomValues(randomValues)
  } else if (window.msCrypto && window.msCrypto.getRandomValues) {
    window.msCrypto.getRandomValues(randomValues)
  } else {
    throw new Error('window.crypto not supported')
  }
  let tmp = ''
  for (let i = 0; i < randomValues.length; i++) {
    tmp += PasswordCharacters.charAt(
      randomValues[i] % PasswordCharacters.length
    )
  }
  return tmp
}
