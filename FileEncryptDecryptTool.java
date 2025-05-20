import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class FileEncryptDecryptTool {

    private static final String ALGORITHM = "AES"; // Advanced Encryption Standard
    private static SecretKeySpec secretKey;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("File Encryption/Decryption Tool");
        System.out.println("\n");

        System.out.print("Enter secret key (at least 10 characters): ");
        String key = scanner.nextLine();
        setKey(key);

        System.out.print("Enter 'encrypt' or 'decrypt': ");
        String mode = scanner.nextLine().toLowerCase();

        System.out.print("Enter the path to the input file: ");
        String inputFile = scanner.nextLine();

        String outputFile;
        if (mode.equals("encrypt")) {
            outputFile = inputFile + ".encrypted";
        } else if (mode.equals("decrypt")) {
            outputFile = inputFile.replace(".encrypted", "");
        } else {
            System.out.println("Invalid mode. Please enter 'encrypt' or 'decrypt'.");
            scanner.close();
            return;
        }

        try {
            File in = new File(inputFile);
            File out = new File(outputFile);

            if (mode.equals("encrypt")) {
                encryptFile(in, out);
                System.out.println("File encrypted successfully. Output file: " + outputFile);
            } else if (mode.equals("decrypt")) {
                decryptFile(in, out);
                System.out.println("File decrypted successfully. Output file: " + outputFile);
            }

        } catch (Exception e) {
            System.err.println("Error during " + mode + "ion: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    // Method to set the secret key from the user-provided string
    private static void setKey(String myKey) {
        MessageDigest sha = null;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-256"); // Secure Hash Algorithm 256
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // Use only the first 128 bits (16 bytes) for AES
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error setting key: " + e.getMessage());
        }
    }

    // Method to encrypt a file
    private static void encryptFile(File inputFile, File outputFile) throws Exception {
        encryptDecryptFile(Cipher.ENCRYPT_MODE, inputFile, outputFile);
    }

    // Method to decrypt a file
    private static void decryptFile(File inputFile, File outputFile) throws Exception {
        encryptDecryptFile(Cipher.DECRYPT_MODE, inputFile, outputFile);
    }

    // Core method to handle both encryption and decryption
    private static void encryptDecryptFile(int mode, File inputFile, File outputFile) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM); // Get the AES cipher
        cipher.init(mode, secretKey); // Initialize the cipher with the mode (encrypt/decrypt) and the key

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192]; // Read file in chunks
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead); // Process the chunk
                if (output != null) {
                    outputStream.write(output); // Write the processed chunk to the output file
                }
        
            }

            byte[] finalOutput = cipher.doFinal(); // Process the last remaining bytes
            if (finalOutput != null) {
                outputStream.write(finalOutput);
            }
        }
    }
}