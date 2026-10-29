package johan;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/** Aplicación independiente de Johan: gestión de productos con árbol binario de búsqueda. */
public class ArbolABB {
    private JPanel rootPanel;
    private JLabel titleLabel, codeLabel, nameLabel, priceLabel, quantityLabel;
    private JTextField codeField;
    private JTextField nameField;
    private JTextField priceField;
    private JTextField quantityField;
    private JButton insertButton;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton resetButton;
    private JButton inorderButton;
    private JButton preorderButton;
    private JButton postorderButton;
    private JButton exampleButton;
    private JPanel drawingPanel;
    private JTextArea resultArea;

    private final Arbol arbol = new Arbol();
    private final Set<Integer> resaltados = new HashSet<>();
    private TreeCanvas canvas;

    public ArbolABB() {
        canvas = new TreeCanvas();
        drawingPanel.setLayout(new BorderLayout());
        drawingPanel.add(canvas, BorderLayout.CENTER);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        Border labelGap = BorderFactory.createEmptyBorder(0, 2, 0, 10);
        codeLabel.setBorder(labelGap);
        nameLabel.setBorder(labelGap);
        priceLabel.setBorder(labelGap);
        quantityLabel.setBorder(labelGap);

        insertButton.addActionListener(e -> registrar());
        searchButton.addActionListener(e -> buscar());
        deleteButton.addActionListener(e -> eliminar());
        resetButton.addActionListener(e -> reiniciar());
        inorderButton.addActionListener(e -> mostrarRecorrido("INORDEN", arbol.inorden()));
        preorderButton.addActionListener(e -> mostrarRecorrido("PREORDEN", arbol.preorden()));
        postorderButton.addActionListener(e -> mostrarRecorrido("POSTORDEN", arbol.postorden()));
        exampleButton.addActionListener(e -> cargarEjemplo());
        resultArea.setText("Ingresa productos o pulsa Cargar ejemplo.");
    }

    public JPanel getRootPanel() { return rootPanel; }

    private void registrar() {
        try {
            int codigo = entero(codeField.getText(), "código");
            int cantidad = entero(quantityField.getText(), "cantidad");
            double precio = Double.parseDouble(priceField.getText().trim());
            String nombre = nameField.getText().trim();
            if (codigo < 0 || cantidad < 0 || !Double.isFinite(precio) || precio < 0 || nombre.isEmpty()) {
                throw new IllegalArgumentException("Completa los campos y usa valores no negativos.");
            }
            if (!arbol.insertar(new Producto(codigo, nombre, precio, cantidad))) {
                throw new IllegalArgumentException("El código ya existe.");
            }
            resaltados.clear();
            actualizar();
            resultArea.setText("Producto " + nombre + " (" + codigo + ") agregado al ABB.");
            nameField.setText("");
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: código, precio y cantidad deben tener un formato numérico válido.");
        } catch (IllegalArgumentException ex) {
            resultArea.setText("Error: " + ex.getMessage());
        }
    }

    private void buscar() {
        try {
            int codigo = entero(codeField.getText(), "código");
            ResultadoBusqueda resultado = arbol.buscar(codigo);
            resaltados.clear();
            for (Producto p : resultado.camino) resaltados.add(p.codigo());
            canvas.repaint();
            if (resultado.encontrado == null) {
                resultArea.setText("No se encontró el producto " + codigo + ".\nCamino comparado: " + codigos(resultado.camino));
            } else {
                Producto p = resultado.encontrado;
                resultArea.setText("Producto encontrado: " + p.nombre() + "\nPrecio: S/ " + String.format(Locale.US, "%.2f", p.precio())
                        + " · Cantidad: " + p.cantidad() + "\nCamino comparado: " + codigos(resultado.camino));
            }
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: ingresa un código entero para buscar.");
        }
    }

    private void eliminar() {
        try {
            int codigo = entero(codeField.getText(), "código");
            if (!arbol.eliminar(codigo)) {
                resultArea.setText("No existe un producto con código " + codigo + ".");
                return;
            }
            resaltados.clear();
            actualizar();
            resultArea.setText("Producto " + codigo + " eliminado. El ABB mantiene el orden.");
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: ingresa el código entero que deseas eliminar.");
        }
    }

    private void mostrarRecorrido(String nombre, List<Producto> productos) {
        if (productos.isEmpty()) {
            resultArea.setText("El ABB está vacío.");
            return;
        }
        resultArea.setText(nombre + " (códigos): " + codigos(productos));
    }

    private void cargarEjemplo() {
        arbol.vaciar();
        int[] codigos = {50, 30, 80, 20, 40, 70, 90, 60};
        String[] nombres = {"Laptop", "Teclado", "Monitor", "Mouse", "Webcam", "Impresora", "Audífonos", "Memoria USB"};
        for (int i = 0; i < codigos.length; i++) arbol.insertar(new Producto(codigos[i], nombres[i], 89.90 + i * 37, 4 + i));
        resaltados.clear();
        actualizar();
        resultArea.setText("Ejemplo cargado. Busca 60 y elimina 50 para ver el caso de dos hijos.");
    }

    private void reiniciar() {
        arbol.vaciar();
        resaltados.clear();
        codeField.setText("");
        nameField.setText("");
        priceField.setText("");
        quantityField.setText("");
        actualizar();
        resultArea.setText("Árbol reiniciado.");
    }

    private void actualizar() {
        canvas.revalidate();
        canvas.repaint();
    }

    private static int entero(String texto, String campo) {
        try { return Integer.parseInt(texto.trim()); }
        catch (NumberFormatException ex) { throw new NumberFormatException(campo); }
    }

    private static String codigos(List<Producto> datos) {
        List<String> resultado = new ArrayList<>();
        for (Producto p : datos) resultado.add(String.valueOf(p.codigo()));
        return resultado.isEmpty() ? "(ninguna comparación)" : String.join(" → ", resultado);
    }

    public record Producto(int codigo, String nombre, double precio, int cantidad) {}
    public record ResultadoBusqueda(List<Producto> camino, Producto encontrado) {}

    /** ABB manual que usa el código como clave. */
    public static final class Arbol {
        private Nodo raiz;
        public Nodo raiz() { return raiz; }
        public boolean vacio() { return raiz == null; }
        public void vaciar() { raiz = null; }
        public int tamano() { return inorden().size(); }

        public boolean insertar(Producto producto) {
            if (raiz == null) { raiz = new Nodo(producto); return true; }
            Nodo actual = raiz;
            while (true) {
                if (producto.codigo() == actual.producto.codigo()) return false;
                if (producto.codigo() < actual.producto.codigo()) {
                    if (actual.izquierda == null) { actual.izquierda = new Nodo(producto); return true; }
                    actual = actual.izquierda;
                } else {
                    if (actual.derecha == null) { actual.derecha = new Nodo(producto); return true; }
                    actual = actual.derecha;
                }
            }
        }

        public ResultadoBusqueda buscar(int codigo) {
            List<Producto> camino = new ArrayList<>();
            Nodo actual = raiz;
            while (actual != null) {
                camino.add(actual.producto);
                if (codigo == actual.producto.codigo()) return new ResultadoBusqueda(List.copyOf(camino), actual.producto);
                actual = codigo < actual.producto.codigo() ? actual.izquierda : actual.derecha;
            }
            return new ResultadoBusqueda(List.copyOf(camino), null);
        }

        public boolean eliminar(int codigo) {
            if (buscar(codigo).encontrado() == null) return false;
            raiz = eliminar(raiz, codigo);
            return true;
        }

        private Nodo eliminar(Nodo nodo, int codigo) {
            if (codigo < nodo.producto.codigo()) nodo.izquierda = eliminar(nodo.izquierda, codigo);
            else if (codigo > nodo.producto.codigo()) nodo.derecha = eliminar(nodo.derecha, codigo);
            else {
                if (nodo.izquierda == null) return nodo.derecha;
                if (nodo.derecha == null) return nodo.izquierda;
                Nodo sucesor = nodo.derecha;
                while (sucesor.izquierda != null) sucesor = sucesor.izquierda;
                nodo.producto = sucesor.producto;
                nodo.derecha = eliminar(nodo.derecha, sucesor.producto.codigo());
            }
            return nodo;
        }

        public List<Producto> inorden() { List<Producto> r = new ArrayList<>(); inorden(raiz, r); return List.copyOf(r); }
        public List<Producto> preorden() { List<Producto> r = new ArrayList<>(); preorden(raiz, r); return List.copyOf(r); }
        public List<Producto> postorden() { List<Producto> r = new ArrayList<>(); postorden(raiz, r); return List.copyOf(r); }
        private void inorden(Nodo n, List<Producto> r) { if (n != null) { inorden(n.izquierda,r); r.add(n.producto); inorden(n.derecha,r); } }
        private void preorden(Nodo n, List<Producto> r) { if (n != null) { r.add(n.producto); preorden(n.izquierda,r); preorden(n.derecha,r); } }
        private void postorden(Nodo n, List<Producto> r) { if (n != null) { postorden(n.izquierda,r); postorden(n.derecha,r); r.add(n.producto); } }
    }

    public static final class Nodo {
        private Producto producto;
        private Nodo izquierda, derecha;
        private Nodo(Producto producto) { this.producto = producto; }
        public Producto producto() { return producto; }
        public Nodo izquierda() { return izquierda; }
        public Nodo derecha() { return derecha; }
    }

    private final class TreeCanvas extends JPanel {
        TreeCanvas() { setBackground(Color.WHITE); setPreferredSize(new Dimension(900, 400)); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (arbol.vacio()) {
                g.setColor(Color.GRAY);
                g.drawString("El árbol aparecerá aquí.", 22, 28);
                g.dispose();
                return;
            }
            int count = arbol.tamano();
            Map<Nodo, Point2D.Double> posiciones = new IdentityHashMap<>();
            colocar(arbol.raiz(), 0, new int[]{0}, Math.max(100, getWidth()), posiciones, count);
            g.setStroke(new BasicStroke(2f));
            dibujarAristas(g, arbol.raiz(), posiciones);
            for (Map.Entry<Nodo, Point2D.Double> entry : posiciones.entrySet()) {
                Nodo n = entry.getKey(); Point2D.Double p = entry.getValue();
                int d = 36;
                g.setColor(resaltados.contains(n.producto.codigo()) ? new Color(255, 194, 91) : new Color(219, 235, 252));
                g.fillOval((int)p.x-d/2, (int)p.y-d/2, d, d);
                g.setColor(new Color(75, 112, 153)); g.drawOval((int)p.x-d/2, (int)p.y-d/2, d, d);
                String t = String.valueOf(n.producto.codigo());
                FontMetrics fm = g.getFontMetrics(); g.setColor(new Color(35,48,68));
                g.drawString(t, (int)p.x-fm.stringWidth(t)/2, (int)p.y+fm.getAscent()/2-2);
            }
            g.dispose();
        }
        private void colocar(Nodo n, int depth, int[] index, int width, Map<Nodo,Point2D.Double> positions, int count) {
            if (n == null) return;
            colocar(n.izquierda, depth+1, index, width, positions, count);
            double x = 40 + (count <= 1 ? 0 : index[0] * (width-80.0)/(count-1));
            index[0]++;
            positions.put(n, new Point2D.Double(x, 38 + depth*72));
            colocar(n.derecha, depth+1, index, width, positions, count);
        }
        private void dibujarAristas(Graphics2D g, Nodo n, Map<Nodo,Point2D.Double> p) {
            if (n == null) return;
            Point2D.Double a = p.get(n);
            g.setColor(new Color(170,185,203));
            if (n.izquierda != null) { Point2D.Double b=p.get(n.izquierda);g.drawLine((int)a.x,(int)a.y+15,(int)b.x,(int)b.y-15); }
            if (n.derecha != null) { Point2D.Double b=p.get(n.derecha);g.drawLine((int)a.x,(int)a.y+15,(int)b.x,(int)b.y-15); }
            dibujarAristas(g,n.izquierda,p);dibujarAristas(g,n.derecha,p);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            ArbolABB app = new ArbolABB();
            JFrame frame = new JFrame("Johan · Árbol Binario de Búsqueda");
            frame.setContentPane(app.rootPanel);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(900, 680));
            frame.setSize(1100, 780);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
