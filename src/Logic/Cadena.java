package Logic;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Cadena {
	//Aplica codificacion Sha256 al String
	public static String aplicarSha256(String entrada){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256"); //Crea instancia algorito Sha
			byte[] hash = digest.digest(entrada.getBytes("UTF-8"));
			StringBuilder hexString = new StringBuilder(); // Convertir hash a hexadecimal
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	//Aplica algoritmo ECDSA y lo convierte como bytes
	public static byte[] aplicarECDSA(PrivateKey llavePrivada, String entrada) {
		Signature dsa; //Firma 
		byte[] salida = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(llavePrivada);
			byte[] strByte = entrada.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			salida = realSig;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return salida;
	}
	//Verificar si la firma es falsa
	public static boolean verificarECDSA(PublicKey llavePublica, String data, byte[] firma) { 
		try {
			Signature verificarECDSA = Signature.getInstance("ECDSA", "BC");
			verificarECDSA.initVerify(llavePublica);
			verificarECDSA.update(data.getBytes());
			return verificarECDSA.verify(firma);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	//Devuelve la dificultad que es comparada con el hash
	public static String obtenerDificultadString(int dificultad) {
		return new String(new char[dificultad]).replace('\0', '0');
	}
	
	public static String generarStringDesdeLlave(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded()); //COdificar la cadena
	}
	
	public static String obtenerMerkleRoot(ArrayList<Transaccion> transacciones) {
		int contador = transacciones.size();
		List<String> arregloBytesAnterior = new ArrayList<>();
		for(Transaccion transaccion : transacciones) {
			arregloBytesAnterior.add(transaccion.transaccionId);
		}
		List<String> arregloBytes = arregloBytesAnterior;
		while(contador > 1) {
			arregloBytes = new ArrayList<>();
			for(int i=1; i < arregloBytesAnterior.size(); i+=2) {
				arregloBytes.add(aplicarSha256(arregloBytesAnterior.get(i-1) + arregloBytesAnterior.get(i)));
			}
			contador = arregloBytes.size();
			arregloBytesAnterior = arregloBytes;
		}
		String merkleRoot = (arregloBytes.size() == 1) ? arregloBytes.get(0) : "";
		return merkleRoot;
	}
}