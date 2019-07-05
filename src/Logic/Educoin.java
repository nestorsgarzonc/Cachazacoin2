package Logic;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Educoin {
	public static Monedero monederoA;
	public static Monedero monederoB;
	public static Transaccion bloqueGenesis;
	public static int dificultad = 3;
	public static float transaccionMinima = 0.1f;
	public static ArrayList<Bloque> blockchain = new ArrayList<>();
	public static HashMap<String,TransaccionSaliente> UTXOs = new HashMap<>();

	public static void main(String[] args) {	
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Api criptografia como proveedor de seguridad
		//Creacion de monederos:
		monederoA = new Monedero();
		monederoB = new Monedero();		
		Monedero monederoCentral = new Monedero();
		//Creacion de bloque genesis que envia 100EC al monederoA
		bloqueGenesis = new Transaccion(monederoCentral.llavePublica, monederoA.llavePublica, 1000f, null);
		bloqueGenesis.generarFirma(monederoCentral.llavePrivada);	 //Firma manual de la transaccion
		bloqueGenesis.transaccionId = "0"; //ID de la transaccion manual asignar
		bloqueGenesis.salidas.add(new TransaccionSaliente(bloqueGenesis.destinatario, bloqueGenesis.valor, bloqueGenesis.transaccionId));
		UTXOs.put(bloqueGenesis.salidas.get(0).id, bloqueGenesis.salidas.get(0));
		//PRUEBA VENTANAS EMERGENTES
		JOptionPane.showMessageDialog(null, "Minando el bloque genesis", "Advertencia" ,JOptionPane.INFORMATION_MESSAGE);

		System.out.println("Minando el bloque genesis ");
		Bloque genesis = new Bloque("0");
		genesis.añadirTransaccion(bloqueGenesis);
		añadirBloque(genesis);
		
		Bloque block1 = new Bloque(genesis.hash);
		System.out.println("\nMonederoA saldo: " + monederoA.obtenerBalance());
		System.out.println("\nMonederoA envia 40 E.C MonederoB...");
		block1.añadirTransaccion(monederoA.enviarFondos(monederoB.llavePublica, 40f));
		añadirBloque(block1);
		System.out.println("\nMonederoA balance: " + monederoA.obtenerBalance());
		System.out.println("\nMonederoB balance: " + monederoB.obtenerBalance());
		
		Bloque block2 = new Bloque(block1.hash);
		System.out.println("\nMonederoA envia 1000 E.C a MonederoB");
		block2.añadirTransaccion(monederoA.enviarFondos(monederoB.llavePublica, 1000f));
		añadirBloque(block2);
		System.out.println("\nMonederoA balance: " + monederoA.obtenerBalance());
		System.out.println("\nMonederoB balance: " + monederoB.obtenerBalance());
		
		Bloque block3 = new Bloque(block2.hash);
		System.out.println("\nMonederoB envia 20 E.C para MonederoA...");
		block3.añadirTransaccion(monederoB.enviarFondos( monederoA.llavePublica, 20));
		System.out.println("\nMonederoA balance: " + monederoA.obtenerBalance());
		System.out.println("\nMonederoB balance: " + monederoB.obtenerBalance());
		
		Bloque block4 = new Bloque(block3.hash);
		System.out.println("\nMonederoB envia 20 para MonederoA...");
		block4.añadirTransaccion(monederoB.enviarFondos( monederoA.llavePublica, 20));
		System.out.println("\nMonederoA balance: " + monederoA.obtenerBalance());
		System.out.println("\nMonederoB balance: " + monederoB.obtenerBalance());
		
		cadenaEsValida();
	}
	
	public static Boolean cadenaEsValida() {
		Bloque bloqueActual; 
		Bloque bloqueAnterior;
		String hashObjetivo = new String(new char[dificultad]).replace('\0', '0');
		HashMap<String,TransaccionSaliente> UTXOsTemporal = new HashMap<>(); //lista temporal transacciones no gastadas
		UTXOsTemporal.put(bloqueGenesis.salidas.get(0).id, bloqueGenesis.salidas.get(0));
		for(int i=1; i < blockchain.size(); i++) {
			bloqueActual = blockchain.get(i);
			bloqueAnterior = blockchain.get(i-1);
			if(!bloqueActual.hash.equals(bloqueActual.calcularHash())){ //Comparar hash registrado y hash calculado
				JOptionPane.showMessageDialog(null, "Error, el hash no es igual", "Error" ,JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(!bloqueAnterior.hash.equals(bloqueActual.hashAnterior)){
				JOptionPane.showMessageDialog(null, "Error, el hash anterior no es igual", "Error" ,JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(!bloqueActual.hash.substring( 0, dificultad).equals(hashObjetivo)) {
				JOptionPane.showMessageDialog(null, "Error, este bloque no puede ser minado", "Error" ,JOptionPane.ERROR_MESSAGE);
				return false;
			}
			TransaccionSaliente salidaTemporal;
			for(int t=0; t <bloqueActual.transacciones.size(); t++) {
				Transaccion transaccionActual = bloqueActual.transacciones.get(t);
				if(!transaccionActual.verificarFirma()) {
					JOptionPane.showMessageDialog(null, "Firma en Transaccion(" +t+ ") is invalida", "Error" ,JOptionPane.ERROR_MESSAGE);
					return false; 
				}
				if(transaccionActual.obtenerValorEntrada() != transaccionActual.obtenerValorSalida()) {
					JOptionPane.showMessageDialog(null, "Las entradas no son iguales para las salidas en la Transaccion(" +t+ ")"
					, "Error" ,JOptionPane.ERROR_MESSAGE);
					return false; 
				}
				for(TransaccionEntrada entrada: transaccionActual.entradas) {	
					salidaTemporal = UTXOsTemporal.get(entrada.idTransaccionSalida);
					if(salidaTemporal == null) {
						JOptionPane.showMessageDialog(null, "Hace falta referencia de entrada en transaccion("+t+")"
						, "Error" ,JOptionPane.ERROR_MESSAGE);
						return false;
					}
					if(entrada.UTXO.valor != salidaTemporal.valor){
						JOptionPane.showMessageDialog(null, "El valor de la transaccion("+t+") es invalido"
						, "Error" ,JOptionPane.ERROR_MESSAGE);
						return false;
					}
					UTXOsTemporal.remove(entrada.idTransaccionSalida);
				}
				for(TransaccionSaliente salida: transaccionActual.salidas){
					UTXOsTemporal.put(salida.id, salida);
				}
				if(transaccionActual.salidas.get(0).destinatario != transaccionActual.destinatario){
					JOptionPane.showMessageDialog(null, "Transaccion("+t+") destinatario incorrecto"
					, "Error" ,JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if(transaccionActual.salidas.get(1).destinatario != transaccionActual.remitente){
					JOptionPane.showMessageDialog(null, "Transaccion(" + t + ") remitente incorrecto"
					, "Error" ,JOptionPane.ERROR_MESSAGE);
					return false;
				}}}
		ImageIcon icono=new ImageIcon(".//recourses/emojiFeliz.png");
		ImageIcon icono1=new ImageIcon(icono.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
		JOptionPane.showMessageDialog(null, "La cadena de bloques es valida", "Felicitaciones" ,JOptionPane.INFORMATION_MESSAGE, icono1);
		return true;
	}
	
	public static void añadirBloque(Bloque nuevoBloque){
		nuevoBloque.minarBloque(dificultad);
		blockchain.add(nuevoBloque);
	}
}