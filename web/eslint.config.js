import mskelton from '@mskelton/eslint-config'

/** @type {import('eslint').Linter.Config[]} */
export default [
  ...mskelton.recommended,
  mskelton.react,
  {
    ignores: ['.next/'],
  },
  {
    rules: {
      'sort/object-properties': 'off',
    },
  },
]
