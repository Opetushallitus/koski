import Atom from 'bacon.atom'

const isMobile = () => window.innerWidth < 900
export const isMobileAtom = Atom(isMobile())

window.addEventListener('resize', () => isMobileAtom.set(isMobile()))
