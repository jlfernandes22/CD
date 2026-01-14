package GUI;

/**
 * Interface de Escuta (Listener) para eventos da Rede P2P.
 * <p>
 * Implementa o <b>Padrão Observer</b>.
 * Serve como ponte entre a lógica de rede (backend RMI) e a interface gráfica (frontend).
 * <p>
 * Permite que a classe {@code RemoteNodeObject} atualize a GUI (logs, listas, alertas)
 * de forma assíncrona sempre que ocorrer um evento na rede distribuída.
 * Created on 27/11/2024, 19:46:43 
 * @author manso - computer
 */
public interface Nodelistener {

    /**
     * Invocado quando o servidor P2P local é iniciado com sucesso.
     * Útil para atualizar a barra de estado ou desbloquear botões na GUI.
     * @param message Mensagem de estado ou confirmação de arranque.
     */
    public void onStart(String message);

    /**
     * Invocado quando uma conexão com um novo nó remoto é estabelecida.
     * Usado para atualizar a lista visual de "Nós Conectados" na aba de rede.
     * @param address O endereço (RMI URL) do nó que foi conectado (ex: //192.168.1.5:10010/remote).
     */
    public void onConect(String address);

    /**
     * Invocado quando ocorre uma exceção ou erro na camada de rede.
     * Permite que a GUI exiba um popup (JOptionPane) amigável ao utilizador com o erro.
     * @param e A exceção capturada (contém a stack trace).
     * @param title Um título descritivo para a janela de erro (ex: "Erro de Conexão").
     */
    public void onException(Exception e, String title);

    /**
     * Invocado quando dados chegam da rede.
     * Este método é genérico e trata dois tipos de eventos críticos:
     * <ol>
     * <li><b>Nova Transação:</b> Recebida na Mempool (atualiza log).</li>
     * <li><b>Novo Bloco:</b> Recebido sinal de "BlockReceived" (atualiza blockchain e inventário).</li>
     * </ol>
     * @param transaction Os dados recebidos (pode ser o JSON da transação ou um comando de controlo).
     */
    public void onTransaction(String transaction);

}