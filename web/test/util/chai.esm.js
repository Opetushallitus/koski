import '../lib/chai.js'

// Chai is a CommonJS module, so we use this hack to import it as an ES Module in the browser

// eslint-disable-next-line no-undef
const chai = globalThis.chai

export const expect = chai.expect
export default chai
