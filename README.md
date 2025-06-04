# Travel Application

A JavaFX application with H2 database integration for managing travel information.

## Requirements

- Java 21 or higher
- Maven (included via wrapper)
- Launch4j (optional, for creating executable)

## Running the Application

To run the application:

```bash
./mvnw javafx:run
```

## Building an Uber JAR

The project is configured to create an uber JAR (fat JAR) that includes all dependencies:

```bash
./mvnw clean package
```

This will create a file named `travel-1.0-SNAPSHOT-uber.jar` in the `target` directory.

## Creating an Executable with Launch4j

1. Download and install Launch4j from [http://launch4j.sourceforge.net/](http://launch4j.sourceforge.net/)
2. Create a new Launch4j configuration with the following settings:
   - **Basic tab**:
     - Output file: Choose where to save the .exe file
     - Jar: Select the uber JAR file (`target/travel-1.0-SNAPSHOT-uber.jar`)
     - Icon: Select the travel icon file (`src/main/resources/travel-icon.png` or `.ico` format)
   - **JRE tab**:
     - Min JRE version: 21
   - **Classpath tab**:
     - Main class: `com.example.travel.App`
3. Click "Build wrapper" to create the executable

## Custom Icon

The application uses a custom travel icon. To replace it:

1. Place your icon file named `travel-icon.png` in the `src/main/resources` directory
2. Rebuild the application

## Database

The application uses H2 database with file storage. The database file will be created automatically in the project root directory as `traveldb.mv.db`.
