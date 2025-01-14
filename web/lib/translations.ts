// import ESV from './data/ESV.json'
import GNT from './data/GNT.json'
// import NIV from './data/NIV.json'
// import NKJV from './data/NKJV.json'
import type { Translation } from './types/translation'

export const translations = {
	// ESV,
	GNT,
	// NIV,
	// NKJV,
} as unknown as Record<string, Translation>
