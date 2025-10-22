/*****************************************************************
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 16/09/2025
* Ultima alteracao.: 27/09/2025
* Nome.............: CamadaEnlaceDadosTransmissora
* Funcao...........: Transfere a mensagem enquadrada para camada fisica transmissora
*************************************************************** */
package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;

public class CamadaEnlaceDadosTransmissora {
  /**************************************************************
  * Metodo: CamadaEnlaceDadosTransmissora
  * Funcao: enquadra os bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  public CamadaEnlaceDadosTransmissora(int []quadro) {
  // O 'quadro' aqui eh a mensagem inteira (array de ints)
    // Vamos iterar sobre cada 'int' e trata-lo como um subquadro de dados.
    
    for (int i = 0; i < quadro.length; i++) {
        if (quadro[i] == 0) continue; // Pular bytes de padding no final

        // O payload do nosso subquadro eh 1 int (4 bytes)
        final int[] subquadroPayload = new int[] { quadro[i] };

        StringBuilder threadName = new StringBuilder("Thread-Subquadro-");
        threadName.append(i);

        // Requisito 2 e 3: Dispara uma nova Thread dedicada para este subquadro
        // O transmissor nao espera (pipeline)
        Thread threadDoSubquadro = new Thread(() -> {
            // 1. Enquadra e Controla ESTE subquadro
            int[] subquadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramento(subquadroPayload);
            int[] subquadroControlado = CamadaDeEnlaceTransmissoraControleDeErro(subquadroEnquadrado);
            CamadaDeEnlaceTransmissoraControleDeFluxo(subquadroControlado);

            // 2. Cria os objetos de comunicacao para este ciclo
            // CamadaFisicaTransmissora agora tem metodos para envio e recebimento de ACK
            CamadaFisicaTransmissora transmissorFisico = new CamadaFisicaTransmissora();
            
            // MeioDeComunicacao agora eh um "onibus" que conecta os dois lados
            MeioDeComunicacao meio = new MeioDeComunicacao();

            // 3. Linka os objetos
            transmissorFisico.setMeio(meio); // T -> M
            meio.setTransmissor(transmissorFisico); // M -> T (para ACKs)
            // (O Meio cria sua propria instancia de CamadaFisicaReceptora)

            // 4. Inicia o envio do subquadro
            transmissorFisico.enviarSubquadro(subquadroControlado);
            
            // A thread morre apos o envio (e eventual recebimento de ACK)
        }, threadName.toString());
        
        threadDoSubquadro.start();
    } // Fim do for (pipeline)
  } //Fim do metodo (construtor)
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramento
  * Funcao: enquadra os bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadroEnquadrado 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramento(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    int tipoDeEnquadramento = auxiliar.enquadCodification(controller.getEnquadramento()); // enquadramento escolhido
    int[] quadroEnquadrado; // quadro depois de ser enquadrado
    // Switch para escolher a codificacao escolhida
    switch(tipoDeEnquadramento) {
      case 0: //contagem de caracteres
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: //insercao de bytes
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: //insercao de bits
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: //violacao da camada fisica
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica(quadro);
        break;
      default:
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
    } // Fim do switch

    return quadroEnquadrado;
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraControleDeErro
  * Funcao: faz o controle de erros dos bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraControleDeErro(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    int tipoDeControle = auxiliar.controlCodification(controller.getControleErro()); // pega o controle de erro escolhido
    int[] quadroControlado; // quadro depois de passar pelo controle de erros
    //switch para pegar o controle de erro 
    switch(tipoDeControle) {
      case 0: //bit de paridade par
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroBitParidadePar(quadro);
        break;
      case 1: //bit de paridade impar
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroBitParidadeImpar(quadro);
        break;
      case 2: //CRC
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroCRC(quadro);
        break;
      case 3: //codigo de hamming
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroCodigoDeHamming(quadro);
        break;
      default:
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroBitParidadePar(quadro);
        break;
    } // Fim do switch

    return quadroControlado;
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraControleDeFluxo
  * Funcao: faz o controle de fluxo dos bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  private static void CamadaDeEnlaceTransmissoraControleDeFluxo(int[] quadro) {
    return;
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramentoContagemDeCaracteres
  * Funcao: enquadra os bits (Contagem de Caracteres) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadroEnquadrado 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    // quantos caracteres vamos contar, por agora, sera em bytes, mas a funcao manipula bits
    final int caracteres = 4;
    // quanto o cabecalho ocupa, 1 byte, ou seja, 8 bits
    final int cabecalho = 1;
    // valor do controle
    final int controle = caracteres + cabecalho;
    // agora sim, tudo em bits
    final int frameBits = controle * 8;

    //calcula o tamanho necessario para o novo array
    int frames = quadro.length;
    int tamanhoBits = frames * frameBits;
    int tamanhoInts = (tamanhoBits + 31) / 32;
    int[] quadroEnquadrado = new int[tamanhoInts];
    // ponteiro que escreve o bit
    int ptr = 0;
    // for para aplicarmos a contagem de caracteres
    for(int i = 0; i < quadro.length; i++) {
      //escreve 5
      int carga = quadro[i];
      auxiliar.escreverBits(quadroEnquadrado, ptr, controle, 8);
      ptr += 8; // avanca o ponteiro
      //escreve o controle depois de escrever o cabecalho (apos os 4 caracteres)
      auxiliar.escreverBits(quadroEnquadrado, ptr, carga, 32);
      ptr += 32;
    } // fim do for

    return quadroEnquadrado; //retorno da funcao
  } // fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramentoInsercaoDeBytes
  * Funcao: enquadra os bits (Insercao de Bytes) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBytes(int[] quadro) {
    final int FLAG = 0b01111110; // ~ em ASCII
    final int ESC = 0b01111101; // } em ASCII
    final int TAMANHO_SUBQUADRO_BYTES = 4; // de quantos em quantos bytes vai adicionar o flag

    //calcular o tamanho do array
    int tamanhoFinal = 2; // comeca em 2 por causa das FLAGs
    int contadorByteFlagIntermediaria = 0; // Contador para adicionar a flag intermediaria
    // for para desempacotar o inteiro
    for(int inteiroBruto : quadro) {
      if(inteiroBruto == 0) break;
      // Manipula os bits para analise
      int[] intBit = new int[4];
      // realiza as operacoes bitwise para manipular o inteiro e transformar em bits
      intBit[0] = (inteiroBruto >> 24) & 0xFF;
      intBit[1] = (inteiroBruto >> 16) & 0xFF;
      intBit[2] = (inteiroBruto >> 8) & 0xFF;
      intBit[3] = (inteiroBruto) & 0xFF;

      //for para calcular o tamanho do array
      for(int intBitAtual : intBit) {
        if(intBitAtual == 0) continue; //ignora bytes "nulos"
        //if para ver em quanto vai aumentar
        if(intBitAtual == FLAG || intBitAtual == ESC) {
          tamanhoFinal += 2;
        } else {
          tamanhoFinal++;
          contadorByteFlagIntermediaria++;
        } // fim do if

        // if para adicioar espaco para FLAG intermediaria
        if (contadorByteFlagIntermediaria == TAMANHO_SUBQUADRO_BYTES) {
          tamanhoFinal++; // Adiciona espaco para a FLAG intermediaria
          contadorByteFlagIntermediaria = 0; // Zera o contador
        } // fim do if
      } // fim do for
    } // fim do for

    int[] enquadrado = new int[tamanhoFinal];
    int ptr = 0;
    enquadrado[ptr++] = FLAG; // adiciona a FLAG de inicio

    contadorByteFlagIntermediaria = 0;

    //preencher o array
    //for para pegar os bits
    for(int inteiroBruto : quadro) {
      if(inteiroBruto == 0) break;
      // Manipula os bits para analise
      int[] intBit = new int[4];
      // realiza as operacoes bitwise para manipular o inteiro e transformar em bits
      intBit[0] = (inteiroBruto >> 24) & 0xFF;
      intBit[1] = (inteiroBruto >> 16) & 0xFF;
      intBit[2] = (inteiroBruto >> 8) & 0xFF;
      intBit[3] = (inteiroBruto) & 0xFF;

      //for para calcular o tamanho do array
      for(int intBitAtual : intBit) {
        if(intBitAtual == 0) continue; //ignora bytes "nulos"
        //if para ver em quanto vai aumentar
        if(intBitAtual == FLAG || intBitAtual == ESC) {
          enquadrado[ptr++] = ESC;
          enquadrado[ptr++] = intBitAtual;
        } else {
          enquadrado[ptr++] = intBitAtual;
          contadorByteFlagIntermediaria++;
        } // fim do if
        //if para escrever a FLAG intermediaria
        if (contadorByteFlagIntermediaria == TAMANHO_SUBQUADRO_BYTES) {
            // if que verifica se a proxima posicao ainda nao eh a ultima (reservada para a FLAG final)
            if (ptr < enquadrado.length -1) {
                enquadrado[ptr++] = FLAG;
                contadorByteFlagIntermediaria = 0;
            } // fim do if
        } // fim do if
      } // fim do for
    } // fim do for

    enquadrado[ptr] = FLAG; // adiciona a flag de fim
    // reempacotar
    int tamanhoInts = (tamanhoFinal+ 3) / 4;
    int[] quadroEnquadrado = new int[tamanhoInts];

    //for para reempacotar o inteiro
    for(int i = 0; i < tamanhoInts; i++) {
      int inteiro = 0;
      // for para manipular os bits
      for(int j = 0; j < 4; j++) {
        int indice = i * 4 + j;
        // if para deslocar os bits
        if(indice < enquadrado.length) {
          int x = enquadrado[indice];
          inteiro |= (x << (24 - j * 8));
        } // fim do if
      } // fim do for
      quadroEnquadrado[i] = inteiro;
    } // fim do for
    return quadroEnquadrado; // retorno da funcao
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramentoInsercaoDeBits
  * Funcao: enquadra os bits (Insercao de Bits) com flags a cada 32 bits e passa para a proxima camada
  * @param quadro | bits recebidos
  * @return quadroEnquadrado 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBits(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    int totalDeBitsOriginal = quadro.length * 32;
    final int FLAG = 0b01111110;
    final int TAMANHO_BLOCO = 32;
    int tamanhoFinalBits = 0;
    tamanhoFinalBits += 8; // FLAG inicial

    int contadorDeUns = 0;
    for (int i = 0; i < totalDeBitsOriginal; i++) {
        if (i > 0 && i % TAMANHO_BLOCO == 0) {
            tamanhoFinalBits += 8; // Adiciona espaco para a FLAG intermediaria
            contadorDeUns = 0;
        }
        
        int bit = auxiliar.lerBits(quadro, i, 1);
        tamanhoFinalBits++; // Adiciona o bit de dado

        if (bit == 1) {
            contadorDeUns++;
            if (contadorDeUns == 5) {
                tamanhoFinalBits++; // Adiciona espaco para o bit de stuffing
                contadorDeUns = 0;
            }
        } else {
            contadorDeUns = 0;
        }
    }
    tamanhoFinalBits += 8; // FLAG final

    int tamanhoFinalInts = (tamanhoFinalBits + 31) / 32;
    int[] quadroEnquadrado = new int[tamanhoFinalInts];
    int ponteiroEscrita = 0;

    // Escreve a FLAG inicial
    auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita, FLAG, 8);
    ponteiroEscrita += 8;

    contadorDeUns = 0;
    // for para iterar sobre os bits originais, escrevendo dados, flags e fazendo stuffing
    for (int i = 0; i < totalDeBitsOriginal; i++) {
        // if para verificar se completamos um bloco, escreve a FLAG intermediaria
        if (i > 0 && i % TAMANHO_BLOCO == 0) {
            auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita, FLAG, 8);
            ponteiroEscrita += 8;
            contadorDeUns = 0; 
        } // fim do if
        
        int bit = auxiliar.lerBits(quadro, i, 1);
        auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita++, bit, 1);

        // if para verificar se eh 1
        if (bit == 1) {
            contadorDeUns++;
            // if para adicionar o 0
            if (contadorDeUns == 5) {
                auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita++, 0, 1);
                contadorDeUns = 0;
            } // fim do if
        } else {
            contadorDeUns = 0;
        } // fim do if-else
    } // fim do for

    // Escreve a FLAG final
    auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita, FLAG, 8);

    return quadroEnquadrado;
  } // fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramentoViolacaoCamadaFisica
  * Funcao: enquadra os bits (Violacao da Camada Fisica) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica(int[] quadro) {
    return quadro;
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceTransmissoraControleDeErroBitParidadePar
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro, ou pelo menos controladas  e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceTransmissoraControleDeErroBitParidadePar(int[] quadro) {
    return quadro;
  } //fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceTransmissoraControleDeErroBitParidadeImpar
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro, ou pelo menos controladas  e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceTransmissoraControleDeErroBitParidadeImpar(int[] quadro) {
    return quadro;
  } // fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceTransmissoraControleDeErroCRC
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro, ou pelo menos controladas  e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceTransmissoraControleDeErroCRC(int[] quadro) {
    return quadro;
  } // fim do metodo
    /**************************************************************
  * Metodo: CamadadeEnlaceTransmissoraControleDeErroCodigoDeHamming
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro, ou pelo menos controladas e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceTransmissoraControleDeErroCodigoDeHamming(int[] quadro) {
    return quadro;
  } // fim do metodo
} // Fim da classe