/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import SaudeCerteira.SaudeWallet;
import SaudeCerteira.User;
import javax.swing.JOptionPane;

/**
 * Janela de Autenticação (Login) do sistema Saúde Certeira.
 * <p>
 * Esta classe serve como o ponto de entrada da aplicação (Entry Point).
 * Permite que os utilizadores:
 * <ul>
 * <li>Introduzam as suas credenciais para aceder à carteira.</li>
 * <li>Naveguem para o ecrã de registo de novos utilizadores.</li>
 * </ul>
 * @author jlfernandes
 */
public class JanelaLogin extends javax.swing.JFrame {

    /** Logger para registo de exceções e eventos do sistema. */
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JanelaLogin.class.getName());

    /**
     * Construtor da Janela de Login.
     * Inicializa os componentes visuais e define a posição da janela.
     */
    public JanelaLogin() {
        try {
            // Nota: A linha abaixo serve para fazer Hard Reset à blockchain em modo debug.
            // core.BlockChain newBlockchain = SaudeCerteira.SaudeWallet.restartSaudeCerteira();
            
            initComponents();
            setLocationRelativeTo(null); // Centrar a janela no ecrã
        } catch (Exception ex) {
            System.getLogger(JanelaLogin.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    /**
     * Inicializa os componentes da interface gráfica.
     * <p>
     * <b>AVISO:</b> Código gerado automaticamente pelo Form Editor do NetBeans.
     * Não deve ser alterado manualmente.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        LoginButton = new javax.swing.JButton();
        UtilizadorField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        RegistarButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        PasswordField = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 153, 153));
        setForeground(new java.awt.Color(0, 153, 153));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(0, 204, 204));

        LoginButton.setBackground(new java.awt.Color(204, 255, 255));
        LoginButton.setFont(new java.awt.Font("Hiragino Sans", 3, 18)); // NOI18N
        LoginButton.setText("Login");
        LoginButton.addActionListener(this::LoginButtonActionPerformed);

        UtilizadorField.setBackground(new java.awt.Color(226, 247, 255));
        UtilizadorField.addActionListener(this::UtilizadorFieldActionPerformed);

        jLabel1.setFont(new java.awt.Font("Hiragino Sans GB", 3, 24)); // NOI18N
        jLabel1.setText("Utilizador");

        RegistarButton.setBackground(new java.awt.Color(204, 255, 255));
        RegistarButton.setFont(new java.awt.Font("Hiragino Sans", 3, 18)); // NOI18N
        RegistarButton.setText("Registar");
        RegistarButton.addActionListener(this::RegistarButtonActionPerformed);

        jLabel2.setFont(new java.awt.Font("Hiragino Sans GB", 3, 24)); // NOI18N
        jLabel2.setText("Password");

        jPanel3.setBackground(new java.awt.Color(204, 255, 255));

        jLabel3.setBackground(new java.awt.Color(255, 204, 153));
        jLabel3.setFont(new java.awt.Font("ITF Devanagari", 3, 48)); // NOI18N
        jLabel3.setText("Saúde Certeira");
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 0, 0))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel3)
                .addGap(0, 0, 0))
        );

        PasswordField.setPreferredSize(new java.awt.Dimension(5, 20));
        PasswordField.addActionListener(this::PasswordFieldActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(124, 124, 124)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(119, 119, 119)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(119, 119, 119)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(RegistarButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LoginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(103, 103, 103)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(UtilizadorField, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                            .addComponent(PasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UtilizadorField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(0, 0, 0)
                .addComponent(PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                .addComponent(LoginButton)
                .addGap(36, 36, 36)
                .addComponent(RegistarButton)
                .addGap(80, 80, 80))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void UtilizadorFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UtilizadorFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UtilizadorFieldActionPerformed

    /**
     * Ação executada ao clicar no botão "Login".
     * <p>
     * Fluxo de Execução:
     * <ol>
     * <li>Obtém o nome de utilizador e password dos campos de texto.</li>
     * <li>Tenta autenticar o utilizador e carregar as chaves (User.login).</li>
     * <li>Carrega a carteira do disco para memória (SaudeWallet.load).</li>
     * <li>Abre a janela principal (MainGUI) passando o utilizador autenticado.</li>
     * <li>Fecha a janela de login.</li>
     * </ol>
     * @param evt O evento de clique.
     */
    private void LoginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoginButtonActionPerformed
        try {
            // 1. Obter credenciais
            String name = UtilizadorField.getText();
            String password = PasswordField.getText();

            // 2. Autenticação Criptográfica
            // Verifica se o user existe e se a password desencripta a chave privada corretamente
            SaudeCerteira.User loggedUser = SaudeCerteira.User.login(name, password);

            // 3. Carregar Carteira
            SaudeCerteira.SaudeWallet t = SaudeWallet.load(name);

            // Debug: Mostrar estado da carteira na consola
            System.out.println(t.toString());

            // 4. Abrir Aplicação Principal
            // Passamos o objeto User logado para configurar permissões na GUI
            MainGUI janelaPrincipal = new MainGUI(loggedUser);

            janelaPrincipal.setVisible(true);
            this.dispose(); // Fecha esta janela

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro no Login: " + ex.getMessage());
        }
    }//GEN-LAST:event_LoginButtonActionPerformed

    /**
     * Ação executada ao clicar no botão "Registar".
     * <p>
     * Abre a janela de registo para criar novos utilizadores.
     * @param evt O evento de clique.
     */
    private void RegistarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegistarButtonActionPerformed

        JanelaRegister janela = new JanelaRegister();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
        this.dispose(); // Fecha o login ao ir para o registo

    }//GEN-LAST:event_RegistarButtonActionPerformed

    private void PasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PasswordFieldActionPerformed
        // Ação ao pressionar Enter no campo password (pode chamar o login automaticamente se desejado)
    }//GEN-LAST:event_PasswordFieldActionPerformed

    /**
     * Ponto de entrada da aplicação.
     * @param args Argumentos da linha de comandos.
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new JanelaLogin().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton LoginButton;
    private javax.swing.JPasswordField PasswordField;
    private javax.swing.JButton RegistarButton;
    private javax.swing.JTextField UtilizadorField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}