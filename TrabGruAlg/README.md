# TrabGruAlg — tres aplicaciones Java Swing independientes

Proyecto único con tres programas separados, uno por integrante. No hay ventana general de navegación.

## Estructura

- src/main/java/johan/ArbolABB.java y ArbolABB.form
- src/main/java/anderson/ArbolAVL.java y ArbolAVL.form
- src/main/java/sam/GrafoBFS.java y GrafoBFS.form

Cada clase principal tiene su propio método main. Los algoritmos están implementados manualmente y cada formulario contiene sus controles visuales, enlazados por nombre de campo a su clase Java.

## Ejecutar en IntelliJ IDEA

1. Abre TrabGruAlg como proyecto Maven.
2. Espera a que IntelliJ termine de indexar e importar el JDK 21 y el proyecto.
3. Abre uno de los archivos Java y ejecuta el icono verde junto a main:
   - johan.ArbolABB
   - anderson.ArbolAVL
   - sam.GrafoBFS
4. Abre el archivo .form del mismo paquete y selecciona la pestaña Design para editar controles.

El compilador de IntelliJ IDEA procesa los formularios enlazados al construir el proyecto. No ejecutes estas ventanas con javac/java directamente: una compilación Java aislada no procesa los archivos .form ni inicializa los componentes enlazados. Las clases se pueden compilar en Maven, pero para ejecutar la interfaz se debe construir desde IntelliJ con Swing UI Designer habilitado.

## Configuración de Swing UI Designer

Conserva habilitado el plugin **Swing UI Designer** (incluido en el plugin de Java de esta instalación de IntelliJ IDEA). En Settings > Editor > GUI Designer, deja activo **Generate GUI into: Binary class files** y usa **Build > Build Project** o el botón Run de cada main. Los archivos .form están enlazados mediante bind-to-class y los nombres de binding coinciden con los campos privados de las clases.

## Respaldo

El código y los entregables anteriores están en .respaldo_proyecto_anterior_20261025. La carpeta activa src/main/java contiene solo los tres paquetes nuevos.