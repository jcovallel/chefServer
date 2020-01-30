#De la imagen que partimos
FROM openjdk:8-jdk-alpine
 
#Directorio de trabajo
WORKDIR /app
 
#Copiamos el uber-jar en el directorio de trabajo
COPY target/demo-0.0.1-SNAPSHOT.jar /app
 
#Exponemos el puerto 8080

 
#Comando que se ejecutará una vez ejecutemos el contendor
ENTRYPOINT ["java","-jar","demo-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080