# Plan de Implementación - Sistema de Autenticación y Autorización

## Objetivo General
Implementar un sistema de autenticación y autorización completo en el microservicio PokéCenter utilizando Spring Security, JWT y base de datos PostgreSQL.

---

## Cambios a Realizar

### 1. **Actualización de Dependencias en `build.gradle`**
Se agregarán las siguientes librerías:
- `spring-boot-starter-security`: Para seguridad y autenticación
- `jjwt`: Para generación y validación de JWT (JSON Web Tokens)
- `jackson-databind`: Para serialización de objetos JWT

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

---

### 2. **Capa de Excepciones Centralizada** 
**Ubicación**: `src/main/java/com/noentiendo/pokecenter/exception/`

Se crearán las siguientes clases:

#### a. `ApiException.java` - Excepción base personalizada
```
- Clase abstracta que extiende RuntimeException
- Propiedades: code, message, status HTTP
- Método: getStatusCode()
```

#### b. `AuthenticationException.java`
- Para errores de autenticación (credenciales inválidas, token expirado)
- Status HTTP: 401 Unauthorized

#### c. `AuthorizationException.java`
- Para errores de autorización (acceso denegado)
- Status HTTP: 403 Forbidden

#### d. `ResourceNotFoundException.java`
- Para cuando un recurso no existe
- Status HTTP: 404 Not Found

#### e. `ValidationException.java`
- Para errores de validación
- Status HTTP: 400 Bad Request

#### f. `GlobalExceptionHandler.java`
- RestControllerAdvice que centraliza el manejo de excepciones
- Retorna ApiResponse estandardizado con error details
- Captura también excepciones generales

---

### 3. **DTOs y Response Standardizados**
**Ubicación**: `src/main/java/com/noentiendo/pokecenter/dto/`

#### a. `ApiResponse<T>` - Envoltorio genérico
```json
{
  "success": true/false,
  "data": <T>,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message"
  }
}
```

#### b. `LoginRequest` - Request para login
```json
{
  "username": "string",
  "password": "string"
}
```

#### c. `RegisterRequest` - Request para registro
```json
{
  "username": "string",
  "password": "string"
}
```

#### d. `AuthResponse` - Response de autenticación
```json
{
  "token": "jwt_token_string",
  "expiresIn": 3600,
  "username": "string"
}
```

---

### 4. **Entidad User y Migración Flyway**
**Ubicación**: 
- Entity: `src/main/java/com/noentiendo/pokecenter/entity/User.java`
- Migración: `src/main/resources/db/migration/V1__Create_user_table.sql`

#### a. Entidad `User.java`
```
- id: UUID (Primary Key)
- username: String (UNIQUE, NOT NULL)
- passwordHash: String (NOT NULL)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### b. Tabla `users` en PostgreSQL
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

---

### 5. **Repositorio User**
**Ubicación**: `src/main/java/com/noentiendo/pokecenter/repository/UserRepository.java`

```
- Extiende JpaRepository<User, UUID>
- Método: Optional<User> findByUsername(String username)
```

---

### 6. **Utilidad de JWT**
**Ubicación**: `src/main/java/com/noentiendo/pokecenter/util/JwtUtil.java`

```
Métodos:
- generateToken(String username): String
- validateToken(String token): boolean
- getUsername(String token): String
- getExpiration(): long (en milisegundos)
```

**Configuración**:
- Secret Key: ${JWT_SECRET:your-secret-key} (variable de entorno)
- Expiración: ${JWT_EXPIRATION:3600000} (1 hora por defecto)

---

### 7. **AuthService**
**Ubicación**: `src/main/java/com/noentiendo/pokecenter/service/AuthService.java`

```
Métodos:
- register(RegisterRequest): AuthResponse
  * Valida que username no exista
  * Hash la contraseña usando BCryptPasswordEncoder
  * Guarda el usuario en BD
  * Genera JWT y lo retorna
  
- login(LoginRequest): AuthResponse
  * Busca usuario por username
  * Valida la contraseña contra el hash
  * Si es válida, genera JWT y lo retorna
  * Si es inválida, lanza AuthenticationException
  
- validateToken(String token): boolean
  * Utiliza JwtUtil para validar token
```

---

### 8. **AuthController**
**Ubicación**: `src/main/java/com/noentiendo/pokecenter/controller/AuthController.java`

```
Base Path: /api/auth

Endpoints:

1. POST /auth/register
   - Request: RegisterRequest
   - Response: ResponseEntity<ApiResponse<AuthResponse>>
   - Llama a AuthService.register()
   - Status: 201 Created si es exitoso

2. POST /auth/login
   - Request: LoginRequest
   - Response: ResponseEntity<ApiResponse<AuthResponse>>
   - Llama a AuthService.login()
   - Status: 200 OK si es exitoso

3. GET /auth/validate
   - Header: Authorization: Bearer <jwt_token>
   - Response: ResponseEntity<ApiResponse<Boolean>>
   - Llama a AuthService.validateToken()
   - Status: 200 OK si token es válido
   - Status: 401 Unauthorized si token es inválido
```

---

### 9. **Configuración Spring Security**
**Ubicación**: `src/main/java/com/noentiendo/pokecenter/config/SecurityConfig.java`

```
- BCryptPasswordEncoder bean para hashear contraseñas
- SecurityFilterChain configurado con:
  * CSRF deshabilitado (API REST)
  * Session policy: STATELESS (sin sesiones, solo JWT)
  * Permitir endpoints públicos: /api/auth/login, /api/auth/register
  * Requerir autenticación para otros endpoints
  * Agregar filtro JWT personalizado
```

---

### 10. **Filtro JWT Personalizado**
**Ubicación**: `src/main/java/com/noentiendo/pokecenter/filter/JwtAuthenticationFilter.java`

```
- OncePerRequestFilter que:
  * Extrae el token del header Authorization (Bearer schema)
  * Valida el token usando JwtUtil
  * Crea SecurityContext con el usuario autenticado
  * Si falla, propaga excepción AuthenticationException
```

---

### 11. **Actualización de application.properties**
Se agregarán las siguientes propiedades:

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:your-secret-key-change-in-production}
jwt.expiration=${JWT_EXPIRATION:3600000}

# Security
spring.security.user.name=admin
spring.security.user.password=admin
```

---

### 12. **Actualización de Controladores Existentes**
**Cambio en**: `PokeCenterController.java`

Todos los endpoints ahora retornarán `ResponseEntity<ApiResponse<T>>` en lugar de `Object`:

```java
@GetMapping("/pokemons")
public ResponseEntity<ApiResponse<List<PokemonDto>>> getAllPokemons(...) {
    // ... lógica
    return ResponseEntity.ok(new ApiResponse<>(true, data, null));
}
```

---

## Flujo de Autenticación

```
1. Usuario llama POST /api/auth/register con username y password
   ↓
2. AuthService valida y guarda usuario con password hasheada
   ↓
3. Se genera JWT y se retorna en AuthResponse
   ↓
4. Usuario usa el JWT en header: Authorization: Bearer <token>
   ↓
5. JwtAuthenticationFilter valida el token en cada request
   ↓
6. Si es válido: request procede
   Si es inválido: retorna 401 Unauthorized
   ↓
7. GET /api/auth/validate verifica si un token es válido
```

---

## Estructura de Carpetas Final

```
src/main/java/com/noentiendo/pokecenter/
├── controller/
│   ├── AuthController.java (NUEVO)
│   └── PokeCenterController.java (MODIFICADO)
├── service/
│   ├── AuthService.java (NUEVO)
│   └── ... (servicios existentes)
├── entity/
│   ├── User.java (NUEVO)
│   └── ... (entidades existentes)
├── repository/
│   ├── UserRepository.java (NUEVO)
│   └── ... (repositorios existentes)
├── dto/
│   ├── ApiResponse.java (NUEVO)
│   ├── LoginRequest.java (NUEVO)
│   ├── RegisterRequest.java (NUEVO)
│   ├── AuthResponse.java (NUEVO)
│   └── PokemonDto.java (existente)
├── exception/
│   ├── ApiException.java (NUEVO)
│   ├── AuthenticationException.java (NUEVO)
│   ├── AuthorizationException.java (NUEVO)
│   ├── ResourceNotFoundException.java (NUEVO)
│   ├── ValidationException.java (NUEVO)
│   └── GlobalExceptionHandler.java (NUEVO)
├── util/
│   └── JwtUtil.java (NUEVO)
├── filter/
│   └── JwtAuthenticationFilter.java (NUEVO)
├── config/
│   └── SecurityConfig.java (NUEVO)
├── client/
│   ├── PokedexClient.java
│   └── PokedexClientImpl.java
└── PokecenterApplication.java

src/main/resources/
└── db/migration/
    └── V1__Create_user_table.sql (NUEVO)
```

---

## Variables de Entorno Necesarias

```bash
# Autenticación JWT
JWT_SECRET=tu-secret-key-muy-largo-y-seguro
JWT_EXPIRATION=3600000

# Base de datos (ya existen)
DB_URL=jdbc:postgresql://localhost:5434/pokedex
DB_USERNAME=pokedex
DB_PASSWORD=pokedex
```

---

## Próximos Pasos

1. ✅ Actualizar `build.gradle` con dependencias
2. ✅ Crear capa de excepciones
3. ✅ Crear DTOs
4. ✅ Crear entidad User y migraciones
5. ✅ Crear AuthService
6. ✅ Crear AuthController
7. ✅ Crear configuración de Security
8. ✅ Crear filtro JWT
9. ✅ Actualizar controladores existentes
10. ✅ Crear archivo de propiedades de seguridad
11. ✅ Testear endpoints con REST Client
