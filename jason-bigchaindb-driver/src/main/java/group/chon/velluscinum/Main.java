package group.chon.velluscinum;

import com.bigchaindb.exceptions.TransactionNotFoundException;
import group.chon.velluscinum.model.WalletContent;
import net.i2p.crypto.eddsa.*;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        String op = null;
        try {
            op = args[0];
            if(op.equals("createKeys")) {createKeys(args);}
            else if(op.equals("newAsset")) {newAsset(args);}
            else if(op.equals("newTransfer")) {newTransfer(args);}
        } catch (Exception ex) {
            Info driver = new Info();
            BufferedReader buffered = new BufferedReader(driver.getMANUAL());
            String line = buffered.readLine();
            while(line != null){
                System.out.println(line);
                line = buffered.readLine();
            }
            buffered.close();
            System.exit(0);
        }
    }

    private static void createKeys(String[] args){
        String output = null;
        KeyManagement keyManagement = new KeyManagement();
        KeyPair bobKeyPair 	= keyManagement.newKey();
        EdDSAPrivateKey bobPrivateKey = (EdDSAPrivateKey) bobKeyPair.getPrivate();
        EdDSAPublicKey bobPublicKey  = (EdDSAPublicKey)  bobKeyPair.getPublic();
        keyManagement.exportPrivateKeyToFile(bobPrivateKey, args[1]+".private");
        keyManagement.exportPublicKeyToFile(bobPublicKey, args[1]+".public");
        System.exit(0);
    }

    private static void newAsset(String[] args) throws Exception {
        JasonBigchaindbDriver bigchaindb4Jason = new JasonBigchaindbDriver();
        KeyManagement keyManagement = new KeyManagement();
        //[SERVER] [PRIVATEKEY-FILE] [PUBLICKEY-FILE] [ASSET]
        String			 serverURL	= args[1];
        bigchaindb4Jason.setConfig(serverURL);
        EdDSAPrivateKey  privateKey = keyManagement.importPrivateKeyFromFile(args[2]);
        EdDSAPublicKey   publicKey  = keyManagement.importPublicKeyFromFile(args[3]);
        JSONObject jSONAsset	= bigchaindb4Jason.importAssetFromFile(args[4]);
        bigchaindb4Jason.newAsset(jSONAsset, privateKey, publicKey);
        System.exit(0);
    }

    private static void newTransfer(String[] args){
        JasonBigchaindbDriver bigchaindb4Jason = new JasonBigchaindbDriver();
        KeyManagement keyManagement = new KeyManagement();
        //[SERVER] [PRIVATEKEY-FILE] [PUBLICKEY-FILE] [ASSET-ID] [METADATA] [PUBLIC_DEST_KEY-FILE]
        String			 serverURL	= args[1];
        bigchaindb4Jason.setConfig(serverURL);
        EdDSAPrivateKey  privateKey = keyManagement.importPrivateKeyFromFile(args[2]);
        EdDSAPublicKey   publicKey  = keyManagement.importPublicKeyFromFile(args[3]);
        String 			 assetID = args[4];
        JSONObject		 jSONAsset	= bigchaindb4Jason.importAssetFromFile(args[5]);
        EdDSAPublicKey   destinatorPublicKey  = keyManagement.importPublicKeyFromFile(args[6]);
        bigchaindb4Jason.newTransfer(privateKey, publicKey, assetID, jSONAsset, destinatorPublicKey);
        System.exit(0);
    }

    private static String test0() {
        //Creating an Asset
        Info driver = new Info();
        JasonBigchaindbDriver bigchaindb4Jason = new JasonBigchaindbDriver();
        String asset = "{\n"
                + "\"asset\":[{\n"
                + "	\"Description\": \"My first Asset in BigChainDB\"\n"
                + "}], \"metadata\":[{\n"
                + "	\"Hello\": \"World\"\n"
                + "  }]\n"
                + "}";
        return bigchaindb4Jason.newAsset(
                driver.getDefaultServer(),
                driver.getBobPrivateKey(),
                driver.getBobPublicKey(),
                asset
                );
    }

    private static String test1(){
        String assetID = test0();
        //Transferring an Asset
        JasonBigchaindbDriver bigchaindb4Jason = new JasonBigchaindbDriver();
        Info driver = new Info();
        String server = driver.getDefaultServer();
        String ownerPrivateKey = driver.getBobPrivateKey();
        String ownerPublicKey  = driver.getBobPublicKey();
        String recipientPublicKey = driver.getAlicePublickey();
        String newMetadata = "{\n"
                + "  \"metadata\":[{\n"
                + "	\"New Owner\": \"Alice\"\n"
                + "  }]\n"
                + "}";

        return bigchaindb4Jason.newTransfer(server,ownerPrivateKey,ownerPublicKey,assetID,newMetadata,recipientPublicKey);
    }

    private static String test2() throws Exception {
        //Creating a fungible token
        Info driver = new Info();
        KeyManagement keyManagement = new KeyManagement();
        EdDSAPublicKey bankPublicKey = keyManagement.importPublicKeyFromBase64(driver.getBobPublicKey());
        EdDSAPrivateKey bankPrivateKey = keyManagement.importPrivateKeyFromBase64(driver.getBobPrivateKey());
        Vellus vellus = new Vellus();
        vellus.setConfig(driver.getDefaultServer());
        String idMoeda = vellus.createFungibleToken(bankPrivateKey,bankPublicKey,"ChonCoin", 9000000000000000000L);
        return  idMoeda;
    }

    private static String test3() throws Exception {
        String fungibleToken =  test2();
        Info driver = new Info();
        KeyManagement keyManagement = new KeyManagement();
        EdDSAPublicKey bankPublicKey = keyManagement.importPublicKeyFromBase64(driver.getBobPublicKey());
        EdDSAPrivateKey bankPrivateKey = keyManagement.importPrivateKeyFromBase64(driver.getBobPrivateKey());
        EdDSAPrivateKey  clientPrivateKey = keyManagement.importPrivateKeyFromBase64(driver.getAlicePrivateKey());
        EdDSAPublicKey  clientPublicKey = keyManagement.importPublicKeyFromBase64(driver.getAlicePublickey());
        Vellus vellus = new Vellus();
        vellus.setConfig(driver.getDefaultServer());

        //bank to client
        vellus.transferToken(bankPrivateKey,bankPublicKey,fungibleToken,clientPublicKey,1);
        vellus.transferToken(bankPrivateKey,bankPublicKey,fungibleToken,clientPublicKey,2);
        vellus.transferToken(bankPrivateKey,bankPublicKey,fungibleToken,clientPublicKey,3);
        vellus.transferToken(bankPrivateKey,bankPublicKey,fungibleToken,clientPublicKey,4);

        //client to bank
        vellus.transferToken(clientPrivateKey,clientPublicKey,fungibleToken,bankPublicKey,5);
        vellus.transferToken(clientPrivateKey,clientPublicKey,fungibleToken,bankPublicKey,5);


        return null;
    }

    private static ArrayList<WalletContent> test4() throws TransactionNotFoundException, IOException {

        Info driver = new Info();
        KeyManagement keyManagement = new KeyManagement();
        Wallet wallet = new Wallet();

        EdDSAPrivateKey bobPrivateKey = keyManagement.importPrivateKeyFromBase64(driver.getBobPrivateKey());
        EdDSAPublicKey bobPublicKey = keyManagement.importPublicKeyFromBase64(driver.getBobPublicKey());

        wallet.setConfig(driver.getDefaultServer());
        ArrayList<WalletContent> walletContents= wallet.getMyTokens(bobPrivateKey,bobPublicKey);

        for(int i =0; i<walletContents.size(); i++){
            System.out.println(walletContents.get(i).getToken()+
                    " "+walletContents.get(i).getAmount()+
                    " "+walletContents.get(i).getTransaction());
        }
        return walletContents;
    }
}