package Logic;

import java.security.PublicKey;

public class TransaccionSaliente {
	public String id;
	public PublicKey destinatario; //also known as the new owner of these coins.
	public float valor; //the amount of coins they own
	public String idTransaccion; 
	
	//Constructor
	public TransaccionSaliente(PublicKey destinatario, float valor, String idTransaccion) {
		this.destinatario = destinatario;
		this.valor = valor;
		this.idTransaccion = idTransaccion;
		this.id = Cadena.aplicarSha256(Cadena.generarStringDesdeLlave(destinatario)+Float.toString(valor)+idTransaccion);
	}
	
	//Verificar si el dinero le pertenece al usuario
	public boolean isMine(PublicKey llavePublica) {
		return (llavePublica == destinatario);
	}
}
