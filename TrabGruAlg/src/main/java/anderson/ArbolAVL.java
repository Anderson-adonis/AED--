package anderson;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/** Aplicación independiente de Anderson: registro administrativo con árbol AVL. */
public class ArbolAVL {
    private JPanel rootPanel;
    private JLabel titleLabel, codeLabel, nameLabel, ageLabel, reasonLabel;
    private JTextField codeField;
    private JTextField nameField;
    private JTextField ageField;
    private JTextField reasonField;
    private JButton insertButton;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton resetButton;
    private JButton demoLLButton;
    private JButton demoRRButton;
    private JButton demoLRButton;
    private JButton demoRLButton;
    private JPanel drawingPanel;
    private JTextArea resultArea;

    private final Arbol arbol = new Arbol();
    private final Set<Integer> resaltados = new HashSet<>();
    private TreeCanvas canvas;

    public ArbolAVL() {
        canvas = new TreeCanvas();
        drawingPanel.setLayout(new BorderLayout());
        drawingPanel.add(canvas, BorderLayout.CENTER);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        insertButton.addActionListener(e -> registrar());
        searchButton.addActionListener(e -> buscar());
        deleteButton.addActionListener(e -> eliminar());
        resetButton.addActionListener(e -> reiniciar());
        demoLLButton.addActionListener(e -> demostrar("LL", 30, 20, 10));
        demoRRButton.addActionListener(e -> demostrar("RR", 10, 20, 30));
        demoLRButton.addActionListener(e -> demostrar("LR", 30, 10, 20));
        demoRLButton.addActionListener(e -> demostrar("RL", 10, 30, 20));
        resultArea.setText("Registros administrativos. Selecciona una demostración LL, RR, LR o RL.");
    }

    public JPanel getRootPanel() { return rootPanel; }

    private void registrar() {
        try {
            Paciente p = leerPaciente();
            if (!arbol.insertar(p)) throw new IllegalArgumentException("El código ya existe.");
            resaltados.clear();
            actualizar();
            resultArea.setText("Paciente " + p.nombre() + " registrado.\n" + estadoRaiz());
            nameField.setText("");
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: código y edad deben ser números enteros.");
        } catch (IllegalArgumentException ex) {
            resultArea.setText("Error: " + ex.getMessage());
        }
    }

    private Paciente leerPaciente() {
        int codigo = Integer.parseInt(codeField.getText().trim());
        int edad = Integer.parseInt(ageField.getText().trim());
        String nombre = nameField.getText().trim(), motivo = reasonField.getText().trim();
        if (codigo < 0 || edad < 0 || edad > 120 || nombre.isEmpty() || motivo.isEmpty()) {
            throw new IllegalArgumentException("Completa todos los campos; la edad debe estar entre 0 y 120.");
        }
        return new Paciente(codigo, nombre, edad, motivo);
    }

    private void buscar() {
        try {
            int codigo = Integer.parseInt(codeField.getText().trim());
            ResultadoBusqueda r = arbol.buscar(codigo);
            resaltados.clear();
            for (Paciente p : r.camino()) resaltados.add(p.codigo());
            canvas.repaint();
            if (r.encontrado() == null) {
                resultArea.setText("No existe el código " + codigo + ".\nComparaciones: " + codigos(r.camino()));
            } else {
                Paciente p = r.encontrado();
                resultArea.setText("Paciente: " + p.nombre() + " · " + p.edad() + " años\nMotivo: " + p.motivo()
                        + "\nComparaciones: " + codigos(r.camino()));
            }
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: escribe el código entero que deseas buscar.");
        }
    }

    private void eliminar() {
        try {
            int codigo = Integer.parseInt(codeField.getText().trim());
            if (!arbol.eliminar(codigo)) { resultArea.setText("No existe el código " + codigo + "."); return; }
            resaltados.clear();
            actualizar();
            resultArea.setText("Registro " + codigo + " eliminado.\n" + arbol.ultimaRotacion() + "\n" + estadoRaiz());
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: escribe el código entero que deseas eliminar.");
        }
    }

    private void demostrar(String tipo, int a, int b, int c) {
        arbol.vaciar();
        resaltados.clear();
        Paciente p1 = ejemplo(a), p2 = ejemplo(b), p3 = ejemplo(c);
        arbol.insertar(p1);
        arbol.insertar(p2);
        String antes = codigos(arbol.inorden());
        arbol.insertar(p3);
        for (Paciente p : arbol.inorden()) resaltados.add(p.codigo());
        actualizar();
        resultArea.setText("Rotación " + tipo + "\nAntes de insertar " + c + ": " + antes
                + "\nInserción real: " + c + "\n" + arbol.ultimaRotacion()
                + "\nDespués: " + codigos(arbol.inorden()) + "\n" + estadoRaiz());
    }

    private Paciente ejemplo(int codigo) { return new Paciente(codigo, "Paciente " + codigo, 20 + codigo % 40, "Consulta administrativa"); }

    private void reiniciar() {
        arbol.vaciar(); resaltados.clear();
        codeField.setText(""); nameField.setText(""); ageField.setText(""); reasonField.setText("");
        actualizar(); resultArea.setText("Árbol AVL reiniciado.");
    }

    private String estadoRaiz() {
        if (arbol.vacio()) return "Árbol vacío.";
        return "Altura de raíz: " + arbol.raiz().altura() + " · FE de raíz: " + arbol.factor(arbol.raiz())
                + " · Balance válido: " + arbol.estaBalanceado();
    }

    private void actualizar() { canvas.revalidate(); canvas.repaint(); }

    private static String codigos(List<Paciente> datos) {
        List<String> r = new ArrayList<>();
        for (Paciente p : datos) r.add(String.valueOf(p.codigo()));
        return r.isEmpty() ? "(ninguna comparación)" : String.join(" → ", r);
    }

    public record Paciente(int codigo, String nombre, int edad, String motivo) {}
    public record ResultadoBusqueda(List<Paciente> camino, Paciente encontrado) {}

    /** AVL implementado manualmente: alturas, factores, rotaciones, búsqueda y borrado. */
    public static final class Arbol {
        private Nodo raiz;
        private String ultimaRotacion = "Sin rotación";
        public Nodo raiz() { return raiz; }
        public boolean vacio() { return raiz == null; }
        public String ultimaRotacion() { return ultimaRotacion; }
        public void vaciar() { raiz = null; ultimaRotacion = "Sin rotación"; }
        private int altura(Nodo n) { return n == null ? 0 : n.altura; }
        public int factor(Nodo n) { return n == null ? 0 : altura(n.izquierda) - altura(n.derecha); }
        private void actualizarAltura(Nodo n) { n.altura = 1 + Math.max(altura(n.izquierda), altura(n.derecha)); }

        public boolean insertar(Paciente p) {
            if (buscar(p.codigo()).encontrado() != null) return false;
            ultimaRotacion = "Sin rotación";
            raiz = insertar(raiz, p);
            return true;
        }
        private Nodo insertar(Nodo n, Paciente p) {
            if (n == null) return new Nodo(p);
            if (p.codigo() < n.paciente.codigo()) n.izquierda = insertar(n.izquierda, p);
            else n.derecha = insertar(n.derecha, p);
            actualizarAltura(n);
            return balancear(n);
        }
        public ResultadoBusqueda buscar(int codigo) {
            List<Paciente> camino = new ArrayList<>();
            Nodo n = raiz;
            while (n != null) {
                camino.add(n.paciente);
                if (codigo == n.paciente.codigo()) return new ResultadoBusqueda(List.copyOf(camino), n.paciente);
                n = codigo < n.paciente.codigo() ? n.izquierda : n.derecha;
            }
            return new ResultadoBusqueda(List.copyOf(camino), null);
        }
        public boolean eliminar(int codigo) {
            if (buscar(codigo).encontrado() == null) return false;
            ultimaRotacion = "Sin rotación";
            raiz = eliminar(raiz, codigo);
            return true;
        }
        private Nodo eliminar(Nodo n, int codigo) {
            if (n == null) return null;
            if (codigo < n.paciente.codigo()) n.izquierda = eliminar(n.izquierda, codigo);
            else if (codigo > n.paciente.codigo()) n.derecha = eliminar(n.derecha, codigo);
            else {
                if (n.izquierda == null) return n.derecha;
                if (n.derecha == null) return n.izquierda;
                Nodo sucesor = n.derecha;
                while (sucesor.izquierda != null) sucesor = sucesor.izquierda;
                n.paciente = sucesor.paciente;
                n.derecha = eliminar(n.derecha, sucesor.paciente.codigo());
            }
            actualizarAltura(n);
            return balancear(n);
        }
        private Nodo balancear(Nodo n) {
            int f = factor(n);
            if (f > 1) {
                if (factor(n.izquierda) < 0) {
                    ultimaRotacion = "Rotación doble izquierda-derecha (LR)";
                    n.izquierda = rotarIzquierda(n.izquierda);
                } else ultimaRotacion = "Rotación simple a la derecha (LL)";
                return rotarDerecha(n);
            }
            if (f < -1) {
                if (factor(n.derecha) > 0) {
                    ultimaRotacion = "Rotación doble derecha-izquierda (RL)";
                    n.derecha = rotarDerecha(n.derecha);
                } else ultimaRotacion = "Rotación simple a la izquierda (RR)";
                return rotarIzquierda(n);
            }
            return n;
        }
        private Nodo rotarDerecha(Nodo y) {
            Nodo x = y.izquierda, t = x.derecha;
            x.derecha = y; y.izquierda = t;
            actualizarAltura(y); actualizarAltura(x); return x;
        }
        private Nodo rotarIzquierda(Nodo x) {
            Nodo y = x.derecha, t = y.izquierda;
            y.izquierda = x; x.derecha = t;
            actualizarAltura(x); actualizarAltura(y); return y;
        }
        public List<Paciente> inorden() { List<Paciente> r = new ArrayList<>(); inorden(raiz,r); return List.copyOf(r); }
        private void inorden(Nodo n,List<Paciente> r){if(n!=null){inorden(n.izquierda,r);r.add(n.paciente);inorden(n.derecha,r);}}
        public boolean estaBalanceado() { return verificar(raiz) != -1; }
        private int verificar(Nodo n) {
            if(n==null)return 0;
            int iz=verificar(n.izquierda),de=verificar(n.derecha);
            if(iz<0||de<0||Math.abs(iz-de)>1||n.altura!=1+Math.max(iz,de))return -1;
            return 1+Math.max(iz,de);
        }
    }

    public static final class Nodo {
        private Paciente paciente;
        private Nodo izquierda, derecha;
        private int altura = 1;
        private Nodo(Paciente p) { paciente = p; }
        public Paciente paciente() { return paciente; }
        public Nodo izquierda() { return izquierda; }
        public Nodo derecha() { return derecha; }
        public int altura() { return altura; }
    }

    private final class TreeCanvas extends JPanel {
        TreeCanvas() { setBackground(Color.WHITE); setPreferredSize(new Dimension(950, 430)); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if(arbol.vacio()){g.setColor(Color.GRAY);g.drawString("Registra pacientes o pulsa una demostración de rotación.",20,28);g.dispose();return;}
            int count=arbol.inorden().size();Map<Nodo,Point2D.Double> p=new IdentityHashMap<>();
            int anchoNodo=Math.min(112,Math.max(70,(getWidth()-60)/count-16));
            colocar(arbol.raiz(),0,new int[]{0},Math.max(100,getWidth()),count,anchoNodo,p);aristas(g,arbol.raiz(),p);
            for(Map.Entry<Nodo,Point2D.Double> e:p.entrySet()){
                Nodo n=e.getKey();Point2D.Double q=e.getValue();int w=anchoNodo,h=54;
                g.setColor(resaltados.contains(n.paciente.codigo())?new Color(255,195,96):new Color(222,236,252));
                g.fillRoundRect((int)q.x-w/2,(int)q.y-h/2,w,h,14,14);g.setColor(new Color(75,112,153));g.drawRoundRect((int)q.x-w/2,(int)q.y-h/2,w,h,14,14);
                g.setColor(new Color(35,48,68));FontMetrics fm=g.getFontMetrics();String code=String.valueOf(n.paciente.codigo());
                g.drawString(code,(int)q.x-fm.stringWidth(code)/2,(int)q.y-4);
                String detail="h="+n.altura+" · FE="+arbol.factor(n);g.setFont(g.getFont().deriveFont(10f));fm=g.getFontMetrics();
                g.drawString(detail,(int)q.x-fm.stringWidth(detail)/2,(int)q.y+15);
            }
            g.dispose();
        }
        private void colocar(Nodo n,int depth,int[] ix,int width,int count,int anchoNodo,Map<Nodo,Point2D.Double> p){
            if(n==null)return;colocar(n.izquierda,depth+1,ix,width,count,anchoNodo,p);
            double margen=anchoNodo/2.0+12;
            double x=margen+(count<=1?0:ix[0]*(width-2*margen)/(count-1));ix[0]++;p.put(n,new Point2D.Double(x,45+depth*82));
            colocar(n.derecha,depth+1,ix,width,count,anchoNodo,p);
        }
        private void aristas(Graphics2D g,Nodo n,Map<Nodo,Point2D.Double> p){
            if(n==null)return;Point2D.Double a=p.get(n);g.setColor(new Color(170,185,203));g.setStroke(new BasicStroke(2));
            if(n.izquierda!=null){Point2D.Double b=p.get(n.izquierda);g.drawLine((int)a.x,(int)a.y+24,(int)b.x,(int)b.y-24);}
            if(n.derecha!=null){Point2D.Double b=p.get(n.derecha);g.drawLine((int)a.x,(int)a.y+24,(int)b.x,(int)b.y-24);}
            aristas(g,n.izquierda,p);aristas(g,n.derecha,p);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            ArbolAVL app = new ArbolAVL();
            JFrame frame = new JFrame("Anderson · Árbol AVL");
            frame.setContentPane(app.rootPanel);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(920, 690));
            frame.setSize(1120, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
