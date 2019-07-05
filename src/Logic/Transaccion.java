package Logic;

import java.security.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Transaccion {
	
	public String transaccionId; //Contiene hash transaccion
	public PublicKey remitente; 
	public PublicKey destinatario;
	public float valor;
	public byte[] Firma; //ID firma que impide que otras personas gasten los fondos
	public ArrayList<TransaccionEntrada> entradas = new ArrayList<>();
	public ArrayList<TransaccionSaliente> salidas = new ArrayList<>();
	private static int contador = 0; //Contador transacciones realizadas 
	
	// Constructor: 
	public Transaccion(PublicKey de, PublicKey para, float valor,  ArrayList<TransaccionEntrada> entradas) {
		this.remitente = de;
		this.destinatario = para;
		this.valor = valor;
		this.entradas = entradas;
	}
	
	public boolean procesoTransaccion() {
		if(verificarFirma() == false) {
			JOptionPane.showMessageDialog(null, "Firma incorrecta", "Error" ,JOptionPane.ERROR_MESSAGE);
			return false;
		}
		for(TransaccionEntrada i : entradas) { //recopilar entrada transaccinoes
			i.UTXO = Educoin.UTXOs.get(i.idTransaccionSalida);
		}
		if(obtenerValorEntrada() < Educoin.transaccionMinima) { //Verificar si la transaccion es valida 
			JOptionPane.showMessageDialog(null, "Valor de transaccion es muy pequeño: " + obtenerValorEntrada(), " " ,JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(null, "Ingrese un monto mas grande que: " + Educoin.transaccionMinima, " " ,JOptionPane.INFORMATION_MESSAGE);
			System.out.println();
			return false;
		}
		//Generar transaccion salidas:
		float leftOver = obtenerValorEntrada() - valor; //Saldo restante
		transaccionId = calcularHash();
		salidas.add(new TransaccionSaliente( this.destinatario, valor,transaccionId)); //transaccion hacia receptor
		salidas.add(new TransaccionSaliente( this.remitente, leftOver,transaccionId)); //transaccion sobrante hacia remitente		
		for(TransaccionSaliente o : salidas) { //Add salidas hacia lista no gastada
			Educoin.UTXOs.put(o.id , o);
		}
		for(TransaccionEntrada i : entradas) { //Remueve lista transaccines no gastadas
			if(i.UTXO == null) continue; //Si la transaccion no se encuentra va a continuar
			Educoin.UTXOs.remove(i.UTXO.id);
		}
		return true;
	}
	
	public float obtenerValorEntrada() {
		float total = 0;
		for(TransaccionEntrada i : entradas) {
			if(i.UTXO == null) continue; //Si la transaccion no se encuentra, va a saltar
			total += i.UTXO.valor;
		}
		return total;
	}
	
	public void generarFirma(PrivateKey llavePrivada) {
		String data = Cadena.generarStringDesdeLlave(remitente) + Cadena.generarStringDesdeLlave(destinatario) + Float.toString(valor);
		Firma = Cadena.aplicarECDSA(llavePrivada,data);		
	}
	
	public boolean verificarFirma() {
		String data = Cadena.generarStringDesdeLlave(remitente) + Cadena.generarStringDesdeLlave(destinatario) + Float.toString(valor);
		return Cadena.verificarECDSA(remitente, data, Firma);
	}
	
	public float obtenerValorSalida() {
		float total = 0;
		for(TransaccionSaliente o : salidas) {
			total += o.valor;
		}
		return total;
	}
	
	private String calcularHash() {
		contador++; //Incrementa contador evitar que transacciones tengan el mismo hash
		return Cadena.aplicarSha256(
				Cadena.generarStringDesdeLlave(remitente) +
				Cadena.generarStringDesdeLlave(destinatario) +
				Float.toString(valor) + contador
				);
	}
}