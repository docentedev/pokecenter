# ✅ Checklist de Implementación - Próximos Pasos

## 1️⃣ Compilar el Proyecto

Después de que Git descargue todos los cambios, ejecutar:

```bash
cd /Users/claudio.viajando/src/duocuc/example/pokecenter
./gradlew clean build
```

**Qué valida:**
- ✅ Todas las dependencias se descargan correctamente
- ✅ El código Java compila sin errores
- ✅ Las migraciones están correctas

---

## 2️⃣ Ejecutar Migraciones (Automático)

Asegúrate que PostgreSQL esté corriendo:

```bash
docker-compose up -d
```

Luego iniciar la aplicación:

```bash
./gradlew bootRun
```

**Qué sucede automáticamente:**
- ✅ Flyway detecta V1__Create_user_table.sql
- ✅ Crea la tabla `users` si no existe
- ✅ Puedes verificar con: `SELECT * FROM users;`

---

## 3️⃣ Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto (opcional, ya tiene defaults):

```bash
JWT_SECRET=tu-secret-key-super-largo-y-seguro
JWT_EXPIRATION=3600000
DB_URL=jdbc:postgresql://localhost:5434/pokedex
DB_USERNAME=pokedex
DB_PASSWORD=pokedex
```

**O configurar en el sistema:**

```bash
export JWT_SECRET="tu-secret-key-super-largo-y-seguro"
export JWT_EXPIRATION="3600000"
```

---

## 4️⃣ Testear Endpoints

### Opción A: Usar REST Client en VS Code

1. Instalar extensión: "REST Client" (humao.rest-client)
2. Abrir archivo: `/pokecenter/api.http`
3. Hacer clic en "Send Request" en cada endpoint
4. Copiar el token de register/login
5. Pegarlo en el header `Authorization: Bearer <token>`

### Opción B: Usar cURL desde terminal

```bash
# Registrar
curl -X POST http://localhost:3334/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Login
curl -X POST http://localhost:3334/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Validar token (reemplazar con tu token)
curl -X GET http://localhost:3334/api/auth/validate \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Acceder a endpoint protegido
curl -X GET http://localhost:3334/api/pokecenter/pokemons \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 5️⃣ Verificar Estructura de Carpetas

Confirmar que se crearon todas las carpetas:

```
src/main/java/com/noentiendo/pokecenter/
├── config/              ✅ NUEVO
│   └── SecurityConfig.java
├── controller/          (existente + new)
│   ├── AuthController.java
│   └── PokeCenterController.java
├── dto/                 (existente + new)
│   ├── ApiResponse.java
│   ├── AuthResponse.java
│   ├── ErrorDetails.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── PokemonDto.java
├── entity/              ✅ NUEVO
│   └── User.java
├── exception/           ✅ NUEVO
│   ├── ApiException.java
│   ├── AuthenticationException.java
│   ├── AuthorizationException.java
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ValidationException.java
├── filter/              ✅ NUEVO
│   └── JwtAuthenticationFilter.java
├── repository/          ✅ NUEVO
│   └── UserRepository.java
├── service/             ✅ NUEVO
│   └── AuthService.java
└── util/                ✅ NUEVO
    └── JwtUtil.java
```

---

## 6️⃣ Validar Base de Datos

Conectarse a PostgreSQL y verificar:

```sql
-- Verificar que la tabla se creó
\dt public.users

-- Ver estructura
\d public.users

-- Insertar un registro de prueba (opcional)
INSERT INTO users (username, password_hash) 
VALUES ('manual_test', '$2a$10$...');

-- Ver usuarios registrados
SELECT id, username, created_at FROM users;
```

---

## 7️⃣ Pruebas de Integración

Ejecutar las pruebas (si existen):

```bash
./gradlew test
```

---

## 8️⃣ Documentación de Referencia

📄 **Ver estos archivos en el proyecto:**

1. **PLAN_IMPLEMENTACION_AUTH.md** - Arquitectura completa y decisiones de diseño
2. **RESUMEN_IMPLEMENTACION.md** - Listado de todos los archivos creados
3. **GUIA_TESTING.md** - Ejemplos de requests HTTP y casos de error
4. **api.http** - Endpoints predefinidos para testing

---

## ⚠️ Problemas Comunes

### "Cannot find symbol: JwtUtil"
- **Causa**: build.gradle no se ha actualizado
- **Solución**: `./gradlew clean && ./gradlew build`

### "Liquibase migration failed" o "Flyway migration error"
- **Causa**: Tabla users ya existe
- **Solución**: Eliminar la BD y recrearla, o verificar la migración

### "401 Unauthorized" en todos los endpoints
- **Causa**: JWT token inválido o expirado
- **Solución**: Obtener un nuevo token con login

### "Invalid JWT token"
- **Causa**: JWT_SECRET cambió o token tiene formato incorrecto
- **Solución**: Asegurar que JWT_SECRET sea el mismo en app y env

### "Cannot resolve symbol: com.noentiendo.pokecenter.exception.ApiException"
- **Causa**: IntelliJ no ha refrescado el índice
- **Solución**: `File → Invalidate Caches → Restart`

---

## 🎯 Success Criteria

Proyecto completamente funcional cuando:

- ✅ `./gradlew build` compila sin errores
- ✅ `./gradlew bootRun` inicia la aplicación
- ✅ POST /api/auth/register devuelve JWT token
- ✅ POST /api/auth/login devuelve JWT token
- ✅ GET /api/auth/validate retorna true/false
- ✅ GET /api/pokecenter/pokemons funciona con token válido
- ✅ GET /api/pokecenter/pokemons retorna 401 sin token
- ✅ Tabla `users` existe en PostgreSQL con datos
- ✅ Las excepciones se manejan de forma centralizada

---

## 🚀 Deployment (Futuro)

Antes de deployar a producción:

1. Cambiar `JWT_SECRET` a algo muy seguro
2. Cambiar `JWT_EXPIRATION` según políticas de seguridad
3. Activar HTTPS/SSL
4. Implementar rate limiting en /api/auth/login
5. Implementar 2FA si es necesario
6. Auditar los logs de seguridad
7. Hacer test de penetración

---

## 📞 Soporte

Si algo no funciona:

1. Revisar los logs: `logging.level.root=DEBUG` está activado
2. Verificar que PostgreSQL está corriendo
3. Confirmar que las variables de entorno están configuradas
4. Revisar GUIA_TESTING.md para ejemplos
5. Revisar PLAN_IMPLEMENTACION_AUTH.md para entender la arquitectura
