FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/quickshop-1.0.0.jar app.jar

CMD ["java", "-cp", "app.jar", "com.quickshop.QuickShop"]
