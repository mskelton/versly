import type { Config } from 'tailwindcss'

export default {
	content: ['./app/**/*.{js,ts,jsx,tsx,mdx}'],
	plugins: [],
	theme: {
		extend: {
			animation: {
				progress: 'progress 1ms linear',
			},
			fontFamily: {
				sans: ['var(--font-sans)'],
			},
			keyframes: {
				progress: {
					from: {
						width: '0',
					},
					to: {
						width: '100%',
					},
				},
			},
		},
	},
} satisfies Config
