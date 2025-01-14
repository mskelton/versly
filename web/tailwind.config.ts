import type { Config } from 'tailwindcss'

export default {
	content: [
		'./components/**/*.{js,ts,jsx,tsx,mdx}',
		'./app/**/*.{js,ts,jsx,tsx,mdx}',
	],
	plugins: [],
	theme: {
		extend: {
			fontFamily: {
				sans: ['var(--font-sans)'],
			},
		},
	},
} satisfies Config
