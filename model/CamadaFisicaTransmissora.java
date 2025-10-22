/***************************************************************** 
* Autor............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 19/08/2025
* Ultima alteracao.: 29/08/2025
* Nome.............: CamadaFisicaTransmissora
* Funcao...........: Codifica os bits da mensagem recebida
*************************************************************** */

package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;

public class CamadaFisicaTransmissora {
/**************************************************************
* Metodo: CamadaFisicaTransmissora
* Funcao: envia a mensagem (em bits) codificada para a proxima camada
* @param quadro | mensagem recebida (em bits)
* @return void 
* ********************************************************* */
  public CamadaFisicaTransmissora(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    int tipoDeCodificacao = auxiliar.numberCodification(controller.getCodificacao()); // codificacao escolhida
    
    int[] fluxoBrutoDeBits; // Fluxo de bits depois de serem codificados
    for(int i  = 0; i < quadro.length; i++) {
      controller.setTextAreaCodificada(auxiliar.binaryString(quadro[i]));
    }
    // switch para codificar corretamente os bits
    switch (tipoDeCodificacao) {
      case 0:
        fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoBinaria(quadro); // Codificacao binaria
        break;
      case 1:
        fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoManchester(quadro); // codificacao manchester
        break;
      case 2:
        fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoManchesterDiferencial(quadro); //codificacao manchester diferencial
        break;
      default:
        fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoBinaria(quadro); //Binario por padrao
        break;
    } // Fim do switch

    // Calcula o numero de bits exatos apos o enquadramento
    /* *********************************************************
      ATENCAO, ESSA PARTE EH SOMENTE PARA MOSTRAR NA GUI, NAO TEM VALOR FUNCIONAL
    ********************************************************* */
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < fluxoBrutoDeBits.length; i++) {
      sb.append(auxiliar.binaryString(fluxoBrutoDeBits[i]));
      if (i < fluxoBrutoDeBits.length - 1) {
        sb.append(" ");
      }
    }
    controller.setTextAreaCodificada(sb.toString());

    int totalDeBitsParaAnimar = quadro.length * 32;
    // Se a codificacao dobra o numero de bits (Manchester por exemplo), a animacao tambem deve dobrar.
    if (tipoDeCodificacao != 0) {
        totalDeBitsParaAnimar *= 2;
    }
    auxiliar.animate(controller, fluxoBrutoDeBits, totalDeBitsParaAnimar);
    new MeioDeComunicacao(fluxoBrutoDeBits);
  } // Fim do metodo
  /* *********************************************************
      ATENCAO, ESSA PARTE EH SOMENTE PARA MOSTRAR NA GUI, NAO TEM VALOR FUNCIONAL
  ********************************************************* */
  
  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoBinaria
  * Funcao: envia a mensagem (em bits) codificada em binario para a proxima camada
  * @param quadro | mensagem recebida (em bits)
  * @return a mensagem eh igual aos bits em binario 
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraCodificacaoBinaria(int[] quadro) {
    return quadro; // Em binario ja eh igual aos bits
  } // Fim do metodo

  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoManchester
  * Funcao: envia a mensagem (em bits) codificada em manchester para a proxima camada
  * @param quadro | mensagem recebida (em bits)
  * @return a mensagem codificada em manchester
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraCodificacaoManchester(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    // if para verificar se precisamos usar a violacao de camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      return CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(quadro);
    } // fim do if
    int bitsOriginais = quadro.length * 32; // cria um int com a quantidade de bits originais
    
    // O array Manchester tera o dobro de bits, calculamos quantos 'ints' sao necessarios
    int numeroDeBitsManchester = bitsOriginais * 2;
    int tamanhoArrayManchester = (numeroDeBitsManchester + 31) / 32; // Formula para arredondar para cima a divisao
    int[] manchester = new int[tamanhoArrayManchester];

    //for para percorrer os bits e realizar a codificacao manchester
    for (int i = 0; i < bitsOriginais; i++) {
        // Pega o bit original da posicao i
        int indiceDoIntOriginal = i / 32; // calcula o indice do int original
        int indiceDoBitNoIntOriginal = 31 - (i % 32); // Do bit mais significativo para o menos
        int bitOriginal = (quadro[indiceDoIntOriginal] >> indiceDoBitNoIntOriginal) & 1; // pega o bit original e calcula o deslocamento

        // Calcula o par Manchester 
        int bit1 = bitOriginal;
        int bit2 = bitOriginal ^ 1; // operacao XOR para inverter o bit

        // Calcula a posicao dos 2 novos bits no array de destino
        int indiceBit1 = i * 2;
        int indiceBit2 = (i * 2) + 1;

        // Escreve o primeiro bit do par
        int indiceInt1 = indiceBit1 / 32;
        int indiceBitInt1 = 31 - (indiceBit1 % 32);
        //if para verificar o valor do bit
        if (bit1 == 1) {
            manchester[indiceInt1] |= (1 << indiceBitInt1);
        } // fim do if

        // Escreve o segundo bit do par
        int indiceInt2 = indiceBit2 / 32;
        int indiceBitInt2 = 31 - (indiceBit2 % 32);
        // if para verificar o valor do segundo bit
        if (bit2 == 1) {
            manchester[indiceInt2] |= (1 << indiceBitInt2);
        } // fim do bit
    }
    return manchester; // retorno da funcao
  } // Fim do metodo

  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoManchesterDiferencial
  * Funcao: envia a mensagem (em bits) codificada em manchester diferencial para a proxima camada
  * @param  quadro | mensagem recebida (em bits)
  * @return a mensagem codificada em manchester diferencial 
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraCodificacaoManchesterDiferencial(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    // if para verificar se precisamos usar a violacao de camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      return CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(quadro);
    } // fim do if
    int bitsOriginais = quadro.length * 32; // cria um int com a quantidade de bits originais

    // O array codificado tera o dobro de bits.
    int numeroDeBitsManchester = bitsOriginais * 2;
    int tamanhoArrayManchester = (numeroDeBitsManchester + 31) / 32;
    int[] diferencial = new int[tamanhoArrayManchester];
    
    // O ultimo nivel de sinal. Comecamos com 1 (alto) por escolha propria.
    int ultimoNivel = 1; 

    //for para percorrer os bits e realizar a codificacao de manchester diferencial
    for (int i = 0; i < bitsOriginais; i++) {
        // Pega o bit original da posicao 'i'
        int indiceDoIntOriginal = i / 32;
        int indiceDoBitNoIntOriginal = 31 - (i % 32);
        int bitOriginal = (quadro[indiceDoIntOriginal] >> indiceDoBitNoIntOriginal) & 1;

        // Logica do Manchester Diferencial
        // Se o bit for '0', ha uma transicao no inicio do bit.
        // Se o bit for '1', nao ha transicao.
        if (bitOriginal == 0) {
            ultimoNivel = ultimoNivel ^ 1; // Inverte o nivel do sinal
        }

        // Codificacao Manchester: o primeiro nivel eh o que definimos, 
        // o segundo eh sempre o inverso.
        int bit1 = ultimoNivel;
        int bit2 = ultimoNivel ^ 1;

        // Calcula a posicao dos 2 novos bits no array de destino
        int indiceBit1 = i * 2;
        int indiceBit2 = (i * 2) + 1;

        // Escreve o primeiro bit do par
        int indiceInt1 = indiceBit1 / 32;
        int indiceBitInt1 = 31 - (indiceBit1 % 32);
        //if para verificar o valor do primeiro bit
        if (bit1 == 1) {
            diferencial[indiceInt1] |= (1 << indiceBitInt1);
        } // fim do if

        // Escreve o segundo bit do par
        int indiceInt2 = indiceBit2 / 32;
        int indiceBitInt2 = 31 - (indiceBit2 % 32);
        //if para verificar o valor do segundo bit
        if (bit2 == 1) {
            diferencial[indiceInt2] |= (1 << indiceBitInt2);
        } // fim do if
        
        // Atualiza o ultimoNivel para o proximo bit
        ultimoNivel = bit2;
    }
    return diferencial; // retorno da funcao
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraEnquadramentoViolacaoFisica
  * Funcao: Adiciona as flags de inicio e fim (1100) para o enquadramento de violacao da camada fisica.
  * @param quadro | quadro de bits original
  * @return int[] | novo quadro com as flags
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    String cod = controller.getCodificacao();
    int tipoDeCodificacao = auxiliar.numberCodification(cod);
    final int VIOLACAO = 0b1100;
    final int TAMANHO_VIOLACAO_BITS = 4;

    final int TAMANHO_SUBQUADRO_EM_BITS = 32; // a cada 32 bits adiciona uma flag

    int totalBitsMensagem = auxiliar.descobrirTotalDeBitsReais(quadro);
    if (totalBitsMensagem == 0)
      return new int[0]; // se a mensagem ta vazia nem finaliza o processamento

    // calcula um tamanho MAXIMO estimado para o buffer temporario, nao exato pois
    // sera aparado depois
    int numSubquadrosEstimado = (totalBitsMensagem + TAMANHO_SUBQUADRO_EM_BITS - 1) / TAMANHO_SUBQUADRO_EM_BITS;
    int totalBitsSinalEstimado = (TAMANHO_VIOLACAO_BITS * (numSubquadrosEstimado + 1)) + (totalBitsMensagem * 2);
    int[] bufferTemporario = new int[(totalBitsSinalEstimado + 31) / 32];
    int bitEscritaGlobal = 0;

    // escreve a violacao de INICIO (1100)
    auxiliar.escreverBits(bufferTemporario, bitEscritaGlobal, VIOLACAO, TAMANHO_VIOLACAO_BITS);
    bitEscritaGlobal += TAMANHO_VIOLACAO_BITS; // pula os 4 bits que foram escritos pra violacao

    // codifica os dados da mensagem
    int nivelAtual = 1; // para Manchester Diferencial

    int contadorBitsSubquadro = 0;

    for (int i = 0; i < totalBitsMensagem; i++) {
      int bitOriginal = auxiliar.lerBits(quadro, i, 1);

      if (tipoDeCodificacao == 1 || tipoDeCodificacao == 2) { // Manchester ou Diferencial
        int sinal1, sinal2;
        if (tipoDeCodificacao == 1) { // Manchester
          sinal1 = (bitOriginal == 1) ? 1 : 0;
          sinal2 = (bitOriginal == 1) ? 0 : 1;
        } else { // Manchester Diferencial
          if (bitOriginal == 0)
            nivelAtual = 1 - nivelAtual;
          sinal1 = nivelAtual;
          nivelAtual = 1 - nivelAtual;
          sinal2 = nivelAtual;
        }
        auxiliar.escreverBits(bufferTemporario, bitEscritaGlobal++, sinal1, 1);
        auxiliar.escreverBits(bufferTemporario, bitEscritaGlobal++, sinal2, 1);
      } // fim do if

      contadorBitsSubquadro++;

      // verifica se o quadro acabou ou se eh o fim da mensagem para adiconar a flag 
      boolean ehFimDoSubquadro = (contadorBitsSubquadro == TAMANHO_SUBQUADRO_EM_BITS);
      boolean ehFimDaMensagem = (i == totalBitsMensagem - 1);

      // if para verificar o fim
      if (ehFimDoSubquadro || ehFimDaMensagem) {
        // Escreve a violacao de FIM de subquadro (que tambem serve como FIM da
        // mensagem)
        auxiliar.escreverBits(bufferTemporario, bitEscritaGlobal, VIOLACAO, TAMANHO_VIOLACAO_BITS);
        bitEscritaGlobal += TAMANHO_VIOLACAO_BITS;
        // Zera o contador para o proximo subquadro
        contadorBitsSubquadro = 0;
      } // fim do if 

    } // fim do for

    // reorganiza o array
    int tamanhoArrayFinal = (bitEscritaGlobal + 31) / 32;
    int[] fluxoBrutoDeBitsFinal = new int[tamanhoArrayFinal];

    // for para copiar apenas os bits validos do buffer temporario para o array final
    for (int i = 0; i < bitEscritaGlobal; i++) {
      int bit = auxiliar.lerBits(bufferTemporario, i, 1);
      auxiliar.escreverBits(fluxoBrutoDeBitsFinal, i, bit, 1);
    } // fim do for

    return fluxoBrutoDeBitsFinal; // retorna o array 
  }// fim do metodo
} // Fim da classe