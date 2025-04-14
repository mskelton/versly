FROM node:20-slim AS base

# Okay, I'm tired, and I want this app to work. So, I'm just manually uploading
# the DB in the Fly volume attached to the machine. Long term I'll have the web
# app download from the API when it boots and load into the volume, but I don't
# care enough now, I just want to get this thing launched.
ENV BIBLE_DATABASE_PATH="/app/data/bible.db"

# I also don't care about this. I'll long term have proper timezone support when
# there is auth and saved timezone for each user, but for now, just roll with CST.
ENV TZ="America/Chicago"

# Install dependencies only when needed
FROM base AS deps

WORKDIR /app

# Install dependencies
COPY package.json yarn.lock .yarnrc.yml ./
COPY .yarn/ .yarn/
RUN corepack enable
RUN yarn install --immutable

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app

ENV NODE_ENV=production

COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Build the app
RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV="production"
ENV PORT="3000"
ENV HOSTNAME="0.0.0.0"

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public

# Set the correct permissions for prerender cache
RUN mkdir .next
RUN chown nextjs:nodejs .next

# Create the DB data directory and set the correct permissions
RUN mkdir /app/data
RUN chown nextjs:nodejs /app/data

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/app/api-reference/next-config-js/output
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

CMD ["node", "server.js"]
