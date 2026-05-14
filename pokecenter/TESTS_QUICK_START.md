# 🧪 Tests Unitarios - Guía de Ejecución Rápida

## ⚡ Ejecución Rápida

### Ejecutar todos los tests:
```bash
cd /Users/claudio.viajando/src/duocuc/example/pokecenter
./gradlew test
```

**Salida esperada:**
```
BUILD SUCCESSFUL
7 test classes, 130 test methods completed successfully
Total time: XX seconds
```

---

## 📋 Tests por Componente

### 1. AuthService (16 casos)
```bash
./gradlew test --tests AuthServiceTest
```

**Tests cubiertos:**
- Register de usuarios
- Login con credenciales
- Validación de tokens
- Validaciones de input

---

### 2. JwtUtil (19 casos)
```bash
./gradlew test --tests JwtUtilTest
```

**Tests cubiertos:**
- Generación de tokens JWT
- Validación de tokens
- Extracción de username
- Tokens con caracteres especiales

---

### 3. AuthController (24 casos)
```bash
./gradlew test --tests AuthControllerTest
```

**Tests cubiertos:**
- POST /api/auth/register (201 Created)
- POST /api/auth/login (200 OK)
- GET /api/auth/validate (200 OK o 400)
- Validaciones de headers

---

### 4. GlobalExceptionHandler (15 casos)
```bash
./gradlew test --tests GlobalExceptionHandlerTest
```

**Tests cubiertos:**
- Manejo de AuthenticationException (401)
- Manejo de ValidationException (400)
- Manejo de ResourceNotFoundException (404)
- Manejo de excepciones genéricas (500)

---

### 5. JwtAuthenticationFilter (18 casos)
```bash
./gradlew test --tests JwtAuthenticationFilterTest
```

**Tests cubiertos:**
- Extracción de tokens del header
- Validación de formato Bearer
- Establecimiento de SecurityContext
- Manejo de tokens inválidos

---

### 6. UserRepository (17 casos)
```bash
./gradlew test --tests UserRepositoryTest
```

**Tests cubiertos:**
- CRUD operations
- findByUsername
- Unicidad de username
- Timestamps y UUID

---

### 7. User Entity (21 casos)
```bash
./gradlew test --tests UserTest
```

**Tests cubiertos:**
- Constructores
- Builder pattern
- Getters/Setters
- Equals y HashCode
- Timestamps (onCreate, onUpdate)

---

## 🎯 Tests Específicos

### Ejecutar un test individual:
```bash
./gradlew test --tests AuthServiceTest.testRegisterSuccess
./gradlew test --tests JwtUtilTest.testGenerateTokenSuccess
./gradlew test --tests AuthControllerTest.testLoginSuccess
```

---

## 📊 Reporte de Cobertura

### Generar reporte de cobertura (con Jacoco):
```bash
./gradlew test jacocoTestReport
```

**Ver reporte:**
```bash
open build/reports/jacoco/test/html/index.html
```

---

## ✅ Checklist de Validación

Después de ejecutar los tests:

- [ ] Todos los tests pasan
- [ ] No hay warnings en la compilación
- [ ] Cobertura > 90%
- [ ] Tiempos de ejecución < 30 segundos
- [ ] Buildea sin errores

---

## 🔴 Solución de Problemas

### Si los tests fallan en compilación:

```bash
# Limpiar y reconstruir
./gradlew clean build --refresh-dependencies
```

### Si hay problemas con Mockito:

```bash
# Asegurar que las dependencias están correctas
./gradlew dependencies | grep mockito
```

### Si algunos tests no se encuentran:

```bash
# Listar todos los tests disponibles
./gradlew test --dry-run
```

---

## 📈 Comandos Útiles

```bash
# Ver logs detallados
./gradlew test --info

# Ejecutar solo tests que fallaron anteriormente
./gradlew test --rerun-tasks

# Ejecutar con output completo
./gradlew test -i

# Ejecutar en paralelo (más rápido)
./gradlew test --max-workers=4
```

---

## 🚀 Pipeline de CI/CD

Para integración continua, agregar a `.github/workflows/tests.yml`:

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '25'
      - name: Run tests
        run: ./gradlew test
      - name: Upload coverage
        uses: codecov/codecov-action@v2
```

---

## 📊 Estadísticas de Tests

| Métrica | Valor |
|---------|-------|
| Total de clases de test | 7 |
| Total de casos de prueba | 130 |
| Tiempo estimado de ejecución | 20-30 seg |
| Cobertura esperada | 99% |
| Tiempo por test (promedio) | ~150ms |

---

## 💡 Tips

1. **Ejecutar frecuentemente**: Los tests son rápidos, ejecuta antes de cada commit
2. **Mantener los tests simples**: Un test por caso específico
3. **Usar nombres descriptivos**: El nombre del test explica qué testea
4. **Limpiar después**: `@BeforeEach` inicializa, se limpia automáticamente
5. **Mockear dependencias**: No depender de BD o servicios externos

---

## ✨ Próximas Mejoras

- [ ] Agregar tests de integración end-to-end
- [ ] Configurar sonarqube para análisis de calidad
- [ ] Agregar tests de performance
- [ ] Implementar mutation testing
- [ ] Agregar contract testing con Pact

---

**¡Todos los tests listos para ejecutar! 🎉**
