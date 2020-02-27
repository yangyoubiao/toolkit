package com.yangyoubiao.www.encryption;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

/**
 * 这个工具类中使用的加密组件是由jdk提供的<br>
 * 参考：https://blog.csdn.net/aimashi620/article/details/80980867
 */
public class JdkEncryptionUtils {

	/**
	 * 生成DES密钥
	 */
	public static byte[] getDesSecretKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");// Key的生成器
		keyGenerator.init(56);// 指定keySize
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] bytesKey = secretKey.getEncoded();
		return bytesKey;
	}

	/**
	 * des加密的方法
	 * 
	 * @param text   加密的主体
	 * @param secret 密钥 长度为56bit
	 * 
	 * @return 返回密文
	 */
	public static byte[] desEncrypt(byte[] text, byte[] secret) throws Exception {
		// KEY转换
		DESKeySpec desKeySpec = new DESKeySpec(secret);// 实例化DESKey秘钥的相关内容
		SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");// 实例一个秘钥工厂，指定加密方式
		Key convertSecretKey = factory.generateSecret(desKeySpec);
		// 加密 DES/ECB/PKCS5Padding--->算法/工作方式/填充方式
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");// 通过Cipher这个类进行加解密相关操作
		cipher.init(Cipher.ENCRYPT_MODE, convertSecretKey);
		byte[] result = cipher.doFinal(text);// 输入明文
		return result;
	}

	/**
	 * des解码的方法
	 * 
	 * @param text   密文
	 * @param secret 密钥 长度为56bit
	 * 
	 * @return 返回明文
	 */
	public static byte[] desDecrypt(byte[] text, byte[] secret) throws Exception {
		// KEY转换
		DESKeySpec desKeySpec = new DESKeySpec(secret);// 实例化DESKey秘钥的相关内容
		SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");// 实例一个秘钥工厂，指定加密方式
		Key convertSecretKey = factory.generateSecret(desKeySpec);
		// 解密 DES/ECB/PKCS5Padding--->算法/工作方式/填充方式

		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");// 通过Cipher这个类进行加解密相关操作
		cipher.init(Cipher.DECRYPT_MODE, convertSecretKey);
		byte[] result = cipher.doFinal(text);// 输入密文
		return result;
	}

	/**
	 * 生成3DES密钥 长度支持112、168默认为168
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] getThreeDesSecretKey() throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("DESEde");
		keyGenerator.init(168);// 长度支持112、168默认为168
		// keyGenerator.init(new SecureRandom());
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] bytesKey = secretKey.getEncoded();
		return bytesKey;

	}

	/**
	 * 3des加密的方法
	 * 
	 * @param text   加密的主体
	 * @param secret 密钥
	 * 
	 * @return 返回密文
	 */
	public static byte[] threeDesEncrypt(byte[] text, byte[] secret) throws Exception {
		// KEY转换
		DESedeKeySpec desKeySpec = new DESedeKeySpec(secret);// 实例化DESKey秘钥的相关内容
		SecretKeyFactory factory = SecretKeyFactory.getInstance("DESEde");// 实例一个秘钥工厂，指定加密方式
		Key convertSecretKey = factory.generateSecret(desKeySpec);
		// 加密
		Cipher cipher = Cipher.getInstance("DESEde/ECB/PKCS5Padding");// DES/ECB/PKCS5Padding--->算法/工作方式/填充方式通过Cipher这个类进行加解密相关操作
		cipher.init(Cipher.ENCRYPT_MODE, convertSecretKey);
		byte[] result = cipher.doFinal(text);// 输入明文
		return result;
	}

	/**
	 * 3des解码的方法
	 * 
	 * @param text   密文
	 * @param secret 密钥
	 * 
	 * @return 返回明文
	 */
	public static byte[] threeDesDecrypt(byte[] text, byte[] secret) throws Exception {
		// KEY转换
		DESedeKeySpec desKeySpec = new DESedeKeySpec(secret);// 实例化DESKey秘钥的相关内容
		SecretKeyFactory factory = SecretKeyFactory.getInstance("DESEde");// 实例一个秘钥工厂，指定加密方式
		Key convertSecretKey = factory.generateSecret(desKeySpec);
		// 解密
		Cipher cipher = Cipher.getInstance("DESEde/ECB/PKCS5Padding");// DES/ECB/PKCS5Padding--->算法/工作方式/填充方式,通过Cipher这个类进行加解密相关操作
		cipher.init(Cipher.DECRYPT_MODE, convertSecretKey);
		byte[] result = cipher.doFinal(text);// 输入密文
		return result;
	}

	/**
	 * 生成AES密钥 长度支持128、192、256默认为128
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] getAesSecretKey() throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(new SecureRandom());
		// keyGenerator.init(128);//用于指定长度
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] bytesKey = secretKey.getEncoded();
		return bytesKey;

	}

	/**
	 * aes加密的方法
	 * 
	 * @param text   加密的主体
	 * @param secret 密钥
	 * 
	 * @return 返回密文
	 */
	public static byte[] aesEncrypt(byte[] text, byte[] secret) throws Exception {
		// KEY转换
		Key key = new SecretKeySpec(secret, "AES");
		// 加密
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] result = cipher.doFinal(text);// 输入明文
		return result;
	}

	/**
	 * aes解码的方法
	 * 
	 * @param text   密文
	 * @param secret 密钥
	 * 
	 * @return 返回明文
	 */
	public static byte[] aesDecrypt(byte[] text, byte[] secret) throws Exception {
		// KEY转换
		Key key = new SecretKeySpec(secret, "AES");
		// 解密
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] result = cipher.doFinal(text);// 输入密文
		return result;
	}

	/** PBE（Password Based Encryption 基于口令加密） */

	/**
	 * 生成PBE盐值
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] getPbeSalt() throws Exception {
		// 1. 初始化盐
		SecureRandom secureRandom = new SecureRandom();
		byte[] salt = secureRandom.generateSeed(8);
		return salt;
	}

	/**
	 * PBE加密的方法
	 * 
	 * @param text   加密的主体
	 * @param salt   pbe的盐值
	 * @param secret 密钥
	 * 
	 * @return 返回密文
	 */
	public static byte[] pbeEncrypt(byte[] text, byte[] salt, String secret) throws Exception {
		// 生成密钥
		PBEKeySpec pbeKeySpec = new PBEKeySpec(secret.toCharArray());
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHMD5andDES");
		Key key = factory.generateSecret(pbeKeySpec);
		// 加密
		PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, 100);// 100是迭代的次数
		Cipher cipher = Cipher.getInstance("PBEWITHMD5andDES");
		cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
		byte[] result = cipher.doFinal(text);
		return result;
	}

	/**
	 * PBE解码的方法
	 * 
	 * @param text   密文
	 * @param salt   pbe的盐值
	 * @param secret 密钥
	 * 
	 * @return 返回明文
	 */
	public static byte[] pbeDecrypt(byte[] text, byte[] salt, String secret) throws Exception {
		// 生成密钥
		PBEKeySpec pbeKeySpec = new PBEKeySpec(secret.toCharArray());
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHMD5andDES");
		Key key = factory.generateSecret(pbeKeySpec);
		// 解密
		PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, 100);// 100是迭代的次数
		Cipher cipher = Cipher.getInstance("PBEWITHMD5andDES");
		cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
		byte[] result = cipher.doFinal(text);
		return result;
	}

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		byte[] salt = getPbeSalt();
		System.out.println(Hex.encodeHexString(salt));
		String str = "杨友彪";
		String secret = "yangyoubiao";
		byte[] result = pbeEncrypt(str.getBytes("UTF-8"), salt, secret);
		result = pbeDecrypt(result, salt, secret);
		System.out.println(new String(result, "UTF-8"));
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}

}
