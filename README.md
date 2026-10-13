# FixLab

Aplicación de escritorio para la gestión de un servicio técnico de reparación de dispositivos. FixLab centraliza la información de clientes, reparaciones, citas y presupuestos en una interfaz gráfica desarrollada con JavaFX.

## Funcionalidades

- **Acceso de usuarios:** inicio de sesión y registro de usuarios.
- **Gestión de clientes:** registro y consulta de los datos de contacto de los clientes.
- **Gestión de reparaciones:** seguimiento de los dispositivos recibidos, su estado y sus fechas de entrada y salida.
- **Agenda:** organización de citas y consulta de la agenda.
- **Gestión de presupuestos:** creación de presupuestos vinculados a clientes y reparaciones, con líneas, importes e IVA.
- **Persistencia local:** almacenamiento de los datos en una base de datos SQLite.
- **Contraseñas protegidas:** las contraseñas se codifican con BCrypt antes de guardarse.

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal |
| JavaFX 21 | Interfaz gráfica de escritorio |
| FXML | Definición de vistas |
| CSS | Estilos de la interfaz |
| Spring Boot 3.2.3 | Configuración e inicialización de la aplicación |
| Spring Data JPA | Acceso y persistencia de datos |
| Hibernate | Mapeo objeto-relacional |
| SQLite | Base de datos local |
| Maven | Gestión de dependencias y ejecución del proyecto |
| JUnit / Spring Boot Test | Dependencias de pruebas |

## Requisitos previos

- JDK 21 instalado.
- Git, si vas a clonar el repositorio.
- No es necesario instalar SQLite por separado: el proyecto incluye el controlador JDBC y utiliza una base de datos local.

Comprueba la instalación de Java desde una terminal:

```bash
java -version
```

La versión instalada debe ser Java 21.

## Instalación y ejecución

1. Clona el repositorio:

   ```bash
   git clone https://github.com/jdzuluaga18/fixlab.git
   cd fixlab
   ```

2. Ejecuta la aplicación con el Maven Wrapper incluido en el proyecto.

   **macOS / Linux:**

   ```bash
   chmod +x mvnw
   ./mvnw clean javafx:run
   ```

   **Windows (CMD o PowerShell):**

   ```bat
   mvnw.cmd clean javafx:run
   ```

La primera ejecución puede tardar mientras Maven descarga las dependencias necesarias.

## Base de datos

FixLab utiliza SQLite y crea/actualiza las tablas mediante JPA/Hibernate. La base de datos se guarda en la carpeta del usuario del sistema:

```text
~/.fixlab/demo.db
```

En macOS y Linux, `~` representa el directorio personal del usuario. En Windows, la ruta corresponde al directorio personal configurado para Java.

La aplicación crea la carpeta `.fixlab` al iniciarse. Para conservar los datos, realiza copias de seguridad del archivo `demo.db` con la aplicación cerrada.

### Usuario inicial

En una instalación nueva, el proyecto crea un usuario administrador si todavía no hay usuarios registrados:

- **Usuario:** `admin`
- **Contraseña inicial:** `admin123`

**Seguridad:** estas credenciales son públicas porque forman parte del código del proyecto. Cámbialas o modifica el inicializador antes de utilizar FixLab con datos reales. No reutilices esta contraseña en otros servicios.

## Estructura del proyecto

```text
fixlab/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    └── main/
        ├── java/com/demoapp/
        │   ├── config/        # Inicialización y contexto de Spring
        │   ├── controller/    # Controladores de las vistas
        │   ├── model/         # Entidades del dominio
        │   ├── repository/    # Acceso a datos
        │   ├── service/       # Lógica de negocio
        │   ├── util/          # Utilidades
        │   └── ...            # Arranque y navegación
        └── resources/
            ├── css/           # Estilos de la interfaz
            ├── fxml/          # Vistas JavaFX
            ├── application.properties
            └── icon.png
```

## Arquitectura

El código se organiza por responsabilidades:

- **Modelo:** representa las entidades principales, como `Cliente`, `Reparacion`, `Cita`, `Presupuesto` y `Usuario`.
- **Vista:** los archivos FXML definen las pantallas y los archivos CSS sus estilos.
- **Controladores:** conectan las acciones de la interfaz con la lógica de la aplicación.
- **Servicios:** agrupan operaciones de negocio.
- **Repositorios:** gestionan la persistencia mediante Spring Data JPA.

## Pruebas y compilación

Para compilar el proyecto y ejecutar las pruebas disponibles:

```bash
./mvnw clean verify
```

En Windows:

```bat
mvnw.cmd clean verify
```

> El repositorio incluye la dependencia de pruebas de Spring Boot, pero no contiene actualmente archivos de pruebas bajo `src/test`. Conviene añadir pruebas unitarias para servicios y pruebas de persistencia.

## Posibles mejoras

- Añadir pruebas automatizadas para las operaciones principales.
- Incorporar exportación e impresión de presupuestos.
- Mejorar la gestión de roles y permisos.
- Añadir copias de seguridad y restauración desde la interfaz.
- Preparar un paquete instalable para facilitar la distribución de la aplicación.
- Añadir capturas de pantalla al README para mostrar las principales vistas.

## Autor

**Juan David Zuluaga Granados**

- GitHub: [@jdzuluaga18](https://github.com/jdzuluaga18)

## Licencia

Actualmente no se especifica una licencia en este README. Si deseas permitir que otras personas reutilicen o modifiquen el proyecto, añade un archivo `LICENSE` con la licencia elegida.
