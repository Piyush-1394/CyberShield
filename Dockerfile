# syntax=docker/dockerfile:1

# ---- Stage 1: build the React frontend ---------------------------------
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
# .env.production (VITE_API_BASE_URL=) makes the built app call same-origin
# /api/... — no CORS, no separate frontend URL needed.
RUN npm run build

# ---- Stage 2: build the Spring Boot backend, embedding the frontend ----
FROM maven:3.9-eclipse-temurin-25 AS backend-build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -q -e -B dependency:go-offline || true
COPY backend/src ./src
# Drop the built frontend into Spring Boot's default static resource folder
# so the same jar serves both the UI and the API.
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn -q -e -B -DskipTests package

# ---- Stage 3: runtime -----------------------------------------------------
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN mkdir -p /data/reports
COPY --from=backend-build /app/target/cybershield-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
