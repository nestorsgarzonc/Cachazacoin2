package Logic;

public class TransaccionEntrada {
	public String idTransaccionSalida;
	public TransaccionSaliente UTXO; //Contiene transacciones no gastadas
	
	public TransaccionEntrada(String idTransaccionSalida) {
		this.idTransaccionSalida = idTransaccionSalida;
	}
}