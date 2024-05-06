import java.util.ArrayList;

public class ManejadorTiempos {

    // Diccionario de tiempos
    private static ArrayList<ArrayList<Long>> tiemposS;
    private static ArrayList<ArrayList<Long>> tiemposC;

    //  Constructor
    public ManejadorTiempos() {
        tiemposS = new ArrayList<ArrayList<Long>>();
        // Crear 4 listas de tiempos vacias
        for (int i = 0; i < 4; i++) {
            tiemposS.add(new ArrayList<Long>());
        }

        tiemposC = new ArrayList<ArrayList<Long>>();
        // Crear 4 listas de tiempos vacias
        for (int i = 0; i < 4; i++) {
            tiemposC.add(new ArrayList<Long>());
        }
    }


    public void addTServerSign(Long tiempo) {
        tiemposS.get(0).add(tiempo);
    } 
    public void addTServerGy(Long tiempo) {
        tiemposS.get(1).add(tiempo);
    } 
    public void addTServerCOD(Long tiempo) {
        tiemposS.get(2).add(tiempo);
    }
    public void addTServerAuth(Long tiempo) {
        tiemposS.get(3).add(tiempo);
    } 


    public void addTClientSign(Long tiempo) {
        tiemposC.get(0).add(tiempo);
    }
    public void addTClientGx(Long tiempo) {
        tiemposC.get(1).add(tiempo);
    }
    public void addTClientCOD(Long tiempo) {
        tiemposC.get(2).add(tiempo);
    }
    public void addTClientAuth(Long tiempo) {
        tiemposC.get(3).add(tiempo);
    }




    
}
