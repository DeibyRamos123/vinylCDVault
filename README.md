# 💿 Vinyl & CD Vault

Aplicación móvil nativa para Android de una tienda virtual de discos de vinilo, CDs y cassettes. Permite que compradores exploren y adquieran música en formato físico, que vendedores publiquen y administren su inventario, y que un administrador gestione usuarios, productos y reportes.

Este repositorio corresponde al **desarrollo del frontend** (interfaces gráficas en XML) de la asignatura *Desarrollo de Aplicaciones Móviles*, Fundación Universitaria Compensar.

- **Autor:** Deiby Julian Ramos Nieto
- **Docente:** FLAVIO ALEXANDER NAVARRO CARMONA

> **Alcance de esta entrega:** solo interfaz y navegación. Los datos son de ejemplo (sin base de datos ni API) y el pago es simulado.

---

## Tecnologías

| Elemento | Detalle |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | XML (Views) con Material Design 3 |
| IDE | Android Studio |
| SDK mínimo | API 26 (Android 8.0) |
| Librerías | Material Components 1.12.0, RecyclerView 1.3.2, AndroidX Biometric 1.1.0 |
| Control de versiones | Git y GitHub |

---

## Cómo ejecutar el proyecto

1. Clona el repositorio:
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   ```
2. Ábrelo con **Android Studio** (File → Open) y espera a que termine el *Gradle Sync*.
3. Crea o selecciona un emulador (por ejemplo, un Pixel con imagen **Google APIs**) o conecta un celular.
4. Presiona **Run ▶**.

También puedes instalar directamente el APK incluido en la entrega.

### Configurar la huella en el emulador

Para probar la biometría:

1. En el emulador: *Settings → Security → Device unlock*. Crea un PIN y registra una huella.
2. Cuando pida tocar el sensor, usa en la barra lateral del emulador **… → Fingerprint → Touch the Sensor**.

---

## Usuarios de prueba

No hay backend: el rol se decide según el correo que escribas en el login (cualquier contraseña funciona).

| Rol | Correo de ejemplo | Entra a |
|---|---|---|
| Administrador | `admin@correo.com` | Panel de administración |
| Vendedor | `vendedor@correo.com` | Panel del vendedor |
| Comprador | cualquier otro, por ejemplo `cliente@correo.com` | Tienda del comprador |

El botón **Ingresar con huella** usa la misma lógica de roles, a partir del correo escrito.

---

## Funcionalidades implementadas

- Navegación por rol con *Bottom Navigation* y *Fragments*.
- Listas con `RecyclerView` y adaptadores reutilizables.
- Autenticación biométrica real con `BiometricPrompt`, en el login y como requisito antes de confirmar el pago.
- Validaciones de formularios con mensajes de error en los campos.
- Diálogos de confirmación para eliminar y para solicitar retracto.
- Estilos, colores, dimensiones y textos centralizados en `res/values`.

## Pendiente para fases posteriores

- Backend y API real, autenticación en servidor y base de datos (Room).
- Pasarela de pagos real (Wompi, PayU o Stripe).
- Captura de fotos con cámara y galería, y selección de dirección en mapa.
- Carga de imágenes de portada con Glide o Coil.
- Arquitectura MVVM al conectar la capa de datos.

---

## Estructura del proyecto

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/vinylcdvault/
│   ├── SplashActivity.kt
│   ├── LoginActivity.kt
│   ├── RegisterActivity.kt
│   ├── RecoverPasswordActivity.kt
│   ├── BuyerActivity.kt            # contenedor del comprador
│   ├── HomeFragment.kt
│   ├── CatalogFragment.kt
│   ├── CartFragment.kt
│   ├── ProfileFragment.kt
│   ├── ProductDetailActivity.kt
│   ├── CheckoutActivity.kt
│   ├── SellerActivity.kt           # contenedor del vendedor
│   ├── SellerProductsFragment.kt
│   ├── SellerOrdersFragment.kt
│   ├── SellerProfileFragment.kt
│   ├── ProductFormActivity.kt
│   ├── AdminActivity.kt            # contenedor del administrador
│   ├── AdminDashboardFragment.kt
│   ├── AdminUsersFragment.kt
│   ├── AdminProductsFragment.kt
│   ├── AdminReportsFragment.kt
│   ├── UserFormActivity.kt
│   ├── ListActivity.kt
│   ├── Product.kt / Order.kt / User.kt   # modelos y datos de ejemplo
│   ├── ProductAdapter.kt / SellerProductAdapter.kt
│   ├── OrderAdapter.kt / UserAdapter.kt
│   ├── BiometricHelper.kt
│   └── Dialogs.kt
└── res/
    ├── layout/     # activity_*, fragment_*, item_*
    ├── drawable/   # bg_* (fondos) e ic_* (íconos vectoriales)
    ├── menu/       # un menú de navegación por rol
    └── values/     # colors, strings, dimens, styles, themes
```

### Convención de nombres

| Prefijo | Uso |
|---|---|
| `activity_` | Pantallas completas |
| `fragment_` | Secciones dentro de la barra de navegación |
| `item_` | Fila o tarjeta de una lista |
| `bg_` | Drawables de fondo (formas, degradados) |
| `ic_` | Íconos vectoriales |
| `menu_*_nav` | Menú inferior de cada rol |

---

## Diseño

Se usa una paleta propia, distinta a la de Android por defecto:

| Color | Código | Uso |
|---|---|---|
| Azul marino | `#1D3557` | Color principal, botones y textos |
| Rojo | `#E63946` | Acento |
| Rojo oscuro | `#C1121F` | Precios y acciones destructivas |
| Crema verdoso | `#F1FAEE` | Fondo |
| Azul acero | `#457B9D` | Degradados y color secundario |

Los estilos reutilizables (`App.Text.Title`, `App.Chip`, `App.Button`, `App.Input`, entre otros) están en `res/values/styles.xml`, y las medidas en `dimens.xml`.

---
