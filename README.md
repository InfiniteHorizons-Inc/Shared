# English _🇺🇸_

## _Project Description Shared_

This project provides a set of shared tools and constants, ideally used for developing applications that require version management, secure authentication, and handling operations through an opcode system. It also includes facilities for managing user permissions and matching patterns for specific inputs.

### _Components_


- **Version**: Provides a structure for representing and comparing software versions using version numbers and optionally a build identifier.
- **SharedConstants**: Define shared constants such as colors, GitHub URLs, and operation prefixes, which can be used in different parts of an application to maintain consistency.
- **SecurityLevel**: Defines different security levels, such as CONTRIBUTOR, STAFF, DEVELOPER, and OWNER, along with a method for checking whether a security level is authorized for certain operations. This is useful for managing permissions and access in a role-based system.
- **Regex**: Provides regular expression patterns for different types of common identifiers on platforms like Discord (user mentions, roles, channels, etc.), making text parsing and validation easier.
- **OpCodes**: Defines operation codes that could be used to identify types of messages or commands in a communication system or network protocol, which is essential for a message-based architecture.
- **ExitCodes**: Defines exit codes that can be used to indicate different types of program terminations, such as stopping processes or restarting applications.
- **Auth**: Provides functionality to create and verify JWT (JSON Web Tokens), which is crucial for implementing a secure authentication and authorization system in web applications.
- **ThreadPoolExecutorLogged**: Extends ThreadPoolExecutor to add logging capabilities, which helps handle unhandled exceptions in threads, improving the reliability and traceability of concurrent task execution.

# Español _🇪🇸_

## _Descripción del proyecto Shared_

Este proyecto proporciona un conjunto de herramientas y constantes compartidas, ideales para desarrollar aplicaciones que requieren administración de versiones, autenticación segura y manejo de operaciones a través de un sistema de código de operación. También incluye funciones para administrar permisos de usuario y patrones coincidentes para entradas específicas.

### _Componentes_

- **Version**: proporciona una estructura para representar y comparar versiones de software mediante números de versión y, opcionalmente, un identificador de compilación.
- **SharedConstants**: define constantes compartidas como colores, URL de GitHub y prefijos de operación, que se pueden usar en diferentes partes de una aplicación para mantener la coherencia.
- **SecurityLevel**: define diferentes niveles de seguridad, como COLABORADOR, PERSONAL, DESARROLLADOR y PROPIETARIO, junto con un método para verificar si un nivel de seguridad está autorizado para ciertas operaciones. Esto es útil para administrar permisos y acceso en un sistema basado en roles.
- **Regex**: proporciona patrones de expresiones regulares para diferentes tipos de identificadores comunes en plataformas como Discord (menciones de usuarios, roles, canales, etc.), lo que facilita el análisis y la validación de texto.
- **OpCodes**: define códigos de operación que se pueden usar para identificar tipos de mensajes o comandos en un sistema de comunicación o protocolo de red, lo cual es esencial para una arquitectura basada en mensajes.
- **ExitCodes**: define códigos de salida que se pueden usar para indicar diferentes tipos de terminaciones de programas, como detener procesos o reiniciar aplicaciones.
- **Auth**: proporciona funcionalidad para crear y verificar JWT (JSON Web Tokens), lo cual es crucial para implementar un sistema de autenticación y autorización seguro en aplicaciones web.
- **ThreadPoolExecutorLogged**: extiende ThreadPoolExecutor para agregar capacidades de registro, lo que ayuda a manejar excepciones no controladas en subprocesos, lo que mejora la confiabilidad y la trazabilidad de la ejecución de tareas simultáneas.

