/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import SaudeCerteira.SaudeWallet;
import SaudeCerteira.User;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Janela de Registo de Novos Utilizadores.
 * <p>
 * Esta interface permite criar uma nova identidade digital no sistema Saúde Certeira.
 * O processo de registo envolve:
 * <ol>
 * <li>Recolha de dados pessoais e profissionais.</li>
 * <li>Definição do Papel (Role) na rede (Utente, Médico ou Farmacêutico).</li>
 * <li>Geração automática de pares de chaves criptográficas (RSA e AES).</li>
 * <li>Criação do ficheiro de carteira (.wlt) inicial.</li>
 * </ol>
 * * @author jlfernandes
 */
public class JanelaRegister extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JanelaRegister.class.getName());

    /**
     * Construtor da Janela de Registo.
     * Inicializa os componentes e configura a lista de Papéis (Roles) disponíveis.
     */
    public JanelaRegister() {
        initComponents();
        
        // Configuração das permissões/papéis do sistema
        TipoConta.removeAllItems();
        TipoConta.addItem("Utente");       // Cidadão comum / Paciente
        TipoConta.addItem("Médico");       // Emissor de Receitas
        TipoConta.addItem("Farmacêutico"); // Aviador de Receitas
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
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        txtDataNascimento = new javax.swing.JTextField();
        txtUtente = new javax.swing.JTextField();
        txtNaturalidade = new javax.swing.JTextField();
        txtNacionalidade = new javax.swing.JTextField();
        txtCC = new javax.swing.JTextField();
        txtNIF = new javax.swing.JTextField();
        txtNISS = new javax.swing.JTextField();
        txtUnidadeSaude = new javax.swing.JTextField();
        txtSexo = new javax.swing.JTextField();
        txtMorada = new javax.swing.JTextField();
        RegistarButton = new javax.swing.JButton();
        txtTelemovel = new javax.swing.JTextField();
        RegistarButton1 = new javax.swing.JButton();
        TipoConta = new javax.swing.JComboBox<>();
        txtPassword = new javax.swing.JPasswordField();
        txtConfirmPassword = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(0, 204, 204));

        jPanel2.setBackground(new java.awt.Color(204, 255, 255));

        jLabel3.setBackground(new java.awt.Color(204, 255, 255));
        jLabel3.setFont(new java.awt.Font("ITF Devanagari", 3, 48)); // NOI18N
        jLabel3.setText("Saúde Certeira");
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        txtNome.addActionListener(this::txtNomeActionPerformed);

        txtDataNascimento.addActionListener(this::txtDataNascimentoActionPerformed);

        txtUtente.addActionListener(this::txtUtenteActionPerformed);

        txtNaturalidade.addActionListener(this::txtNaturalidadeActionPerformed);

        txtNacionalidade.addActionListener(this::txtNacionalidadeActionPerformed);

        txtCC.addActionListener(this::txtCCActionPerformed);

        txtNIF.addActionListener(this::txtNIFActionPerformed);

        txtNISS.addActionListener(this::txtNISSActionPerformed);

        txtUnidadeSaude.addActionListener(this::txtUnidadeSaudeActionPerformed);

        txtSexo.addActionListener(this::txtSexoActionPerformed);

        txtMorada.addActionListener(this::txtMoradaActionPerformed);

        RegistarButton.setBackground(new java.awt.Color(204, 255, 255));
        RegistarButton.setFont(new java.awt.Font("Hiragino Sans", 3, 18)); // NOI18N
        RegistarButton.setText("Registar");
        RegistarButton.addActionListener(this::RegistarButtonActionPerformed);

        txtTelemovel.addActionListener(this::txtTelemovelActionPerformed);

        RegistarButton1.setBackground(new java.awt.Color(204, 255, 255));
        RegistarButton1.setFont(new java.awt.Font("Hiragino Sans", 3, 18)); // NOI18N
        RegistarButton1.setText("Login");
        RegistarButton1.addActionListener(this::RegistarButton1ActionPerformed);

        TipoConta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtConfirmPassword.addActionListener(this::txtConfirmPasswordActionPerformed);

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 51, 51));
        jLabel1.setText("Nome:");

        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("Sexo:");

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(51, 51, 51));
        jLabel4.setText("Morada:");

        jLabel5.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(51, 51, 51));
        jLabel5.setText("Tipo de Conta:");

        jLabel6.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(51, 51, 51));
        jLabel6.setText("NISS:");

        jLabel7.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(51, 51, 51));
        jLabel7.setText("Unidade de Saúde:");

        jLabel8.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(51, 51, 51));
        jLabel8.setText("Telemóvel:");

        jLabel9.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(51, 51, 51));
        jLabel9.setText("NIF:");

        jLabel10.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(51, 51, 51));
        jLabel10.setText("Número do CC:");

        jLabel11.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(51, 51, 51));
        jLabel11.setText("País de Nacionalidade:");

        jLabel12.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(51, 51, 51));
        jLabel12.setText("Número de Utente de Saúde:");

        jLabel13.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(51, 51, 51));
        jLabel13.setText("Naturalidade:");

        jLabel14.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(51, 51, 51));
        jLabel14.setText("Data de Nascimento:");

        jLabel15.setFont(new java.awt.Font("Helvetica Neue", 2, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(51, 51, 51));
        jLabel15.setText("Palavra-Passe:");

        jLabel16.setFont(new java.awt.Font("Dialog", 2, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(51, 51, 51));
        jLabel16.setText("Confirmar Palavra-Passe");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(RegistarButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(RegistarButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMorada)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel4)
                                    .addComponent(txtSexo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(txtTelemovel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6)
                                    .addComponent(txtNISS, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCC, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10)
                                    .addComponent(txtNaturalidade, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDataNascimento, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel14)
                                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TipoConta, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5)
                                    .addComponent(txtUnidadeSaude, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel11)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtNacionalidade, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                        .addComponent(txtNIF, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addComponent(txtUtente, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel12)
                                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel16)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addGap(156, 156, 156)
                                        .addComponent(jLabel15)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(30, 30, 30)))
                        .addGap(12, 12, 12)))
                .addGap(25, 25, 25))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel1))
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNome, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(txtDataNascimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNaturalidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUtente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNacionalidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNISS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNIF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTelemovel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUnidadeSaude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSexo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TipoConta, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMorada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RegistarButton)
                    .addComponent(RegistarButton1))
                .addGap(38, 38, 38))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomeActionPerformed

    private void txtDataNascimentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataNascimentoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDataNascimentoActionPerformed

    private void txtUtenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUtenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUtenteActionPerformed

    private void txtNaturalidadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNaturalidadeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNaturalidadeActionPerformed

    private void txtNacionalidadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNacionalidadeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNacionalidadeActionPerformed

    private void txtCCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCCActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCCActionPerformed

    private void txtNIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNIFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNIFActionPerformed

    private void txtNISSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNISSActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNISSActionPerformed

    private void txtUnidadeSaudeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUnidadeSaudeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUnidadeSaudeActionPerformed

    private void txtSexoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSexoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSexoActionPerformed

    private void txtMoradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMoradaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMoradaActionPerformed

    /**
     * Ação executada ao clicar no botão "Registar".
     * <p>
     * Fluxo de execução:
     * <ol>
     * <li>Obtém todos os dados preenchidos nos formulários.</li>
     * <li>Valida se as passwords coincidem.</li>
     * <li>Valida se os campos obrigatórios estão preenchidos.</li>
     * <li>Inicia uma Thread para criar o utilizador e a carteira sem bloquear a interface.</li>
     * <li>Grava os ficheiros de chaves (.pub, .priv, .aes) e a carteira (.wlt).</li>
     * </ol>
     * @param evt O evento de clique do botão.
     */
    private void RegistarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegistarButtonActionPerformed
        try {           
        // 1. Recolher dados da GUI
        String name = txtNome.getText().trim();
        String pass = txtPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        // Validação de passwords
        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "As passwords não coincidem!", "Erro de Registo", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Recolha dos restantes campos
        String dataNasc = txtDataNascimento.getText();
        String cc = txtCC.getText();
        String utente = txtUtente.getText();
        String sexo = txtSexo.getText();
        String nacionalidade = txtNacionalidade.getText();
        String naturalidade = txtNaturalidade.getText();
        String morada = txtMorada.getText();
        String niss = txtNISS.getText();
        String telemovel = txtTelemovel.getText();
        String tipoDeConta = (String) TipoConta.getSelectedItem();
        String unidade = txtUnidadeSaude.getText();

        // 2. Validação de campos obrigatórios
        if (name.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()
                || dataNasc.isEmpty() || cc.isEmpty() || utente.isEmpty() 
                || sexo.isEmpty() || nacionalidade.isEmpty() || naturalidade.isEmpty()
                || morada.isEmpty() || niss.isEmpty() || telemovel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios!");
            return;
        }

        // 3. Execução do Registo e Criação de Carteira (Assíncrono)
        RegistarButton.setEnabled(false); // Evita cliques múltiplos durante o processamento
        
        new Thread(() -> {
            try {
                // ALTERAÇÃO IMPORTANTE: 
                // Usamos SaudeWallet.create para garantir que tanto o Utilizador (User)
                // como a Carteira (Wallet) são criados e persistidos no disco simultaneamente.
                SaudeWallet.create(name, pass, dataNasc, cc, utente, sexo, 
                                 nacionalidade, naturalidade, morada, 
                                 niss, telemovel, tipoDeConta, unidade);
                
                // Atualizar a GUI na Thread de eventos do Swing
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Utilizador e Carteira de " + name + " criados com sucesso!");
                    RegistarButton.setEnabled(true);
                    
                         });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Erro no Registo/Carteira: " + ex.getMessage(), 
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    RegistarButton.setEnabled(true);
                });
            }
        }).start();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro ao processar formulário: " + e.getMessage());
    }

    }//GEN-LAST:event_RegistarButtonActionPerformed

    private void txtTelemovelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTelemovelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTelemovelActionPerformed

    /**
     * Ação do botão "Login" (incorretamente nomeado RegistarButton1).
     * <p>
     * Redireciona o utilizador de volta para a janela de Login.
     * @param evt O evento de clique.
     */
    private void RegistarButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegistarButton1ActionPerformed
         JanelaLogin Login =  new JanelaLogin();
         Login.setVisible(true);
         Login.setLocationRelativeTo(null);
         
         this.dispose(); // Fecha a janela de registo

    }//GEN-LAST:event_RegistarButton1ActionPerformed

    private void txtConfirmPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtConfirmPasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtConfirmPasswordActionPerformed

    /**
     * @param args the command line arguments
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
        java.awt.EventQueue.invokeLater(() -> new JanelaRegister().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton RegistarButton;
    private javax.swing.JButton RegistarButton1;
    private javax.swing.JComboBox<String> TipoConta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField txtCC;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtDataNascimento;
    private javax.swing.JTextField txtMorada;
    private javax.swing.JTextField txtNIF;
    private javax.swing.JTextField txtNISS;
    private javax.swing.JTextField txtNacionalidade;
    private javax.swing.JTextField txtNaturalidade;
    private javax.swing.JTextField txtNome;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtSexo;
    private javax.swing.JTextField txtTelemovel;
    private javax.swing.JTextField txtUnidadeSaude;
    private javax.swing.JTextField txtUtente;
    // End of variables declaration//GEN-END:variables
}