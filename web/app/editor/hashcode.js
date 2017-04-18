let hashCode = (x) => {
  if (!x) return 0
  let hash = 0
  if (typeof x == 'string') {
    if (x.length == 0) return hash
    for (var i = 0; i < x.length; i++) {
      var char = x.charCodeAt(i)
      hash = hashAdd(hash, char)
    }
    return hash
  }
  if (typeof x == 'number') {
    return x
  }
  if (typeof x == 'boolean') {
    return x ? 1 : 0
  }
  if (x instanceof Date) {
    return x.getTime()
  }
  if (typeof x == 'object') {
    Object.getOwnPropertyNames(x).forEach((name) => hash = hashAdd(hash, hashCode(x[name])))
    return hash
  }
  if (x instanceof Array) {
    for (i in x) {
      hash = hashAdd(hash, hashCode(x[i]))
    }
    return hash
  }
  console.error('no hash for', x)
  return x
}

export default hashCode

let hashAdd = (hash, y) => {
  hash = ((hash<<5)-hash)+y
  return hash & hash // Convert to 32bit integer
}