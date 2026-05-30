# Video Demo — Panini FIFA 2026 Support Tickets

**Link:** _(pendiente — subir a Google Drive o YouTube y pegar el enlace aquí)_

> El video debe tener acceso para: **rachel.bolivar.morales@una.cr**

---

## Guión del video (5–10 minutos)

---

### Parte 1 — Introducción y contexto (1 min)

**Lo que decís:**

> "Buenas, este es el handoff técnico de la prueba de concepto desarrollada para Panini,
> para la gestión de tickets de soporte del álbum FIFA 2026.
> La app centraliza incidencias de proveedores, distribución e inventario,
> reemplazando el flujo actual de correos y hojas de cálculo.
> Voy a explicar la arquitectura, cómo funciona la comunicación basada en eventos,
> los Feature Flags, y cómo un ingeniero nuevo podría continuar este proyecto."

---

### Parte 2 — Demo funcional de la app (2 min)

**Mostrás en el emulador o dispositivo:**

1. **Pantalla de Login**
   - Iniciás sesión con `admin` / `admin123`
   - Explicás: *"La autenticación es simulada. En producción se conectaría al endpoint `POST /auth/login` definido en el contrato YAML."*

2. **Lista de tickets**
   - Mostrás los 6 tickets pre-cargados con datos realistas de proveedores
   - Señalás los chips de prioridad y estado
   - Explicás: *"Los tickets están ordenados por prioridad — CRITICAL aparece primero. Esto es automático y reactivo, no manual."*

3. **Detalle de un ticket**
   - Abrís el ticket TKT-001
   - Mostrás la información completa: proveedor, categoría, descripción
   - Cambiás el estado a `IN_PROGRESS` usando el botón "Update Status"
   - Explicás: *"Al confirmar, el cambio se propaga automáticamente al listado sin necesidad de recargar la pantalla. Eso lo maneja el EventBus."*

4. **Creación de ticket**
   - Volvés al listado, tocás el botón "+"
   - Llenás el formulario con datos realistas
   - Enviás y mostrás cómo el nuevo ticket aparece inmediatamente en el listado

---

### Parte 3 — Comunicación basada en eventos (2 min)

**Abrís en el IDE los archivos:**
- `core/events/TicketEvent.kt`
- `core/events/TicketEventBus.kt`
- `ui/screen/ticketlist/TicketListViewModel.kt`

**Lo que explicás:**

> "El problema que esto resuelve es simple: cuando creás un ticket desde `CreateTicketScreen`,
> ¿cómo se entera `TicketListScreen` sin que tenga que recargar manualmente?
> La solución es un EventBus basado en `SharedFlow` de Kotlin Coroutines."

**Mostrás `TicketEventBus.kt`:**
> "Este objeto singleton expone un `SharedFlow`. Cualquier ViewModel puede publicar eventos
> con `publish()`, y cualquier ViewModel suscrito los recibe automáticamente.
> Usé `extraBufferCapacity = 16` con `DROP_OLDEST` para que nunca bloquee la coroutine que emite."

**Mostrás `TicketEvent.kt`:**
> "Los eventos son un `sealed class`. Hay tres tipos:
> `TicketCreated` cuando se crea un ticket,
> `PriorityUpdated` cuando cambia la prioridad,
> y `StatusUpdated` cuando cambia el estado.
> Usar sealed class garantiza que el `when` en el ViewModel sea exhaustivo — si alguien agrega un evento nuevo, el compilador obliga a manejarlo."

**Mostrás `TicketListViewModel.kt`, función `observeEvents()`:**
> "El `TicketListViewModel` se suscribe al EventBus en su `init{}`.
> Cuando llega un `TicketCreated`, agrega el ticket a la lista y la reordena por prioridad.
> Cuando llega un `PriorityUpdated`, actualiza el ticket en memoria y vuelve a ordenar.
> Todo esto ocurre sin reiniciar la pantalla — el `StateFlow` notifica al Composable automáticamente."

**Flujo completo en una frase:**
> "Usuario crea ticket → `CreateTicketViewModel` llama al repositorio → si hay éxito, publica al EventBus → `TicketListViewModel` recibe el evento → actualiza su `StateFlow` → la UI recompone."

---

### Parte 4 — Feature Flags (1.5 min)

**Abrís en el IDE:**
- `core/flags/FeatureFlags.kt`
- `ui/screen/ticketlist/TicketListScreen.kt` (línea del FAB)
- `ui/screen/ticketdetail/TicketDetailScreen.kt` (botón de prioridad)
- `ui/screen/createticket/CreateTicketViewModel.kt` (guard al inicio de `createTicket`)

**Lo que explicás:**

> "Los Feature Flags permiten activar o desactivar funcionalidades sin tocar múltiples partes del sistema.
> Implementé cuatro flags como propiedades `var` en un objeto singleton."

**Demostrás en tiempo real:**
1. Abrís `FeatureFlags.kt` y cambiás `isTicketCreationEnabled = false`
2. Ejecutás la app → el botón "+" ya no aparece en el listado
3. Explicás: *"Esto está controlado en `TicketListScreen` con un simple `if (FeatureFlags.isTicketCreationEnabled)` alrededor del FAB. No hay lógica compleja."*
4. Cambiás `isPriorityUpdateEnabled = false`
5. Mostrás que el botón "Update Priority" desaparece en la pantalla de detalle
6. Explicás: *"El flag también protege el ViewModel — aunque alguien llame a `updatePriority()` directamente, el ViewModel verifica el flag antes de ejecutar."*

> "¿Por qué un `object` simple y no una librería de feature flags?
> Porque esto es un PoC de corto plazo con equipo pequeño.
> La complejidad debe estar justificada. Cuando el proyecto crezca,
> estas cuatro líneas se reemplazan con lecturas de Firebase Remote Config
> sin tocar ningún otro archivo."

---

### Parte 5 — Arquitectura y networking layer (2 min)

**Mostrás la estructura de paquetes en el IDE:**

> "La arquitectura sigue MVVM con capas bien definidas.
> La regla es simple: cada capa solo conoce la que tiene debajo."

```
UI (Screens)       → solo consumen ViewModels
ViewModels         → solo llaman al Repository
Repository         → solo habla con la API service
Retrofit           → interceptado por MockTicketInterceptor
```

**Abrís `data/remote/NetworkModule.kt`:**
> "El `NetworkModule` configura Retrofit con la URL base del backend real.
> La única diferencia entre el PoC y producción es este interceptor.
> Para conectar el backend real, se remueve `MockTicketInterceptor()` de aquí y listo —
> el resto del código no cambia."

**Abrís `data/mock/MockTicketInterceptor.kt`:**
> "El interceptor captura todas las llamadas HTTP antes de que salgan a la red
> y devuelve respuestas JSON realistas. Los datos representan escenarios reales:
> faltantes de inventario, paquetes dañados, envíos perdidos, errores de impresión.
> No son datos genéricos — son consistentes con el contexto del sistema."

**Abrís `data/dto/Dtos.kt`:**
> "Los DTOs separan el modelo de red del modelo de dominio.
> Si el backend cambia un nombre de campo, solo se actualiza el DTO y la función `toDomain()`.
> Las pantallas nunca ven la estructura del JSON."

**Mostrás `core/state/UiState.kt`:**
> "Cada pantalla maneja tres estados: `Loading`, `Success` y `Error`.
> El ViewModel expone un `StateFlow<UiState<T>>` y la pantalla simplemente
> hace `when(state)` para decidir qué renderizar. Sin variables booleanas separadas."

---

### Parte 6 — Cómo continuar el proyecto (30 seg)

> "Para un ingeniero que tome este proyecto:
>
> 1. **Backend real:** quitar el interceptor, actualizar la URL en `NetworkModule`
> 2. **Inyección de dependencias:** agregar Hilt — un `@HiltViewModel` por ViewModel, un `@Singleton` en el Repository
> 3. **Feature flags remotos:** reemplazar los `var` de `FeatureFlags` con lecturas de Firebase Remote Config
> 4. **Paginación:** el `TicketApiService` ya está preparado para agregar `@Query("page")` sin cambiar el ViewModel
>
> La arquitectura fue diseñada deliberadamente para que estas evoluciones
> sean cambios de configuración, no refactorizaciones."

---

## Checklist antes de grabar

- [ ] App corriendo en emulador o dispositivo físico
- [ ] IDE abierto con los archivos clave visibles
- [ ] Micrófono funcionando correctamente
- [ ] Pantalla del emulador visible y legible
- [ ] Feature Flags en estado `true` al inicio (estado por defecto)
- [ ] Video subido con acceso a `rachel.bolivar.morales@una.cr`
