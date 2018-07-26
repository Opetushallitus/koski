import Atom from 'bacon.atom'

const isMobile = () => window.innerWidth <= 768
export const isMobileAtom = Atom(isMobile())

window.addEventListener('resize', () => isMobileAtom.set(isMobile()))
