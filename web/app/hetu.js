const re = /^(0[1-9]|1[0-9]|2[0-9]|3[0-1])(0[1-9]|1[0-2])([0-9][0-9])(A|-|\+)([0-9]{3})([0-9A-Y])$/
const checkChar = ['0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','H','J','K','L','M','N','P','R','S','T','U','V','W','X','Y']

export const isValidHetu = hetu => {
  if(!hetu || !re.test(hetu)) {
    return false
  }

  const [_, day, month, year, separator, identifier, checkCharacter] = hetu.match(re)

  if(checkCharacter !== checkChar[Math.round(((`${day}${month}${year}${identifier}` / 31) % 1) * 31)]) {
    return false
  }
  return isValidDate(day, month, year, separator)
}

const isValidDate = (day, month, yearDigits, separator) => {
  const birthDate = new Date(yearFromDigits(yearDigits, separator), month - 1, day)
  return birthDate && (birthDate.getDate() === parseInt(day) && birthDate.getMonth() + 1 === parseInt(month))
}

const yearFromDigits = (digits, separator) => {
  let completeYear
  switch (separator) {
    case '+':
      completeYear = '18' + digits
      break
    case '-':
      completeYear = '19' + digits
      break
    case 'A':
      completeYear = '20' + digits
  }
  return completeYear
}





