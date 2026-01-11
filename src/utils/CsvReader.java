package utils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvReader {

    public static void main(String[] args) {
        // Path to your file
        String nomeFicheiro = "medicamentos.csv"; 
        String separador = ","; // Use ";" if your Excel/CSV uses semicolons

        try (BufferedReader br = new BufferedReader(new FileReader(nomeFicheiro))) {
            String linha;
            
            // Loop through every line of the file
            while ((linha = br.readLine()) != null) {
                
                // Split the line by the separator
                String[] dados = linha.split(separador);

                // Example: accessing columns
                // dados[0] = First column (e.g., Name)
                // dados[1] = Second column (e.g., Quantity)
                
                System.out.println("Medicamento: " + dados[0] + " | Qtd: " + dados[1]);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}