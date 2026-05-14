# Guía de Testing - Sistema de Autenticación

## 🧪 Testing Rápido de Endpoints

### Usando VS Code REST Client (api.http)

#### 1. Registrar un nuevo usuario

```http
POST http://localhost:3334/api/auth/register
Content-Type: application/json

{
  "username": "alice",
  "password": "securepass123"
}
```

**Respuesta esperada (201 Created):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 3600000,
    "username": "alice"
  }
}
```

---

#### 2. Hacer Login

```http
POST http://localhost:3334/api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "securepass123"
}
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 3600000,
    "username": "alice"
  }
```

---

#### 3. Validar Token

```http
GET http://localhost:3334/api/auth/validate
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "data": true
}
```

---

#### 4. Acceder a Endpoint Protegido (Pokémon)

```http
GET http://localhost:3334/api/pokecenter/pokemons
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Respuesta esperada (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Pikachu",
      "type": "Electric",
      "pokedexNumber": 25
    }
  ]
}
```

---

### Usando cURL

#### Registrar usuario
```bash
curl -X POST http://localhost:3334/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","password":"password123"}'
```

#### Login
```bash
curl -X POST http://localhost:3334/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","password":"password123"}'
```

#### Validar token (guardar el token en una variable)
```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
curl -X GET http://localhost:3334/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

#### Acceder endpoint protegido
```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
curl -X GET http://localhost:3334/api/pokecenter/pokemons \
  -H "Authorization: Bearer $TOKEN"
```

---

## ❌ Casos de Error

### 1. Username ya existe
```http
POST http://localhost:3334/api/auth/register
Content-Type: application/json

{
  "username": "alice",
  "password": "another_password"
}
```

**Respuesta (400 Bad Request):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Username already exists"
  }
}
```

---

### 2. Credenciales inválidas (password incorrecto)
```http
POST http://localhost:3334/api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "wrong_password"
}
```

**Respuesta (401 Unauthorized):**
```json
{
  "success": false,
  "error": {
    "code": "AUTHENTICATION_ERROR",
    "message": "Invalid username or password"
  }
}
```

---

### 3. Usuario no existe
```http
POST http://localhost:3334/api/auth/login
Content-Type: application/json

{
  "username": "nonexistent",
  "password": "password123"
}
```

**Respuesta (401 Unauthorized):**
```json
{
  "success": false,
  "error": {
    "code": "AUTHENTICATION_ERROR",
    "message": "Invalid username or password"
  }
}
```

---

### 4. Token inválido o expirado
```http
GET http://localhost:3334/api/auth/validate
Authorization: Bearer invalid_token_here
```

**Respuesta (200 OK):**
```json
{
  "success": true,
  "data": false
}
```

---

### 5. Sin Authorization header
```http
GET http://localhost:3334/api/pokecenter/pokemons
```

**Respuesta (401 Unauthorized):**
Spring Security rechazará el request sin token

---

### 6. Sin Bearer prefix
```http
GET http://localhost:3334/api/auth/validate
Authorization: eyJhbGciOiJIUzI1NiJ9...
```

**Respuesta (400 Bad Request):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Missing or invalid Authorization header"
  }
}
```

---

## 🔍 Checklist de Testing

- [ ] Registrar usuario nuevo
- [ ] Intentar registrar con username duplicado (debe fallar)
- [ ] Login con credenciales correctas
- [ ] Login con contraseña incorrecta (debe fallar)
- [ ] Validar token válido (debe retornar true)
- [ ] Validar token inválido (debe retornar false)
- [ ] Acceder a endpoint protegido con token válido
- [ ] Acceder a endpoint protegido sin token (debe fallar)
- [ ] Acceder a endpoint protegido con token inválido (debe fallar)
- [ ] Verificar que tokens expiran después de 1 hora

---

## 📊 Datos de Prueba

```
Username: alice
Password: securepass123

Username: bob
Password: password123

Username: charlie
Password: complex_Pass@123
```

---

## 🐛 Debugging

### Ver logs de Spring Security
En `application.properties` ya está configurado:
```properties
logging.level.org.springframework.security=DEBUG
```

### Ver SQL de Flyway
Las migraciones se ejecutarán automáticamente:
```properties
logging.level.org.flywaydb=DEBUG
```

### Verificar tabla users
```sql
SELECT * FROM users;
```

---

## 🔐 Seguridad en Testing

**⚠️ IMPORTANTE**: 
- Nunca uses secretos reales en test/development
- Cambiar `JWT_SECRET` antes de producción
- Las contraseñas de prueba son solo para development
- No commitear tokens en el repositorio
