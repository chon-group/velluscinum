#! /bin/bash
bigchaindbNODE="http://testchain.chon.group:9984"
mkdir -p .jacamoCoin
echo "Preparing cryptocurrency to execute the example"
echo "Using $bigchaindbNODE as Node in BigchainDB Network"
echo " "
echo "Creating the Bank Of Agents and the JacamoCoin"
java -jar ../../lib/jacamo-1.1/libs/velluscinum.jar buildWallet .jacamoCoin/bankOfAgents
java -jar ../../lib/jacamo-1.1/libs/velluscinum.jar buildAsset  .jacamoCoin/JacamoCoin "cryptocurrency:JacamoCoin" "null:null"
java -jar ../../lib/jacamo-1.1/libs/velluscinum.jar deployToken $bigchaindbNODE .jacamoCoin/bankOfAgents.privkey .jacamoCoin/bankOfAgents.publkey .jacamoCoin/JacamoCoin.asset 100000

echo " "
echo "Transfering 50000 JacamoCoin to Giacomo Agent"
java -jar ../../lib/jacamo-1.1/libs/velluscinum.jar buildWallet .jacamoCoin/giacomoWallet 
JacamoCoinID=`cat .jacamoCoin/JacamoCoin.assetId`
java -jar ../../lib/jacamo-1.1/libs/velluscinum.jar transferToken $bigchaindbNODE .jacamoCoin/bankOfAgents.privkey .jacamoCoin/bankOfAgents.publkey $JacamoCoinID .jacamoCoin/giacomoWallet.publkey 10700

echo " "
echo "Updating beliefs about jacamoCoin"
mv src/agt/inc/common.asl src/agt/inc/common.asl.bkp
echo "jacamoCoin(\"$JacamoCoinID\"). /* AutoGenerated - consult startMAS.sh*/" > src/agt/inc/common.asl
echo "bigchaindbNode(\"$bigchaindbNODE\"). /* AutoGenerated - consult startMAS.sh*/" >> src/agt/inc/common.asl
cat src/agt/inc/common.asl.bkp | grep -v "AutoGenerated" >> src/agt/inc/common.asl
rm src/agt/inc/common.asl.bkp

echo " "
echo "Updating beliefs about Giacomo Wallet"
mv src/agt/giacomo.asl src/agt/giacomo.asl.bkp
GiacomoPrivateKey=`cat .jacamoCoin/giacomoWallet.privkey`
GiacomoPublickKey=`cat .jacamoCoin/giacomoWallet.publkey`
echo "myWallet(\"$GiacomoPrivateKey\",\"$GiacomoPublickKey\"). /* Auto generated GiacomoWallet - consult startMAS.sh*/" > src/agt/giacomo.asl
cat src/agt/giacomo.asl.bkp | grep -v "Auto generated GiacomoWallet" >> src/agt/giacomo.asl
rm src/agt/giacomo.asl.bkp

echo " "
echo "Executing the house_building jacamo example"
#Replace ant compiler by gradle
#java -jar ../../lib/jacamo-1.1/libs/ant-launcher-1.10.5.jar -f bin/house_building.xml
#Remove this file (this file may be left behind after compiling/running by ant)
if [ -f "velluscinum.lock" ] ; then
    rm velluscinum.lock
fi
#Run gradle (see the run task on build.gradle file)
./gradlew run