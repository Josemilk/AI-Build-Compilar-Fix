# AGENT.md — Instrucciones Obligatorias para Generación de Código

## Regla Principal (NO NEGOCIABLE)
Nunca generes código simulado, mock, fake, ficticio, de ejemplo o de demostración cuando el usuario pida integración con Firebase o Firestore.
Toda conexión a base de datos DEBE ser real, usando el SDK oficial de Firebase y las APIs reales de Firestore.

## Prohibiciones Absolutas
- NO uses `firebase-mock`, `firestore-mock`, `mockFirebase`, `jest.mock`, `vi.mock` ni ninguna librería de mocking.
- NO crees objetos falsos tipo `const db = { collection: () => ({ ... }) }`.
- NO escribas comentarios del estilo `// Aquí iría la conexión real` o `// TODO: conectar a Firebase`.
- NO generes funciones que devuelvan datos hardcodeados o promesas resolviendo datos inventados.
- NO uses `localStorage`, `sessionStorage` ni arrays en memoria como sustituto de Firestore.
- NO inventes nombres de colecciones, documentos o campos que el usuario no haya especificado.
- NO generes código que "simule" éxito de autenticación o escritura sin llamar realmente a Firebase.

## Obligaciones Técnicas
1. Usa siempre el SDK oficial:
   - Web: `firebase` (modular v9+) → `import { initializeApp } from 'firebase/app'`
   - `import { getFirestore, collection, doc, getDoc, getDocs, setDoc, addDoc, updateDoc, deleteDoc, onSnapshot, query, where, orderBy, limit } from 'firebase/firestore'`
   - Auth: `import { getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword, onAuthStateChanged, signOut } from 'firebase/auth'`
   - Android (Kotlin): SDK oficial de Firebase (`com.google.firebase:firebase-firestore`, `firebase-auth`, etc.) con `FirebaseFirestore.getInstance()`, `FirebaseAuth.getInstance()`, `google-services.json` y llamadas reales a Firestore/Auth.

2. La configuración de Firebase DEBE estar en un archivo real del proyecto:
   - Web: `src/firebase/config.js` o `src/lib/firebase.js` o `firebaseConfig.js`
   - Android: `google-services.json` / configuración oficial de Firebase.
   - Debe contener la configuración real (apiKey, authDomain, projectId, etc.) o inyección segura vía variables de entorno / Secrets.
   - Nunca dejes valores placeholder tipo `"YOUR_API_KEY"`. Si el usuario no los proporciona, pide los datos reales antes de generar el código.

3. Inicialización real obligatoria:
```js
import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "...",
  authDomain: "...",
  projectId: "...",
  storageBucket: "...",
  messagingSenderId: "...",
  appId: "..."
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
export const auth = getAuth(app);
```

4. Todas las operaciones de lectura/escritura deben usar las funciones reales del SDK:
   - Lectura: `getDoc`, `getDocs`, `onSnapshot` / `firestore.collection(...).get()` / `.addSnapshotListener`
   - Escritura: `setDoc`, `addDoc`, `updateDoc` / `.set()`, `.add()`, `.update()`
   - Eliminación: `deleteDoc` / `.delete()`
   - Consultas: `query`, `where`, `orderBy`, `limit` / `.whereEqualTo(...)`, `.orderBy(...)`, `.limit(...)`

5. Manejo de errores real:
   - Siempre captura errores de Firebase (`FirebaseError` / `FirebaseFirestoreException` / `FirebaseAuthException`)
   - Muestra mensajes reales de error (permission-denied, not-found, etc.)
   - No ocultes fallos con try/catch vacíos o datos de respaldo inventados.

6. Estructura de archivos obligatoria cuando se genere una app:
```
src/
  firebase/
    config.js          ← configuración e inicialización real
    auth.js            ← funciones de autenticación reales
    firestore.js       ← funciones de CRUD reales (o por dominio)
  ...
```

7. Si el usuario pide "crear una aplicación con Firebase":
   - Primero solicita (o usa) los datos reales de configuración del proyecto Firebase.
   - Genera el archivo de configuración completo.
   - Genera las funciones de servicio que llaman directamente a Firestore.
   - Nunca generes una versión "demo" o "sin backend".

## Comportamiento ante ambigüedad
Si el usuario no proporciona las credenciales de Firebase:
1. Pregunta explícitamente por el objeto `firebaseConfig` o el archivo de configuración.
2. No generes código hasta tener los valores reales (o al menos una estructura clara de dónde se van a colocar).
3. Si el usuario insiste en "genera el código igual", genera el código con variables de entorno (`import.meta.env.VITE_FIREBASE_API_KEY` o BuildConfig en Android) y deja claro que debe rellenar la configuración.

## Verificación final antes de responder
Antes de entregar cualquier código que involucre Firebase/Firestore, verifica mentalmente:
- ¿Estoy usando el SDK oficial?
- ¿Hay alguna función que devuelva datos inventados?
- ¿Existe algún mock o simulación?
- ¿La conexión se inicializa realmente?

Si alguna respuesta es "sí" a mock/simulación → REESCRIBE el código hasta que sea 100% real.
