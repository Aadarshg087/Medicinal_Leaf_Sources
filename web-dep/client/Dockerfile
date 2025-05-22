# 1. Base image for building the app
FROM node:20-alpine AS builder

# Set working directory
WORKDIR /app

# Copy dependency definitions
COPY package*.json ./

# Set environment variable before build
ENV VITE_REACT_APP_API_URL=/api

# Install dependencies
RUN npm install

# Copy env file before build
COPY .env .env

# Copy rest of the app
COPY . .

# Build the app for production
RUN npm run build


# 2. Production image
FROM node:20-alpine AS runner

# Set working directory
WORKDIR /app

# Copy only the build output from the builder
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package*.json ./

# Optional: Install 'serve' to serve static files
RUN npm install -g serve

# Expose the port that the app will run on
EXPOSE 3000

# Run the app
CMD ["serve", "-s", "dist", "-l", "3000"]
