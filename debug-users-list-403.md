# [OPEN] Debug Session: users-list-403

## Contexto
- Sintoma: despues de iniciar sesion con Laura, la vista intenta cargar la lista de usuarios y recibe `403` en `GET /api/users`.
- Esperado: Laura tiene rol ADMIN y debe ver la lista de usuarios excepto sus propios datos.

## Hipotesis
1. El frontend esta enviando `Authorization: Bearer null` y no esta enviando la cookie `jwt`.
2. Laura autentica, pero su authority no llega como `ROLE_ADMIN` al endpoint `/api/users`.
3. La regla de seguridad no coincide con la ruta real del endpoint.
4. El endpoint falla al resolver el usuario autenticado o al serializar roles/usuarios.

## Plan
1. Revisar `SecurityConfig`, `UserController` y `TecnicoService`.
2. Reproducir `GET /api/users` por HTTP con y sin cookie.
3. Instrumentar el endpoint o filtro si hace falta para confirmar principal y authorities.
4. Aplicar un arreglo minimo y verificar desde backend y frontend.
