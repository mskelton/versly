export const logger = {
  debug: (message: string) => console.debug(`[DEBUG] ${message}`),
  error: (message: string) => console.error(`[ERROR] ${message}`),
  info: (message: string) => console.log(`[INFO] ${message}`),
  warn: (message: string) => console.warn(`[WARN] ${message}`),
}
