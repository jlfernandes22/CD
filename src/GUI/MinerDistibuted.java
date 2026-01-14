package GUI;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Motor de Mineração Distribuída (Multi-Threaded).
 * <p>
 * Esta classe implementa o algoritmo de Proof of Work (PoW) utilizando paralelismo.
 * Divide a tarefa de procurar o "Nonce" por todos os núcleos (cores) do processador
 * disponíveis na máquina para maximizar a performance.
 * <p>
 * Utiliza variáveis atómicas para garantir a segurança entre threads sem bloquear o processamento.
 * Created on 27/11/2024, 14:14:13
 * @author manso - computer
 */
public class MinerDistibuted {

    /**
     * Método Main para testes isolados da classe.
     * Simula o início da mineração e uma paragem forçada após 1 segundo.
     */
    public static void main(String[] args) {
        String msg = "Transaction 7";
        MinerDistibuted miner = new MinerDistibuted();

        // Thread paralela para simular uma interrupção externa (ex: outro nó ganhou)
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("STOP");
                System.out.println("Mining " + miner.isMining());
                miner.stopMining(20); // Simula que o nonce 20 foi encontrado por outro
            } catch (InterruptedException ex) {
                System.getLogger(MinerDistibuted.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

        }).start();
        
        // Inicia a mineração
        int n = miner.mine(msg, 4);
        
        System.out.println("Message = " + msg);
        System.out.println("Nonce = " + miner.nonce);
        System.out.println("Hash = " + getHash(msg + n));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: VARIÁVEIS PARTILHADAS (THREAD-SAFE)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    /** * O Nonce encontrado.
     * Iniciado a 0. Se for diferente de 0, significa que uma thread encontrou a solução
     * ou que foi interrompido externamente.
     */
    AtomicInteger nonce = new AtomicInteger(0); 
    
    /** Flag que indica se o mineiro está ativo. */
    AtomicBoolean isWorking = new AtomicBoolean();
    
    /** * Flag que indica se FOI ESTE NÓ que encontrou o bloco (Vencedor).
     * Se false, significa que parámos porque outro nó encontrou primeiro.
     */
    static AtomicBoolean isChampion = new AtomicBoolean();
    
    /** Listener para comunicar eventos à GUI. */
    static MinerListener listener;
    
    /** Dados do cabeçalho do bloco a minerar. */
    String message;

    /**
     * Regista um listener para receber atualizações do progresso.
     * @param listener A classe que implementa a interface MinerListener (ex: MainGUI).
     */
    public void addListener(MinerListener listener) {
        this.listener = listener;
    }

    /**
     * Pára a mineração imediatamente.
     * Usado quando recebemos um bloco válido da rede e devemos parar de gastar energia.
     * @param number O nonce encontrado pelo outro nó (ou -1 se irrelevante).
     */
    public void stopMining(int number) {
        isWorking.set(false);
        nonce.set(number); // Define o nonce para parar os loops das threads
        
        if(listener != null){
            listener.onStopMining(number);
        } else {
            System.out.println("Stoping miner .. " + nonce);
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS DE ESTADO
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public int getNonce() {
        return nonce.get();
    }

    public boolean isMining() {
        return isWorking.get();
    }

    /** @return true se fui eu que encontrei o nonce, false se fui interrompido. */
    public boolean isWinner() {
        return isChampion.get();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: CORE DE MINERAÇÃO
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Inicia o processo de mineração bloqueante.
     * Cria tantas threads quanto o número de núcleos do CPU.
     * @param msg Cabeçalho do bloco (Base64).
     * @param dificulty Número de zeros exigidos.
     * @return O nonce que resolve o puzzle.
     */
    public int mine(String msg, int dificulty) {
        this.message = msg;
        try {
            // Notificar início
            if (listener != null) {
                listener.onStartMining(msg, dificulty);
            } else {
                System.out.println("Start Mining " + dificulty + "\t" + msg);
            }
            
            // Inicializar controlo de threads
            isWorking.set(true);
            isChampion.set(false);
            nonce = new AtomicInteger(0); 
            
            // Ticket: Um contador atómico partilhado.
            // Cada thread pede um número único daqui para testar.
            // Random garante que não começamos sempre do 0 em todas as máquinas.
            Random rnd = new Random();
            AtomicInteger ticket = new AtomicInteger(Math.abs(rnd.nextInt())); 
            
            // Criar Pool de Threads (Worker Threads)
            int numCores = Runtime.getRuntime().availableProcessors();
            MinerThr thr[] = new MinerThr[numCores];
            
            for (int i = 0; i < thr.length; i++) {
                thr[i] = new MinerThr(nonce, ticket, dificulty, msg);
                thr[i].start();
            }
            
            // Esperar que a primeira thread termine (quando nonce != 0)
            thr[0].join();
            
            return nonce.get();
            
        } catch (InterruptedException ex) {
            return 0;
        }
    }

    /**
     * Thread de Trabalho (Worker).
     * Cada instância desta classe corre num núcleo do CPU.
     */
    private static class MinerThr extends Thread {

        AtomicInteger trueNonce;    // Referência partilhada para o resultado final
        AtomicInteger numberTicket; // Contador global de números a testar
        int dificulty;              // Dificuldade (Zeros)
        String message;             // Dados do bloco

        public MinerThr(AtomicInteger nonce, AtomicInteger ticket, int dificulty, String msg) {
            this.trueNonce = nonce;
            this.numberTicket = ticket;
            this.dificulty = dificulty;
            this.message = msg;
        }

        @Override
        public void run() {
            // Criar string de zeros alvo (Ex: "0000")
            String zeros = String.format("%0" + dificulty + "d", 0);
            
            // Loop enquanto ninguém encontrou o nonce (trueNonce == 0)
            while (trueNonce.get() == 0) {
                
                // 1. Obter próximo número único para testar
                int n = numberTicket.getAndIncrement();
                
                // 2. Calcular Hash
                String hash = getHash(message + n);
                
                // 3. Verificar se resolve o puzzle
                if (hash.startsWith(zeros)) {
                    isChampion.set(true); // SOU O VENCEDOR!
                    
                    // Definir o resultado (isto fará as outras threads pararem)
                    trueNonce.set(n);
                    
                    if (listener != null) {
                        listener.onNonceFound(n);
                    } else {
                        System.out.println(Thread.currentThread().getName() + " found nonce " + n);
                        System.out.println("Hash " + hash);
                    }
                }
            }
            System.out.println(Thread.currentThread().getName() + " stop ");
        }
    }

    /**
     * Calcula o Hash SHA-256 de uma string.
     * @param msg Texto a cifrar.
     * @return Hash em Base64.
     */
    public static String getHash(String msg) {
        try {
            // Algoritmo standard da Blockchain
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            md.update(msg.getBytes());
            return java.util.Base64.getEncoder().encodeToString(md.digest());
        } catch (java.security.NoSuchAlgorithmException ex) {
            return "ERROR";
        }
    }
}