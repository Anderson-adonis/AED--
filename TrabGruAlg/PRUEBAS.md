# Guía de pruebas para la exposición

Cada programa se inicia por separado desde el método main indicado. Los botones de demostración cargan datos deterministas y reinician solo el árbol correspondiente.

## JOHAN — ABB

**Datos de entrada:** usa el botón Cargar ejemplo. Se insertan códigos 50, 30, 80, 20, 40, 70, 90 y 60.

**Pasos:**
1. Ejecuta johan.ArbolABB.
2. Pulsa Cargar ejemplo. Deben aparecer los ocho nodos en el dibujo.
3. Escribe 60 en Código y pulsa Buscar. Se debe resaltar el camino 50 → 80 → 70 → 60.
4. Pulsa Inorden, Preorden y Postorden. Inorden debe mostrar los códigos en orden ascendente: 20 → 30 → 40 → 50 → 60 → 70 → 80 → 90.
5. Escribe 20 y pulsa Eliminar para mostrar el borrado de hoja.
6. Para mostrar borrado con un hijo, carga el ejemplo otra vez, elimina 70 y observa que 60 ocupa su enlace.
7. Para mostrar borrado con dos hijos, carga el ejemplo otra vez y elimina 50. El sucesor inorden mantiene ordenado el árbol.
8. Pulsa Reiniciar árbol. El dibujo debe quedar vacío.

**Resultado esperado:** los duplicados se rechazan; búsqueda y eliminación actualizan el dibujo; un árbol vacío muestra un mensaje comprensible.

## ANDERSON — AVL

**Datos de entrada:** los botones de demostración ingresan secuencias de tres códigos y reconstruyen el árbol antes de cada caso.

| Botón | Secuencia insertada | Rotación esperada | Raíz final |
|---|---|---|---|
| Demo LL | 30, 20, 10 | Simple a la derecha | 20 |
| Demo RR | 10, 20, 30 | Simple a la izquierda | 20 |
| Demo LR | 30, 10, 20 | Doble izquierda-derecha | 20 |
| Demo RL | 10, 30, 20 | Doble derecha-izquierda | 20 |

**Pasos:**
1. Ejecuta anderson.ArbolAVL.
2. Pulsa cada botón Demo. El área de resultado debe indicar el estado antes de la tercera inserción y la rotación aplicada.
3. Comprueba en el dibujo la altura y el factor de equilibrio de cada nodo.
4. Registra otro paciente y prueba Buscar por código y Eliminar código.
5. Pulsa Reiniciar árbol. La visualización debe quedar vacía.

**Resultado esperado:** en los cuatro ejemplos la raíz queda en 20 y todos los factores quedan entre −1 y 1. La pantalla muestra que el registro es administrativo y no realiza diagnósticos clínicos.

## SAM — BFS

**Datos de entrada:** origen Entrada principal y destino Servicios estudiantiles. El campus tiene caminos alternativos y un vértice Anexo cerrado que está aislado.

**Pasos:**
1. Ejecuta sam.GrafoBFS.
2. Deja Entrada principal → Servicios estudiantiles y pulsa Iniciar demostración.
3. Pulsa Siguiente paso hasta terminar. La etiqueta Cola muestra la cola FIFO y el área de resultado añade vértices visitados.
4. El mapa debe resaltar la ruta Entrada principal → Auditorio → Servicios estudiantiles, con 2 conexiones.
5. Pulsa Buscar ruta para ejecutar BFS completo con otra pareja de ubicaciones.
6. Elige Anexo cerrado como destino y pulsa Buscar ruta. Debe indicarse que no hay ruta.
7. Pulsa Reiniciar búsqueda. Se limpia la búsqueda y el grafo permanece visible.

**Resultado esperado:** BFS devuelve una ruta con el menor número de aristas del grafo no ponderado; el resultado no representa necesariamente la menor distancia física.