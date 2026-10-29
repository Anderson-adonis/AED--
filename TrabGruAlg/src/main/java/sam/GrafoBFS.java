package sam;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/** Aplicación independiente de Sam: búsqueda de rutas de un campus con BFS. */
public class GrafoBFS {
    private JPanel rootPanel;
    private JLabel titleLabel, originLabel, destinationLabel;
    private JComboBox<String> originCombo;
    private JComboBox<String> destinationCombo;
    private JButton runButton;
    private JButton stepButton;
    private JButton startButton;
    private JButton resetButton;
    private JPanel drawingPanel;
    private JLabel queueLabel;
    private JTextArea resultArea;
    private JLabel infoLabel;

    private final Grafo grafo = new Grafo();
    private Buscador busqueda;
    private MapCanvas canvas;

    public GrafoBFS() {
        for (String v : grafo.vertices()) {
            originCombo.addItem(v);
            destinationCombo.addItem(v);
        }
        originCombo.setSelectedItem("Entrada principal");
        destinationCombo.setSelectedItem("Servicios estudiantiles");
        canvas = new MapCanvas();
        drawingPanel.setLayout(new BorderLayout());
        drawingPanel.add(canvas, BorderLayout.CENTER);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        queueLabel.setText("Cola: —");
        startButton.addActionListener(e -> iniciarDemostracion());
        stepButton.addActionListener(e -> siguientePaso());
        runButton.addActionListener(e -> buscarCompleto());
        resetButton.addActionListener(e -> reiniciar());
        resultArea.setText("Selecciona origen y destino, luego ejecuta BFS.");
    }

    public JPanel getRootPanel() { return rootPanel; }

    private void iniciarDemostracion() {
        String origen = (String) originCombo.getSelectedItem();
        String destino = (String) destinationCombo.getSelectedItem();
        busqueda = grafo.iniciar(origen, destino);
        queueLabel.setText("Cola: " + busqueda.cola());
        resultArea.setText(busqueda.mensaje() + "\nPulsa Siguiente paso para procesar un vértice.");
        canvas.repaint();
        if (busqueda.completo()) finalizar();
    }

    private void siguientePaso() {
        if (busqueda == null) { resultArea.setText("Primero pulsa Iniciar demostración."); return; }
        if (busqueda.completo()) { finalizar(); return; }
        busqueda.siguiente();
        queueLabel.setText("Cola FIFO: " + busqueda.cola());
        resultArea.append("\n\n" + busqueda.mensaje());
        canvas.repaint();
        if (busqueda.completo()) finalizar();
    }

    private void buscarCompleto() {
        iniciarDemostracion();
        int guard = 0;
        while (busqueda != null && !busqueda.completo() && guard++ <= grafo.vertices().size()) {
            busqueda.siguiente();
        }
        queueLabel.setText("Cola FIFO: " + busqueda.cola());
        canvas.repaint();
        finalizar();
    }

    private void finalizar() {
        if (busqueda == null) return;
        if (busqueda.encontrado()) {
            List<String> ruta = busqueda.ruta();
            resultArea.append("\n\nRuta con menos conexiones (" + (ruta.size() - 1) + "):\n" + String.join(" → ", ruta));
            infoLabel.setText("Ruta mínima: " + (ruta.size() - 1) + " conexiones (aristas).");
        } else if (busqueda.completo()) {
            resultArea.append("\n\nNo existe una ruta entre el origen y el destino.");
            infoLabel.setText("No se encontró una ruta.");
        }
    }

    private void reiniciar() {
        busqueda = null;
        queueLabel.setText("Cola: —");
        resultArea.setText("Búsqueda reiniciada. El grafo del campus permanece cargado.");
        infoLabel.setText("BFS minimiza el número de conexiones, no la distancia física.");
        canvas.repaint();
    }

    /** Lista de adyacencia manual; el anexo cerrado queda desconectado para mostrar ausencia de ruta. */
    public static final class Grafo {
        private final LinkedHashMap<String, List<String>> adyacencia = new LinkedHashMap<>();
        public Grafo() {
            String[] lugares = {"Entrada principal","Biblioteca","Laboratorio","Cafetería","Auditorio",
                    "Pabellón A","Pabellón B","Secretaría","Servicios estudiantiles","Anexo cerrado"};
            for (String l : lugares) adyacencia.put(l, new ArrayList<>());
            conectar("Entrada principal","Biblioteca");
            conectar("Entrada principal","Auditorio");
            conectar("Biblioteca","Laboratorio");
            conectar("Biblioteca","Pabellón A");
            conectar("Laboratorio","Cafetería");
            conectar("Laboratorio","Pabellón B");
            conectar("Cafetería","Pabellón A");
            conectar("Cafetería","Secretaría");
            conectar("Auditorio","Pabellón A");
            conectar("Auditorio","Servicios estudiantiles");
            conectar("Pabellón A","Pabellón B");
            conectar("Pabellón A","Secretaría");
            conectar("Pabellón B","Servicios estudiantiles");
            conectar("Secretaría","Servicios estudiantiles");
        }
        public void conectar(String a, String b) {
            if (!adyacencia.containsKey(a) || !adyacencia.containsKey(b)) throw new IllegalArgumentException("Vértice desconocido.");
            if (!adyacencia.get(a).contains(b)) adyacencia.get(a).add(b);
            if (!adyacencia.get(b).contains(a)) adyacencia.get(b).add(a);
        }
        public Set<String> vertices() { return Collections.unmodifiableSet(adyacencia.keySet()); }
        public List<String> vecinos(String v) { return List.copyOf(adyacencia.getOrDefault(v, List.of())); }
        public Buscador iniciar(String origen, String destino) { return new Buscador(this, origen, destino); }
    }

    /** BFS incremental con cola FIFO, visitados y predecesores para reconstruir el camino. */
    public static final class Buscador {
        private final Grafo grafo;
        private final String origen, destino;
        private final ArrayDeque<String> cola = new ArrayDeque<>();
        private final LinkedHashSet<String> visitados = new LinkedHashSet<>();
        private final Map<String, String> anterior = new HashMap<>();
        private String actual, mensaje = "BFS listo.";
        private boolean completo, encontrado;

        private Buscador(Grafo grafo, String origen, String destino) {
            this.grafo = grafo; this.origen = origen; this.destino = destino;
            if (!grafo.vertices().contains(origen) || !grafo.vertices().contains(destino)) {
                completo = true; mensaje = "Selecciona ubicaciones válidas."; return;
            }
            cola.add(origen); visitados.add(origen);
            mensaje = "El origen se agrega a la cola: " + origen;
            if (origen.equals(destino)) { encontrado = true; completo = true; mensaje = "Origen y destino coinciden."; }
        }
        public boolean siguiente() {
            if (completo) return false;
            actual = cola.poll();
            if (actual == null) { completo = true; mensaje = "La cola se agotó; no existe una ruta."; return false; }
            StringBuilder log = new StringBuilder("Se desencola y visita: ").append(actual);
            for (String vecino : grafo.vecinos(actual)) {
                if (visitados.add(vecino)) {
                    anterior.put(vecino, actual);
                    cola.add(vecino);
                    log.append("\n  Descubre ").append(vecino).append(" y lo agrega a la cola.");
                    if (vecino.equals(destino)) { encontrado = true; completo = true; break; }
                }
            }
            if (!encontrado && cola.isEmpty()) {
                completo = true;
                log.append("\nLa cola se agotó; no existe una ruta.");
            }
            mensaje = log.toString();
            return true;
        }
        public List<String> ruta() {
            if (!encontrado) return List.of();
            LinkedList<String> camino = new LinkedList<>();
            for (String n = destino; n != null; n = anterior.get(n)) {
                camino.addFirst(n);
                if (n.equals(origen)) break;
            }
            return List.copyOf(camino);
        }
        public List<String> cola() { return List.copyOf(cola); }
        public Set<String> visitados() { return Collections.unmodifiableSet(visitados); }
        public String origen() { return origen; }
        public String destino() { return destino; }
        public String actual() { return actual; }
        public String mensaje() { return mensaje; }
        public boolean completo() { return completo; }
        public boolean encontrado() { return encontrado; }
    }

    private final class MapCanvas extends JPanel {
        private final Map<String, Point2D.Double> ubicaciones = new LinkedHashMap<>();
        MapCanvas() {
            setBackground(Color.WHITE); setPreferredSize(new Dimension(780, 520));
            ubicaciones.put("Entrada principal",new Point2D.Double(.10,.43));
            ubicaciones.put("Biblioteca",new Point2D.Double(.29,.20));
            ubicaciones.put("Laboratorio",new Point2D.Double(.51,.16));
            ubicaciones.put("Cafetería",new Point2D.Double(.72,.30));
            ubicaciones.put("Auditorio",new Point2D.Double(.28,.70));
            ubicaciones.put("Pabellón A",new Point2D.Double(.49,.48));
            ubicaciones.put("Pabellón B",new Point2D.Double(.73,.62));
            ubicaciones.put("Secretaría",new Point2D.Double(.89,.40));
            ubicaciones.put("Servicios estudiantiles",new Point2D.Double(.87,.77));
            ubicaciones.put("Anexo cerrado",new Point2D.Double(.51,.91));
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Map<String,Point> p=new LinkedHashMap<>();
            ubicaciones.forEach((k,v)->p.put(k,new Point((int)(v.x*getWidth()),(int)(v.y*getHeight()))));
            Set<String> aristas=new HashSet<>();
            g.setColor(new Color(190,202,218));g.setStroke(new BasicStroke(2));
            for(String a:grafo.vertices())for(String b:grafo.vecinos(a)){
                String key=a.compareTo(b)<0?a+"|"+b:b+"|"+a;
                if(aristas.add(key)){Point x=p.get(a),y=p.get(b);g.drawLine(x.x,x.y,y.x,y.y);}
            }
            Set<String> ruta=busqueda==null?Set.of():new HashSet<>(busqueda.ruta());
            if(!ruta.isEmpty()){
                g.setColor(new Color(38,154,108));g.setStroke(new BasicStroke(4));
                List<String> r=busqueda.ruta();for(int i=1;i<r.size();i++){Point a=p.get(r.get(i-1)),b=p.get(r.get(i));g.drawLine(a.x,a.y,b.x,b.y);}
            }
            for(String name:grafo.vertices()){
                Point q=p.get(name);Color fill=new Color(226,237,251);
                if(busqueda!=null&&busqueda.visitados().contains(name))fill=new Color(255,224,156);
                if(ruta.contains(name))fill=new Color(158,224,192);
                if(busqueda!=null&&name.equals(busqueda.origen()))fill=new Color(148,201,255);
                if(busqueda!=null&&name.equals(busqueda.destino()))fill=busqueda.encontrado()?new Color(158,224,192):new Color(255,177,177);
                if(busqueda!=null&&name.equals(busqueda.actual())&&!busqueda.completo())fill=new Color(255,190,91);
                dibujarLugar(g,q,name,fill);
            }
            g.dispose();
        }
        private void dibujarLugar(Graphics2D g,Point p,String nombre,Color fill){
            int w=154,h=42;g.setColor(fill);g.fillRoundRect(p.x-w/2,p.y-h/2,w,h,18,18);
            g.setColor(new Color(91,127,167));g.drawRoundRect(p.x-w/2,p.y-h/2,w,h,18,18);
            g.setColor(new Color(35,48,68));g.setFont(new Font("Segoe UI",Font.PLAIN,11));
            FontMetrics fm=g.getFontMetrics();String texto=nombre;
            while(texto.length()>1&&fm.stringWidth(texto)>w-16)texto=texto.substring(0,texto.length()-2)+"…";
            g.drawString(texto,p.x-fm.stringWidth(texto)/2,p.y+fm.getAscent()/2-2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            GrafoBFS app = new GrafoBFS();
            JFrame frame = new JFrame("Sam · Rutas universitarias con BFS");
            frame.setContentPane(app.rootPanel);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(980, 700));
            frame.setSize(1180, 820);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
