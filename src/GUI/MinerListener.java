package GUI;

/**
 * Interface de Escuta (Listener) para eventos do processo de Mineração (Proof of Work).
 * <p>
 * Implementa o <b>Padrão Observer</b>.
 * Permite que o motor de mineração (backend) comunique alterações de estado
 * à interface gráfica (frontend) de forma desacoplada.
 * <p>
 * Quem implementar esta interface (ex: MainGUI) será notificado quando:
 * <ul>
 * <li>A mineração começar.</li>
 * <li>A mineração for interrompida (por outro nó).</li>
 * <li>Uma solução (Nonce) for encontrada por este nó.</li>
 * </ul>
 * Created on 10/12/2025, 14:26:19
 * @author manso - computer
 */
public interface MinerListener {

    /**
     * Invocado quando o processo de mineração é iniciado.
     * Útil para ativar animações na GUI (ex: imgMiner) ou bloquear botões.
     * @param message A mensagem (cabeçalho do bloco) que está a ser processada.
     * @param dificulty O nível de dificuldade (número de zeros) exigido.
     */
    public void onStartMining(String message, int dificulty);

    /**
     * Invocado quando a mineração é parada forçosamente.
     * Geralmente ocorre quando outro nó da rede encontrou o bloco antes de nós.
     * @param nonce O nonce encontrado pelo outro nó (ou um código de erro).
     */
    public void onStopMining(int nonce);

    /**
     * Invocado quando ESTE nó encontra a solução do puzzle (Vencedor).
     * Útil para mostrar mensagens de vitória, tocar sons ou exibir troféus.
     * @param nonce O número inteiro que satisfaz a dificuldade do hash.
     */
    public void onNonceFound(int nonce);

}