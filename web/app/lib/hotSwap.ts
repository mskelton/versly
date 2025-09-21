declare let globalThis: {
  instances?: Record<symbol, any>
}

export type HotSwappable<T> = T & {
  /** Swap the instance with a new one */
  swap: (value: T) => void
}

export function hotSwap<T>(initializer: () => T): HotSwappable<T> {
  const sym = Symbol()

  if (!globalThis.instances?.[sym]) {
    globalThis.instances ??= {}
    globalThis.instances[sym] = initializer()
  }

  const swappable = {
    swap: (value: T) => {
      globalThis.instances![sym] = value
    },
  }

  return new Proxy(swappable as any, {
    get(_, prop) {
      return prop === 'swap' ? swappable.swap : globalThis.instances![sym][prop]
    },
  })
}
