package Logic;

import java.util.ArrayList;
import java.util.Date;
import javax.swing.JOptionPane;

public class Bloque {
	public String hash;
	public String hashAnterior; 
	public String merkleRoot; //Contiene el hash de todas las transacciones de los bloques en la Blockchain
	public ArrayList<Transaccion> transacciones = new ArrayList<>(); //our data will be a simple message.
	public long timeStamp; //milisegundos desde 1/1/1970 para firma hash
	public int varAux; //Var contador bloques
	
	//Bloque Constructor.  
	public Bloque(String hashAnterior ) {
		this.hashAnterior = hashAnterior;
		this.timeStamp = new Date().getTime();
		this.hash = calcularHash(); 
	}
	//calcular nuevo hash basado en contenido
	public String calcularHash() {
		String hashCalculado = Cadena.aplicarSha256( 
				hashAnterior +
				Long.toString(timeStamp) +
				Integer.toString(varAux) + 
				merkleRoot
				);
		return hashCalculado;
	}
	//Incrementa valor varAux hasta que el hash deseado se satisfasca
	public void minarBloque(int dificultad) {
		merkleRoot = Cadena.obtenerMerkleRoot(transacciones);
		String objetivo = Cadena.obtenerDificultadString(dificultad); //Crea un Stringcon dificultad * "0" 
		while(!hash.substring( 0, dificultad).equals(objetivo)) {
			varAux ++;
			hash = calcularHash();
		}
		JOptionPane.showMessageDialog(null, "Bloque minado!", " " ,JOptionPane.INFORMATION_MESSAGE);
	}
	//Verifica la transaccion y la añade las transacciones al bloque
	public boolean añadirTransaccion(Transaccion transaccion) {
		if(transaccion == null) return false;		
		if((!"0".equals(hashAnterior))) {
			if((transaccion.procesoTransaccion() != true)) {
				JOptionPane.showMessageDialog(null, "Transaccion incorrecta", " " ,JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		transacciones.add(transaccion);
		JOptionPane.showMessageDialog(null, "Transaccion añadida al blockchain", " " ,JOptionPane.INFORMATION_MESSAGE);
		return true;
	}
}