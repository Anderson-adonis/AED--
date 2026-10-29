import anderson.ArbolAVL;
import johan.ArbolABB;
import sam.GrafoBFS;
import java.util.List;

public final class VerificacionAlgoritmos {
    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
    public static void main(String[] args) {
        verificarABB();
        verificarAVL();
        verificarBFS();
        System.out.println("OK: ABB, rotaciones AVL, balance y rutas BFS verificados.");
    }
    private static void verificarABB() {
        ArbolABB.Arbol t = new ArbolABB.Arbol();
        for (int c : new int[]{50,30,80,20,40,70,90,60})
            comprobar(t.insertar(new ArbolABB.Producto(c,"P"+c,1,1)), "insertar ABB "+c);
        comprobar(!t.insertar(new ArbolABB.Producto(50,"duplicado",1,1)), "ABB rechaza duplicado");
        List<ArbolABB.Producto> orden=t.inorden();
        for(int i=1;i<orden.size();i++)comprobar(orden.get(i-1).codigo()<orden.get(i).codigo(),"inorden ABB");
        comprobar(t.buscar(60).camino().size()==4,"camino de búsqueda ABB");
        comprobar(t.eliminar(20),"eliminar hoja");
        comprobar(t.eliminar(70),"eliminar nodo con un hijo");
        comprobar(t.eliminar(50),"eliminar nodo con dos hijos");
        comprobar(t.inorden().size()==5,"tamaño después de eliminaciones");
    }
    private static void verificarAVL() {
        int[][] casos={{30,20,10},{10,20,30},{30,10,20},{10,30,20}};
        String[] nombres={"derecha (LL)","izquierda (RR)","izquierda-derecha (LR)","derecha-izquierda (RL)"};
        for(int i=0;i<casos.length;i++){
            ArbolAVL.Arbol t=new ArbolAVL.Arbol();
            for(int c:casos[i])t.insertar(new ArbolAVL.Paciente(c,"N"+c,20,"demo"));
            comprobar(t.raiz().paciente().codigo()==20,"raíz rotación "+nombres[i]);
            comprobar(t.ultimaRotacion().contains(nombres[i]),"tipo rotación "+nombres[i]);
            comprobar(t.estaBalanceado(),"balance "+nombres[i]);
        }
        ArbolAVL.Arbol t=new ArbolAVL.Arbol();
        for(int i=1;i<=200;i++){t.insertar(new ArbolAVL.Paciente(i,"N"+i,20,"demo"));comprobar(t.estaBalanceado(),"AVL después de alta "+i);}
        for(int i=2;i<=200;i+=2){comprobar(t.eliminar(i),"baja AVL "+i);comprobar(t.estaBalanceado(),"AVL después de baja "+i);}
    }
    private static void verificarBFS() {
        GrafoBFS.Grafo g=new GrafoBFS.Grafo();
        GrafoBFS.Buscador b=g.iniciar("Entrada principal","Servicios estudiantiles");
        int guard=0;while(!b.completo()&&guard++<20)b.siguiente();
        comprobar(b.encontrado(),"BFS encuentra destino");
        comprobar(b.ruta().equals(List.of("Entrada principal","Auditorio","Servicios estudiantiles")),"ruta BFS mínima");
        GrafoBFS.Buscador sinRuta=g.iniciar("Entrada principal","Anexo cerrado");
        guard=0;while(!sinRuta.completo()&&guard++<20)sinRuta.siguiente();
        comprobar(sinRuta.completo()&&!sinRuta.encontrado()&&sinRuta.ruta().isEmpty(),"BFS informa destino aislado");
    }
}