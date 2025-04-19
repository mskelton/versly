declare let globalThis: {
	instances?: Record<symbol, any>
}

export function yolo<T>(initializer: () => T): T {
	const sym = Symbol()

	if (process.env.NODE_ENV !== 'production') {
		if (!globalThis.instances?.[sym]) {
			globalThis.instances ??= {}
			globalThis.instances[sym] = initializer()
		}

		return globalThis.instances[sym]
	} else {
		return initializer()
	}
}
