// Environment code for project cozinheiroEcomilao.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;
import group.chon.velluscinum.*;

public class Env extends Environment {

    private Logger logger = Logger.getLogger("cozinheiroEcomilao.mas2j."+Env.class.getName());
	private JasonBigchaindbDriver bigchaindb4Jason = new JasonBigchaindbDriver();

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
		if(action.toString().substring(0,14).equals("gerarCarteira(")){
			String server = action.getTerm(0).toString().replace("\"", "");
			String privateKey = action.getTerm(1).toString().replace("\"", "");
			String publicKey  = action.getTerm(2).toString().replace("\"", "");
			String bancoPublicKey = action.getTerm(3).toString().replace("\"", "");
			String asset = "{\n"
				+ "	\"metadata\": [{\n"
				+ "	  \"Saldo Inicial\": \"0\"\n"
				+ "	}],\n"
				+ "	\"asset\": [{\n"
				+ "	  \"Objeto\": \"Carteira\",\n"
				+ "	  \"Agente\" : \""+agName+"\"\n"
				+ "	}]\n"
				+ "}";
			String assetID = bigchaindb4Jason.newAsset(server,privateKey,publicKey,asset);
			addPercept(agName, Literal.parseLiteral("myWallet(\""+assetID+"\")"));
			String registro = "{\n"
				+ "	\"metadata\": [{\n"
				+ "	  \"Descricao\": \"Abertura de Conta Bancaria\"\n"
				+ "	}]"
				+ "}";
			String transferID = bigchaindb4Jason.newTransfer(server,privateKey,publicKey,assetID,registro,bancoPublicKey);
			addPercept(agName, Literal.parseLiteral("protocolo(\""+transferID+"\")"));
			
		}else if(action.toString().substring(0,24).equals("consultaUltimaTransacao(")){
				String server = action.getTerm(0).toString().replace("\"", "");
				String ativo  = action.getTerm(1).toString().replace("\"", "");
				String transactionID = bigchaindb4Jason.getTransactionIDFromAsset(server, ativo, -1);
				addPercept(agName, Literal.parseLiteral("transacaoValida(\""+ativo+"\",\""+transactionID+"\")"));
				
		}else if(action.toString().substring(0,11).equals("abrirConta(")){
			
				String server = action.getTerm(0).toString().replace("\"", "");
				String privateKey = action.getTerm(1).toString().replace("\"", "");
				String publicKey  = action.getTerm(2).toString().replace("\"", "");
				String carteira   = action.getTerm(3).toString().replace("\"", "");

				Integer valor = Integer.parseInt(action.getTerm(4).toString());
				
				String registro = "{\n"
				+ "	\"metadata\": [{\n"
				+ "		\"Descricao\": \"Abertura de Conta\",\n"
				+ "		\"SaldoAnterior\": \"0\",\n"
				+ "		\"Operacao\": \"Credito\",\n"
				+ "		\"Valor\": \""+valor+"\",\n"
				+ "		\"SaldoAtual\": \""+valor+"\"\n"
				+ "	}]\n"
				+ "}";	
				
				bigchaindb4Jason.newTransfer(server,privateKey,publicKey,carteira,registro,publicKey);
		}else if(action.toString().substring(0,15).equals("consultarSaldo(")){
			String server = action.getTerm(0).toString().replace("\"", "");
			String carteira   = action.getTerm(1).toString().replace("\"", "");
			
			String Saldo = bigchaindb4Jason.getFieldOfTransactionFromAssetID(server, carteira, "SaldoAtual", -1);

			addPercept(agName, Literal.parseLiteral("saldo(\""+carteira+"\","+Saldo+")"));

		}else if(action.toString().substring(0,14).equals("transferirPix(")){
			String server 	= action.getTerm(0).toString().replace("\"", "");
			String privateK	= action.getTerm(1).toString().replace("\"", "");
			String publicK 	= action.getTerm(2).toString().replace("\"", "");
			String cOrigem  = action.getTerm(3).toString().replace("\"", "");
			String cDestino = action.getTerm(4).toString().replace("\"", "");
			String Valor  	= action.getTerm(5).toString().replace("\"", "");
			String Controle	= action.getTerm(6).toString().replace("\"", "");
			
			/*Atualizando Saldo da Origem*/
			String SaldoOrigem = bigchaindb4Jason.getFieldOfTransactionFromAssetID(server, cOrigem, "SaldoAtual", -1);
			Integer NovoSaldoAtualOrigem = Integer.parseInt(SaldoOrigem)-Integer.parseInt(Valor);
			
			String registro = "{\n"
				+ "	\"metadata\": [{\n"
				+ "		\"Descricao\": \"Pix para: "+cDestino+"\",\n"
				+ "		\"SaldoAnterior\": \""+SaldoOrigem+"\",\n"
				+ "		\"Operacao\": \"Debito\",\n"
				+ "		\"Valor\": \""+Valor+"\",\n"
				+ "		\"SaldoAtual\": \""+NovoSaldoAtualOrigem+"\"\n"
				+ "	}]\n"
				+ "}";
			bigchaindb4Jason.newTransfer(server,privateK,publicK,cOrigem,registro,publicK);
			
			/*Atualizando Saldo do Destino*/
			String SaldoDestino = bigchaindb4Jason.getFieldOfTransactionFromAssetID(server, cDestino, "SaldoAtual", -1);
			Integer NovoSaldoAtualDestino = Integer.parseInt(SaldoDestino)+Integer.parseInt(Valor);
			registro = "{\n"
				+ "	\"metadata\": [{\n"
				+ "		\"Descricao\": \"Pix recebido de: "+cOrigem+"\",\n"
				+ "		\"SaldoAnterior\": \""+SaldoDestino+"\",\n"
				+ "		\"Operacao\": \"Credito\",\n"
				+ "		\"Valor\": \""+Valor+"\",\n"
				+ "		\"SaldoAtual\": \""+NovoSaldoAtualDestino+"\"\n"
				+ "	}]\n"
				+ "}";
			String comprovante = bigchaindb4Jason.newTransfer(server,privateK,publicK,cDestino,registro,publicK);
			
			addPercept(agName, Literal.parseLiteral("pix("+Controle+",\""+comprovante+"\",\""+cOrigem+"\",\""+cDestino+"\","+Valor+")"));

			
		}else{
			logger.info("executing: "+action+", but not implemented!");
		}
        return true;
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
