package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {

    /**
     * Comprime uma PASTA inteira para um array de bytes (Formato ZIP)
     */
    public static byte[] zipFolder(File folder) throws IOException {
        if (!folder.exists() || !folder.isDirectory()) {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        // Cria entrada no Zip
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zos.putNextEntry(zipEntry);

                        // Lê o ficheiro e escreve no Zip
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) >= 0) {
                                zos.write(buffer, 0, length);
                            }
                        }
                        zos.closeEntry();
                    }
                }
            }
        }
        return bos.toByteArray();
    }

    /**
     * Descomprime um array de bytes (ZIP) para dentro de uma PASTA
     */
    public static void unzipFolder(byte[] data, File destFolder) throws IOException {
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try (ZipInputStream zis = new ZipInputStream(bis)) {
            ZipEntry zipEntry = zis.getNextEntry();
            
            while (zipEntry != null) {
                File newFile = new File(destFolder, zipEntry.getName());
                
                // Proteção contra Zip Slip (segurança)
                if (!newFile.getCanonicalPath().startsWith(destFolder.getCanonicalPath())) {
                    throw new IOException("Entrada Zip fora da pasta de destino: " + zipEntry.getName());
                }

                // Escrever o ficheiro no disco
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }
}