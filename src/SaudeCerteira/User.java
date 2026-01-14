package SaudeCerteira;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import utils.FolderUtils;
import utils.SecurityUtils;

/**
 * Representa um Utilizador (Identidade Digital) no sistema SaúdeCerteira.
 * <p>
 * Esta classe gere:
 * <ul>
 * <li><b>Dados Pessoais:</b> Nome, CC, NISS, Role, etc.</li>
 * <li><b>Criptografia:</b> Par de chaves RSA (Identidade) e chave AES
 * (Sessão/Dados).</li>
 * <li><b>Persistência:</b> Gravação segura das chaves em disco.</li>
 * </ul>
 * * <b>Estrutura de Ficheiros por Utilizador:</b>
 * <ul>
 * <li><code>nome.pub</code>: Chave Pública (Acessível a todos).</li>
 * <li><code>nome.priv</code>: Chave Privada (Cifrada com a Password do
 * utilizador).</li>
 * <li><code>nome.aes</code>: Chave AES (Cifrada com a Chave Pública do próprio
 * utilizador).</li>
 * <li><code>nome.user</code>: Objeto Java serializado com os dados
 * biográficos.</li>
 * </ul>
 * Created on 08/10/2025, 16:47:31
 *
 * @author aluno_25979, aluno_25946
 */
public class User implements Serializable {

    public static final String FILE_PATH = "data_user/";

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: DADOS BIOGRÁFICOS E PAPEL (ROLE)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Papel do utilizador no sistema (ex: "Médico", "Utente", "Farmacêutico").
     */
    String role;

    public String userName;
    public String dataNascimento;
    private String identidadeCC;
    public String numeroUtente;
    private String sexo;
    private String paisnacionalidade;
    private String naturalidade;
    private String morada;
    private String NISS;
    private String telemovel;
    private String unidadeSaude;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: SEGURANÇA E CHAVES
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private PublicKey publicKey;

    /**
     * * Chave Privada RSA.
     * <p>
     * <b>Transient:</b> Não é serializada automaticamente no ficheiro .user por
     * segurança. É carregada apenas mediante password correta.
     */
    transient private PrivateKey privateKey;

    /**
     * * Chave Simétrica AES.
     * <p>
     * <b>Transient:</b> Não é serializada automaticamente. Usada para encriptar
     * dados volumosos (ex: conteúdo das receitas).
     */
    public transient Key aesKey;

    /**
     * Construtor privado. Garante que a criação de utilizadores passa pelo
     * método estático {@link #register}. Cria a diretoria de dados se não
     * existir.
     */
    protected User() {
        new File(FILE_PATH).mkdirs();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS E SETTERS
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Key getAesKey() {
        return aesKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getIdentidadeCC() {
        return identidadeCC;
    }

    public String getUnidadeSaude() {
        return unidadeSaude;
    }

    public String getNumeroUtente() {
        return numeroUtente;
    }

    public String getSexo() {
        return sexo;
    }

    public String getPaisnacionalidade() {
        return paisnacionalidade;
    }

    public String getNaturalidade() {
        return naturalidade;
    }

    public String getMorada() {
        return morada;
    }

    public String getNISS() {
        return NISS;
    }

    public String getTelemovel() {
        return telemovel;
    }

    /**
     * @return Versão da serialização para controlo de compatibilidade.
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: VERIFICAÇÃO DE PAPÉIS (ROLES)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    public boolean isMedico() {
        return "Médico".equals(this.role);
    }

    public boolean isFarmaceutico() {
        return "Farmacêutico".equals(this.role); // Corrigido para incluir acentos se necessário
    }

    public String getRole() {
        return this.role;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: REGISTO DE UTILIZADOR
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Regista um novo utilizador no sistema. Gera todas as chaves
     * criptográficas e guarda os ficheiros em disco.
     *
     * @param name Nome de utilizador (Login).
     * @param password Password para encriptar a chave privada.
     * @param dataNascimento Data de nascimento.
     * @param identidadeCC Número do Cartão de Cidadão.
     * @param numeroUtente Número de Utente de Saúde.
     * @param sexo Género.
     * @param paisnacionalidade Nacionalidade.
     * @param naturalidade Naturalidade.
     * @param morada Morada completa.
     * @param NISS Número de Segurança Social.
     * @param telemovel Contacto telefónico.
     * @param role Papel no sistema (Médico/Utente/Farmacêutico).
     * @param unidadeSaude Local de afiliação.
     * @return O objeto User criado com as chaves geradas.
     * @throws Exception Se o utilizador já existir ou erro na geração de
     * chaves.
     */
    public static User register(String name, String password, String dataNascimento,
            String identidadeCC, String numeroUtente, String sexo,
            String paisnacionalidade, String naturalidade, String morada,
            String NISS, String telemovel, String role, String unidadeSaude) throws Exception {

        // 1. Verificar duplicação
        if (new File(FILE_PATH + name + ".pub").exists()) {
            throw new Exception("User already exists :" + name);
        }

        // 2. Preencher dados biográficos
        User user = new User();
        user.userName = name;
        user.dataNascimento = dataNascimento;
        user.identidadeCC = identidadeCC;
        user.role = role;
        user.unidadeSaude = unidadeSaude;
        user.numeroUtente = numeroUtente;
        user.sexo = sexo;
        user.paisnacionalidade = paisnacionalidade;
        user.naturalidade = naturalidade;
        user.morada = morada;
        user.NISS = NISS;
        user.telemovel = telemovel;

        // 3. Gerar Chaves Criptográficas
        // AES: Chave simétrica de 256 bits para dados
        user.aesKey = SecurityUtils.generateAESKey(256);
        // RSA: Par de chaves assimétricas de 2048 bits para identidade
        KeyPair kp = SecurityUtils.generateRSAKeyPair(2048);
        user.publicKey = kp.getPublic();
        user.privateKey = kp.getPrivate();

        // 4. Persistir Chaves de forma Segura
        // A. Chave PÚBLICA (Guardada em claro - Todos podem ler)
        Files.write(Path.of(FILE_PATH + name + ".pub"), user.publicKey.getEncoded());

        // B. Chave AES (Envelope Digital Pessoal)
        // Cifrada com a própria chave pública do utilizador.
        // Assim, só ele (com a privada) a poderá recuperar no login.
        byte[] secretAes = SecurityUtils.encrypt(user.aesKey.getEncoded(), user.publicKey);
        Files.write(Path.of(FILE_PATH + name + ".aes"), secretAes);

        // C. Chave PRIVADA (Segurança Máxima)
        // Cifrada simetricamente com a PASSWORD do utilizador.
        byte[] secretPriv = SecurityUtils.encrypt(user.privateKey.getEncoded(), password);
        Files.write(Path.of(FILE_PATH + name + ".priv"), secretPriv);

        // 5. Persistir Objeto User (Metadados)
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH + name + ".user"))) {
            out.writeObject(user);
        }

        return user;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: LOGIN E CARREGAMENTO
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Login Completo (Autenticação). Carrega o utilizador e desbloqueia as
     * chaves privadas usando a password.
     *
     * @param name Nome de utilizador.
     * @param pass Password para desencriptar a chave privada.
     * @return O utilizador autenticado com todas as chaves carregadas.
     * @throws Exception Se a password estiver incorreta ou ficheiros
     * corrompidos.
     */
    public static User login(String name, String pass) throws Exception {
        User user;

        // 1. Carregar Metadados (.user)
        File userFile = new File(FILE_PATH + name + ".user");
        if (userFile.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(userFile))) {
                user = (User) in.readObject();
            }
        } else {
            // Fallback: Se não houver .user, cria estrutura básica (não recomendado)
            user = new User();
            user.userName = name;
        }
        user.userName = name; // Garante consistência

        // 2. Recuperar Chave PRIVADA
        // Lê os bytes cifrados do disco
        byte[] secretPriv = Files.readAllBytes(Path.of(FILE_PATH + name + ".priv"));
        // Tenta desencriptar com a password fornecida
        byte[] plainPriv = SecurityUtils.decrypt(secretPriv, pass);
        // Reconstrói o objeto PrivateKey
        user.privateKey = SecurityUtils.getPrivateKey(plainPriv);

        // 3. Recuperar Chave AES
        // Lê os bytes cifrados (Envelope Digital)
        byte[] secretAes = Files.readAllBytes(Path.of(FILE_PATH + name + ".aes"));
        // Desencripta usando a chave privada recém-recuperada
        byte[] plainAes = SecurityUtils.decrypt(secretAes, user.privateKey);
        user.aesKey = SecurityUtils.getAESKey(plainAes);

        // 4. Recuperar Chave PÚBLICA (Em claro)
        byte[] plainPub = Files.readAllBytes(Path.of(FILE_PATH + name + ".pub"));
        user.publicKey = SecurityUtils.getPublicKey(plainPub);

        return user;
    }

    /**
     * Login Público (Lookup). Carrega apenas os dados públicos do utilizador
     * (Perfil e Chave Pública). Usado para procurar outros utilizadores na rede
     * para lhes enviar mensagens.
     *
     * @param name Nome do utilizador a pesquisar.
     * @return O utilizador carregado (sem chave privada e sem chave AES).
     * @throws Exception Se o utilizador não existir.
     */
    public static User login(String name) throws Exception {
        User user;

        // 1. Carregar Metadados
        File userFile = new File(FILE_PATH + name + ".user");
        if (userFile.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(userFile))) {
                user = (User) in.readObject();
            }
        } else {
            user = new User();
            user.userName = name;
        }

        // 2. Carregar Apenas Chave PÚBLICA
        byte[] plainPub = Files.readAllBytes(Path.of(FILE_PATH + name + ".pub"));
        user.publicKey = SecurityUtils.getPublicKey(plainPub);

        return user;
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(userName);
        txt.append("\npub ").append(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        if (privateKey != null) {
            txt.append("\npriv ").append(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            txt.append("\nAES ").append(Base64.getEncoder().encodeToString(aesKey.getEncoded()));
        }
        return txt.toString();
    }

    /**
     * Apaga todos os utilizadores do sistema (Hard Reset). Cuidado: Apaga a
     * pasta data_user.
     */
    public static void deleteAllUsers() throws Exception {
        FolderUtils.cleanFolder(FILE_PATH, true);
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081647L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::
}
