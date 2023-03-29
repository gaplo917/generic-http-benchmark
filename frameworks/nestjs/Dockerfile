# First stage
FROM node:18-alpine as FirstStage

# Create app directory
WORKDIR /usr/src/app

# Install dependencies
COPY package*.json ./
COPY tsconfig.json ./

RUN npm install

# Final stage
FROM node:18-alpine

WORKDIR /usr/src/app

# Copy all files from builder
COPY --from=FirstStage /usr/src/app /usr/src/app

# Copy source files
COPY . .

# Build app and remove Dev Dependencies
RUN npm run build && npm install --production

EXPOSE 8080

# Run the app
CMD [ "npm", "run", "start:prod" ]
