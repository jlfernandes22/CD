//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package SaudeCerteira;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * Created on 08/10/2025, 16:47:31
 *
 * @author manso - computer
 */
public class User implements Serializable{

    public static final String FILE_PATH = "data_user/";

    private String userName;
    private String dataNascimento;
    private String identidadeCC;
    private String numeroUtente;
    private String sexo;
    private String paisnacionalidade;
    private String naturalidade;
    private String morada;
    private String NISS;
    private String telemovel;
    private boolean medico;
    private String unidadeSaude;
    private PublicKey publicKey;
    transient private PrivateKey privateKey; // não gravar as chaves nas streams
    transient private Key aesKey; // não gravar as chaves nas streams

    protected User() {
        //construtor privado que so pode ser chamado na classe 
        new File(FILE_PATH).mkdirs();
    }

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

    public boolean isMedico() {
        return medico;
    }
    
    public String getUnidadeSaude(){
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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    
    
    
    

    public static User register(String name, String password, String dataNascimento, 
                                String identidadeCC, String numeroUtente, String sexo,
                                String paisnacionalidade, String naturalidade, String morada,
                                String NISS, String telemovel, boolean medico, String unidadeSaude) throws Exception {
        //verificar se o user já esta registado
        if( new File(FILE_PATH + name + ".pub").exists())
            throw new Exception("User already exists :" + name);
        
        User user = new User();
        user.userName = name;
        user.dataNascimento = dataNascimento;
        user.identidadeCC = identidadeCC;
        user.medico = medico;
        user.unidadeSaude = unidadeSaude;
        user.numeroUtente = numeroUtente;
        user.sexo = sexo;
        user.paisnacionalidade = paisnacionalidade;
        user.naturalidade = naturalidade;
        user.morada = morada;
        user.NISS = NISS;
        user.telemovel = telemovel;
        //gerar as chaves
        user.aesKey = SecurityUtils.generateAESKey(256);
        KeyPair kp = SecurityUtils.generateRSAKeyPair(2048);
        user.publicKey = kp.getPublic();
        user.privateKey = kp.getPrivate();
        //guardar a publica em claro
        Files.write(Path.of(FILE_PATH + name + ".pub"), user.publicKey.getEncoded());
        //encriptar a Key AES com a publica (que desaencripta com a privada)
        byte[] secretAes = SecurityUtils.encrypt(user.aesKey.getEncoded(), user.publicKey);
        Files.write(Path.of(FILE_PATH + name + ".aes"), secretAes);

        //encriptar a privada com a password
        byte[] secretPriv = SecurityUtils.encrypt(user.privateKey.getEncoded(), password);
        Files.write(Path.of(FILE_PATH + name + ".priv"), secretPriv);
        
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH + name + ".user"))) {
            out.writeObject(user);
        }

        return user;
    }

    public static User login(String name, String pass) throws Exception {
        User user;
        
        File userFile = new File(FILE_PATH + name + ".user");
        if (userFile.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(userFile))) {
                user = (User) in.readObject();
            }
        } else {
            // Fallback: se não houver ficheiro .user, cria um vazio (mas perde dados pessoais)
            user = new User();
            user.userName = name;
        }
        user.userName = name;
        //ler a chave privada
        byte[] secretPriv = Files.readAllBytes(Path.of(FILE_PATH + name + ".priv"));
        //desencriptar com a password
        byte[] plainPriv = SecurityUtils.decrypt(secretPriv, pass);
        user.privateKey = SecurityUtils.getPrivateKey(plainPriv);
        //ler a AES
        byte[] secretAes = Files.readAllBytes(Path.of(FILE_PATH + name + ".aes"));
        //desencriptar com a chave privada
        byte[] plainAes = SecurityUtils.decrypt(secretAes, user.privateKey);
        user.aesKey = SecurityUtils.getAESKey(plainAes);
        //ler a publica
        byte[] plainPub = Files.readAllBytes(Path.of(FILE_PATH + name + ".pub"));
        user.publicKey = SecurityUtils.getPublicKey(plainPub);
        return user;
    }

    public static User login(String name) throws Exception {
        User user ;
        
        // --- NOVO: TENTAR LER OS DADOS PESSOAIS DO DISCO ---
        File userFile = new File(FILE_PATH + name + ".user");
        if (userFile.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(userFile))) {
                user = (User) in.readObject();
            }
        } else {
            user = new User();
            user.userName = name;
        }
        
        
        //ler a publica
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
    
    public static void deleteAllUsers() throws Exception{
        FolderUtils.cleanFolder(FILE_PATH, true);
        
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081647L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::

    

}
