package simple.example.linux;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.frameworkdemoiselle.certificate.signer.SignerException;
import br.gov.frameworkdemoiselle.certificate.signer.factory.PKCS7Factory;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.PKCS7Signer;
import br.gov.frameworkdemoiselle.policy.engine.factory.PolicyFactory.Policies;


public class CertificateSignerLinux {

	private static final Logger logger = LoggerFactory
			.getLogger(CertificateSignerLinux.class);

	public static void main(String[] args) throws KeyStoreException {

		String configName = "/home/01534562567/drivers.config";
		String PIN = "";
		Certificate[] certificates = null;


		String alias;
		try {
			logger.info("-------- Fabrica do certificate --------");

			Provider p = new sun.security.pkcs11.SunPKCS11(configName);
			Security.addProvider(p);

			KeyStore keyStore = KeyStore.getInstance("PKCS11", "SunPKCS11-Provedor");
			keyStore.load(null, PIN.toCharArray());

			alias = (String) keyStore.aliases().nextElement();
			logger.info("alias ...........: {}", alias);
			PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias,
					PIN.toCharArray());
			logger.info("privateKey ......: {}", privateKey);

			byte[] content = "SERPRO".getBytes();

			PKCS7Signer signer = PKCS7Factory.getInstance().factoryDefault();
			signer.setCertificates(keyStore.getCertificateChain(alias));
			signer.setPrivateKey(privateKey);
			signer.setSignaturePolicy(Policies.AD_RB_CADES_2_1);
			//signer.setSignaturePolicy(new ADRBCMS_2_1());
			signer.setAttached(true);

			logger.info("Efetuando a  assinatura do conteudo");
			byte[] signed = signer.doSign(content);
			//byte[] signed = signer.signer(content);

			logger.info("Efetuando a validacao da assinatura.");
			boolean checked = signer.check(content, signed);

			if (checked) {
				logger.info("A assinatura foi validada.");
			} else {
				logger.info("A assinatura foi invalidada!");
			}

			logger.info("Exportando a assintatura.");
			File file = new File("assinaturaV200RB.p7s"); 
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file)); // Criamos o arquivo
			bos.write(signed); // Gravamos os bytes lá
			bos.close(); // Fechamos o stream.

		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} catch (NoSuchProviderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CertificateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (SignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
