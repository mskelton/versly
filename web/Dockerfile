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

# Build time environment variables
ARG NEXT_PUBLIC_GA_ID

# Build the app
RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV="production"
ENV PORT="3000"
ENV HOSTNAME="0.0.0.0"

COPY --from=builder /app/public ./public

# Create the .next directory for the prerender cache
RUN mkdir .next

# Create the DB data directory
RUN mkdir /app/data

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/app/api-reference/next-config-js/output
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000

CMD ["node", "server.js"]
