# Copy pom.xml first (for caching)
COPY pom.xml . 

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Install dependencies offline
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build without running tests
RUN ./mvnw clean install -DskipTests
