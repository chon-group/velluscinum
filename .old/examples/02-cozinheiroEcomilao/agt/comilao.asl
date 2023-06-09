// Agent comilao in project cozinheiroEcomilao.mas2j
/* Crenças Iniciais */
estoqueComida(10).
energia(10).

/* Objetivos Iniciais */
!curtir.

/* Planos */
+!pedirEmprestimo: cryptocurrency(Coin) & bankWallet(BankW) & chainServer(Server) & myWallet(MyPriv,MyPub)<-
	.print("Solicitando crédito");
	.deployNFT(Server,MyPriv,MyPub,
				"name:comilao;address:5362fe5e-aaf1-43e6-9643-7ab094836ff4",
				"description:Criação de conta bancária",
				contaID);
				
	.wait(contaID(AssetID));
	.transferNFT(Server,MyPriv,MyPub,AssetID,BankW,
				 "description:solicitação de empréstimo;valor_chainCoin:10",protocoloID);
	.wait(protocoloID(PP));
	
	.print("protocolo nr:",PP);
	.send(banco,achieve,solicitacaoEmprestimo(PP,MyPub,10)).
	
	
+!comer: sintoFome & estoqueComida(C) & C>3 & energia(E) & E<=10 <-
	-+estoqueComida(C-3);
	-+energia(E+3);
	.print("Comendo........ [Geladeira=",C-3,"] [Energia=",E+3,"]");
	.wait(1000);
	!comer.
	
+!comer: energia(E) & E>10	<-
	.print("Satisfeito...... [Energia=",E,"]");
	-sintoFome;
	!curtir.

+!comer <-
	?estoqueComida(X);
	.print("Preciso pedir comida..... [Geladeira=",X,"]");
	.wait(1000);
	!pedirComida.

+!curtir: energia(E) & E>5 <-
	.print("Curtindo....");
	.wait(1000);
	-+energia(E-3);
	-sintoFome;
	!curtir.

+!curtir: energia(E) & E<=5<-
	.print("Sem Energia..... [Energia=",E,"]");
	+sintoFome;
	!comer.

+!pedirComida: contaBancaria(ok)[source(banco)] & cryptocurrency(Coin) 
			& chainServer(Server) & myWallet(MyPriv,MyPub) 
			& cozinheiroWallet(Cozinheiro)<-
			
	.print("Pedindo Comida.....");
	.transferToken(Server,MyPriv,MyPub,Coin,Cozinheiro,5,pix);
	.wait(pix(CodPix));
	.send(cozinheiro,achieve,pedido(lanche,5,CodPix));
	.wait(5000);
	!comer.

+!pedirComida: not contaBancaria(ok)[source(banco)] <-
	.wait(5000);
	.buildWallet(myWallet);
	!pedirEmprestimo;
	.wait(5000);
	.send(cozinheiro,askOne,cozinheiroWallet(Cozinheiro),Reply);
	+Reply;
	!pedirComida.
	
-!pedirComida: cryptocurrency(Coin) & chainServer(Server) 
		& myWallet(MyPriv,MyPub) <-
		.tokenBalance(Server,MyPriv,MyPub,Coin,saldo).	

+entregaComida(Product,Qtd)[source(Entregador)] <-
	?estoqueComida(X);
	-+estoqueComida(X+Qtd);
	.print("Oba! Chegou comida!!!!");
	-entregaComida(Product,Qtd)[source(Entregador)].
	
+saldo(T,V)[source(self)]: cryptocurrency(Coin)[source(banco)] & V<5 & Coin==T <-
	.drop_desire(curtir);
	.print("Ababou a festa :(");
	.wait(5000);
	.stopMAS.